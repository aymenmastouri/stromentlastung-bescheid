package de.stromentlastung.bescheid.dokument;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.stromentlastung.bescheid.common.CurrentUser;
import de.stromentlastung.bescheid.common.NichtGefunden;
import de.stromentlastung.bescheid.common.RegelVerletzung;
import de.stromentlastung.bescheid.common.ZugriffVerweigert;
import de.stromentlastung.bescheid.dokument.dto.BescheidAnlage;
import de.stromentlastung.bescheid.dokument.dto.BescheidResponse;

@Service
@Transactional
public class BescheidService {

    static final String TRENNER = " | ";

    private final BescheidRepository bescheide;
    private final BescheidRenderer renderer;
    private final ObjectMapper json;
    private final CurrentUser user;
    private final Clock clock;

    public BescheidService(BescheidRepository bescheide, BescheidRenderer renderer, ObjectMapper json, CurrentUser user, Clock clock) {
        this.bescheide = bescheide;
        this.renderer = renderer;
        this.json = json;
        this.user = user;
        this.clock = clock;
    }

    /** Erlässt einen Bescheid: Kennung, Absendung, Bekanntgabe (F-03), Fälligkeit (F-05), PDF und Hash. */
    public BescheidResponse erlasse(BescheidAnlage a) {
        String dienststelle = user.dienststelleOderVerweigert();
        if (!dienststelle.equals(a.dienststelle())) {
            throw new ZugriffVerweigert("Bescheid für " + a.dienststelle() + " kann nicht von " + dienststelle + " erlassen werden");
        }
        if (a.art() == BescheidArt.ABLEHNUNG && a.gruende().isEmpty()) {
            throw new RegelVerletzung("ABLEHNUNG_OHNE_GRUND", "ein Ablehnungsbescheid braucht mindestens einen Ablehnungsgrund");
        }
        if (a.art() == BescheidArt.AENDERUNG && (a.rueckforderungCent() == null || a.rueckforderungCent() <= 0 || a.bisherFestgesetztCent() == null)) {
            throw new RegelVerletzung("AENDERUNG_OHNE_RUECKFORDERUNG", "ein Änderungsbescheid trägt die bisherige Festsetzung und einen Rückforderungsbetrag");
        }
        String kennung = a.aktenzeichen() + "-B" + (bescheide.countByAktenzeichen(a.aktenzeichen()) + 1);
        LocalDate absendung = a.absendung() != null ? a.absendung() : LocalDate.now(clock);
        LocalDate bekanntgabe = Werktage.bekanntgabe(absendung);
        LocalDate faelligkeit = a.art() == BescheidArt.AENDERUNG ? Werktage.monatsfrist(bekanntgabe) : null;
        Bescheid b = new Bescheid(kennung, a.aktenzeichen(), a.unternehmenKennung(), a.dienststelle(), a.art(), a.entnahmejahr(),
                absendung, bekanntgabe, a.berechnung().festgesetztCent(), a.art() == BescheidArt.AENDERUNG ? a.rueckforderungCent() : null,
                faelligkeit, String.join(",", a.gruende()), String.join(TRENNER, a.zitate()), schreibe(a), Textbausteine.VERSION, Instant.now(clock));
        byte[] pdf = renderer.render(a, kennung, absendung, bekanntgabe, faelligkeit);
        b.hinterlegePdf(pdf, Hash.sha256(pdf));
        return response(bescheide.save(b));
    }

    @Transactional(readOnly = true)
    public List<BescheidResponse> postfach() {
        return bescheide.findByUnternehmenKennungOrderByDatumDescIdDesc(user.unternehmenOderVerweigert()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<BescheidResponse> zumAktenzeichen(String aktenzeichen) {
        List<Bescheid> liste = bescheide.findByAktenzeichenOrderByDatumAscIdAsc(aktenzeichen);
        liste.forEach(this::pruefeLesen);
        return liste.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<BescheidResponse> derDienststelle() {
        return bescheide.findByDienststelleOrderByDatumDescIdDesc(user.dienststelleOderVerweigert()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public BescheidResponse einer(String kennung) {
        Bescheid b = lade(kennung);
        pruefeLesen(b);
        return response(b);
    }

    @Transactional(readOnly = true)
    public byte[] pdf(String kennung) {
        Bescheid b = lade(kennung);
        pruefeLesen(b);
        if (b.getPdf() == null) {
            throw new NichtGefunden("Bescheid " + kennung + " ist noch nicht gerendert");
        }
        return b.getPdf();
    }

    public BescheidResponse markiereGelesen(String kennung) {
        Bescheid b = lade(kennung);
        if (!user.unternehmen().map(b.getUnternehmenKennung()::equals).orElse(false)) {
            throw new ZugriffVerweigert("Bescheid " + kennung + " gehört zu " + b.getUnternehmenKennung());
        }
        b.markiereGelesen(Instant.now(clock));
        return response(b);
    }

    /** Saat-Bescheide tragen Daten, aber keine PDF-Datei; sie werden beim Start gerendert (Architektur A-05). */
    public int renderFehlende() {
        List<Bescheid> offen = bescheide.findByPdfIsNull();
        for (Bescheid b : offen) {
            byte[] pdf = renderer.render(lies(b.getDaten()), b.getKennung(), b.getAbsendung(), b.getBekanntgabe(), b.getFaelligkeit());
            b.hinterlegePdf(pdf, Hash.sha256(pdf));
        }
        return offen.size();
    }

    private Bescheid lade(String kennung) {
        return bescheide.findByKennung(kennung).orElseThrow(() -> new NichtGefunden("Bescheid " + kennung));
    }

    private void pruefeLesen(Bescheid b) {
        boolean eigenes = user.unternehmen().map(b.getUnternehmenKennung()::equals).orElse(false);
        boolean eigeneDienststelle = user.dienststelle().map(b.getDienststelle()::equals).orElse(false);
        if (!eigenes && !eigeneDienststelle) {
            throw new ZugriffVerweigert("kein Zugriff auf Bescheid " + b.getKennung());
        }
    }

    private String schreibe(BescheidAnlage a) {
        try {
            return json.writeValueAsString(a);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Bescheiddaten nicht serialisierbar", e);
        }
    }

    BescheidAnlage lies(String daten) {
        try {
            return json.readValue(daten, BescheidAnlage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Bescheiddaten nicht lesbar", e);
        }
    }

    private BescheidResponse response(Bescheid b) {
        List<String> gruende = b.getGruende().isBlank() ? List.of() : Arrays.asList(b.getGruende().split(","));
        List<String> zitate = b.getZitate().isBlank() ? List.of() : Arrays.asList(b.getZitate().split(java.util.regex.Pattern.quote(TRENNER)));
        return new BescheidResponse(b.getKennung(), b.getAktenzeichen(), b.getUnternehmenKennung(), b.getDienststelle(), b.getArt(),
                b.getEntnahmejahr(), b.getDatum(), b.getAbsendung(), b.getBekanntgabe(), Werktage.monatsfrist(b.getBekanntgabe()),
                b.getBetragCent(), b.getRueckforderungCent(), b.getFaelligkeit(), gruende, zitate, b.getHash(),
                b.getTextbausteinVersion(), b.getGelesenAm());
    }
}
