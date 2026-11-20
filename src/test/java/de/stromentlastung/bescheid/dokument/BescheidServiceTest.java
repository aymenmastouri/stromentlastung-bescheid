package de.stromentlastung.bescheid.dokument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.stromentlastung.bescheid.common.CurrentUser;
import de.stromentlastung.bescheid.common.RegelVerletzung;
import de.stromentlastung.bescheid.common.ZugriffVerweigert;
import de.stromentlastung.bescheid.dokument.dto.BescheidAnlage;
import de.stromentlastung.bescheid.dokument.dto.BescheidResponse;

class BescheidServiceTest {

    private final BescheidRepository repo = mock(BescheidRepository.class);
    private final CurrentUser user = mock(CurrentUser.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-02-09T10:00:00Z"), ZoneId.of("Europe/Berlin"));
    private final BescheidService service = new BescheidService(repo, new BescheidRenderer(),
            new ObjectMapper().registerModule(new JavaTimeModule()), user, clock);

    static BescheidAnlage anlage(BescheidArt art, Long bisher, Long rueckforderung, List<String> gruende) {
        return new BescheidAnlage("HZA-N-9b-2024-000002", "U-007", "HZA-N", art, 2024,
                new BescheidAnlage.Hauptzollamt("HZA-N", "Hauptzollamt Nord", "Kiel", "DE02120300000000202051"),
                new BescheidAnlage.Adressat("Ostsee Werft GmbH", "GmbH", "Werftallee 1", "18055", "Rostock"),
                new BescheidAnlage.Berechnung(1500000, 311500, false, 0, 1188500, 2000, 2377000, 25000, 2352000, null),
                List.of("§ 9b Abs. 1 StromStG", "§ 9b Abs. 2a StromStG"), gruende, null, bisher, rueckforderung,
                new BescheidAnlage.Zeichnung("Murat Demir", "Lars Becker"), null);
    }

    @Test
    void aenderungsbescheid_bekommt_die_zweite_kennung_und_eine_faelligkeit_einen_monat_nach_bekanntgabe() {
        when(user.dienststelleOderVerweigert()).thenReturn("HZA-N");
        when(repo.countByAktenzeichen("HZA-N-9b-2024-000002")).thenReturn(1L);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BescheidResponse r = service.erlasse(anlage(BescheidArt.AENDERUNG, 2975000L, 623000L, List.of("ABZUG_NACH_PRUEFUNG_GEAENDERT")));

        assertThat(r.kennung()).isEqualTo("HZA-N-9b-2024-000002-B2");
        assertThat(r.absendung()).isEqualTo(LocalDate.of(2026, 2, 9));
        assertThat(r.bekanntgabe()).isEqualTo(LocalDate.of(2026, 2, 13));
        assertThat(r.faelligkeit()).isEqualTo(LocalDate.of(2026, 3, 13));
        assertThat(r.rueckforderungCent()).isEqualTo(623000);
        assertThat(r.hash()).hasSize(64);
        assertThat(r.zitate()).containsExactly("§ 9b Abs. 1 StromStG", "§ 9b Abs. 2a StromStG");
    }

    @Test
    void fremde_dienststelle_erlaesst_nichts() {
        when(user.dienststelleOderVerweigert()).thenReturn("HZA-S");
        assertThatThrownBy(() -> service.erlasse(anlage(BescheidArt.ENTLASTUNG, null, null, List.of())))
                .isInstanceOf(ZugriffVerweigert.class);
    }

    @Test
    void aenderung_ohne_rueckforderung_und_ablehnung_ohne_grund_sind_regelverletzungen() {
        when(user.dienststelleOderVerweigert()).thenReturn("HZA-N");
        assertThatThrownBy(() -> service.erlasse(anlage(BescheidArt.AENDERUNG, null, null, List.of())))
                .isInstanceOf(RegelVerletzung.class).extracting("code").isEqualTo("AENDERUNG_OHNE_RUECKFORDERUNG");
        assertThatThrownBy(() -> service.erlasse(anlage(BescheidArt.ABLEHNUNG, null, null, List.of())))
                .isInstanceOf(RegelVerletzung.class).extracting("code").isEqualTo("ABLEHNUNG_OHNE_GRUND");
    }

    @Test
    void die_pdf_datei_ist_eine_pdf_datei() {
        byte[] pdf = new BescheidRenderer().render(anlage(BescheidArt.ENTLASTUNG, null, null, List.of()),
                "HZA-N-9b-2024-000002-B1", LocalDate.of(2025, 5, 20), LocalDate.of(2025, 5, 26), null);
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(1500);
        when(repo.findByKennung("X")).thenReturn(Optional.empty());
    }
}
