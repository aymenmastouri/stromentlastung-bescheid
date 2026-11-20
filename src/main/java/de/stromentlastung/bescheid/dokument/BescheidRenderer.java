package de.stromentlastung.bescheid.dokument;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.stromentlastung.bescheid.dokument.dto.BescheidAnlage;

/** Setzt die Daten eines Bescheids in die Textbausteine und erzeugt die PDF-Datei (Fachkonzept B-01 bis B-08). */
@Component
public class BescheidRenderer {

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FETT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font TITEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font KLEIN = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

    public byte[] render(BescheidAnlage a, String kennung, LocalDate absendung, LocalDate bekanntgabe, LocalDate faelligkeit) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 56, 56, 56, 56);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            absatz(doc, a.hauptzollamt().name() + " · " + a.hauptzollamt().sitz(), FETT);
            absatz(doc, "Aktenzeichen " + a.aktenzeichen() + " · Bescheid " + kennung + " · Datum " + DATUM.format(absendung), NORMAL);
            absatz(doc, " ", NORMAL);
            absatz(doc, a.adressat().name() + (a.adressat().rechtsform() == null ? "" : ""), NORMAL);
            absatz(doc, a.adressat().strasse(), NORMAL);
            absatz(doc, a.adressat().plz() + " " + a.adressat().ort(), NORMAL);
            absatz(doc, " ", NORMAL);
            absatz(doc, Textbausteine.titel(a.art()), TITEL);
            absatz(doc, "Entnahmejahr " + a.entnahmejahr(), FETT);
            absatz(doc, Textbausteine.HINWEIS, KLEIN);
            absatz(doc, " ", NORMAL);
            absatz(doc, tenor(a), NORMAL);
            absatz(doc, " ", NORMAL);
            absatz(doc, "Berechnung", FETT);
            doc.add(berechnung(a));
            if (!a.gruende().isEmpty() || (a.begruendung() != null && !a.begruendung().isBlank())) {
                absatz(doc, " ", NORMAL);
                absatz(doc, "Gründe", FETT);
                for (String code : a.gruende()) {
                    absatz(doc, "– " + Textbausteine.grund(code), NORMAL);
                }
                if (a.begruendung() != null && !a.begruendung().isBlank()) {
                    absatz(doc, a.begruendung(), NORMAL);
                }
            }
            absatz(doc, " ", NORMAL);
            absatz(doc, "Rechtsgrundlagen", FETT);
            for (String zitat : a.zitate()) {
                absatz(doc, "– " + zitat, NORMAL);
            }
            if (a.art() != BescheidArt.ABLEHNUNG) {
                absatz(doc, " ", NORMAL);
                absatz(doc, "Nebenbestimmung", FETT);
                absatz(doc, Textbausteine.NEBENBESTIMMUNG, NORMAL);
            }
            if (a.art() == BescheidArt.AENDERUNG && a.rueckforderungCent() != null) {
                absatz(doc, " ", NORMAL);
                absatz(doc, "Leistungsgebot", FETT);
                absatz(doc, "Der zu viel gewährte Entlastungsbetrag von " + euro(a.rueckforderungCent()) + " ist bis zum "
                        + DATUM.format(faelligkeit) + " auf das Konto " + a.hauptzollamt().iban() + " des " + a.hauptzollamt().name()
                        + " zurückzuzahlen. Wird der Betrag nicht bis zum Ablauf des Fälligkeitstages entrichtet, entstehen Säumniszuschläge (§ 240 AO).", NORMAL);
            }
            absatz(doc, " ", NORMAL);
            absatz(doc, "Rechtsbehelfsbelehrung", FETT);
            absatz(doc, Textbausteine.rechtsbehelfsbelehrung(a.hauptzollamt().name(), a.hauptzollamt().sitz()), NORMAL);
            absatz(doc, " ", NORMAL);
            absatz(doc, "Im Auftrag: " + a.zeichnung().sachbearbeitung() + " · Freigabe: " + a.zeichnung().freigabe(), NORMAL);
            absatz(doc, "Abgesendet am " + DATUM.format(absendung) + "; gilt am " + DATUM.format(bekanntgabe)
                    + " als bekannt gegeben (§ 122 Abs. 2a AO). Textbausteine " + Textbausteine.VERSION + ".", KLEIN);
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Bescheid " + kennung + " konnte nicht erzeugt werden", e);
        }
        return out.toByteArray();
    }

    private static String tenor(BescheidAnlage a) {
        BescheidAnlage.Berechnung b = a.berechnung();
        return switch (a.art()) {
            case ENTLASTUNG -> "Auf Ihren Antrag wird die Steuerentlastung für den im Entnahmejahr " + a.entnahmejahr()
                    + " für betriebliche Zwecke entnommenen Strom auf " + euro(b.festgesetztCent()) + " festgesetzt.";
            case ABLEHNUNG -> "Ihr Antrag auf Steuerentlastung für das Entnahmejahr " + a.entnahmejahr() + " wird abgelehnt.";
            case AENDERUNG -> "Die Festsetzung der Steuerentlastung für das Entnahmejahr " + a.entnahmejahr() + " über "
                    + euro(a.bisherFestgesetztCent() == null ? 0 : a.bisherFestgesetztCent()) + " wird geändert und auf "
                    + euro(b.festgesetztCent()) + " festgesetzt. Der Unterschiedsbetrag von " + euro(a.rueckforderungCent() == null ? 0 : a.rueckforderungCent())
                    + " wird zurückgefordert.";
        };
    }

    private static PdfPTable berechnung(BescheidAnlage a) {
        BescheidAnlage.Berechnung b = a.berechnung();
        PdfPTable t = new PdfPTable(new float[] {3, 1.4f});
        t.setWidthPercentage(100);
        zeile(t, "Nach § 3 StromStG versteuert entnommener Strom", kwh(b.versteuertKwh()));
        zeile(t, "abzüglich Strom für Elektromobilität" + (b.elektromobilitaetGeschaetzt() ? " (geschätzt, § 17b Abs. 4a StromStV)" : ""), kwh(b.elektromobilitaetKwh()));
        zeile(t, "abzüglich Strom für Nutzenergie an Unternehmen außerhalb des Produzierenden Gewerbes", kwh(b.nutzenergieDritteKwh()));
        zeile(t, "Entlastungsfähige Menge", kwh(b.entlastungsfaehigKwh()));
        zeile(t, "Entlastungssatz je Megawattstunde", euro(b.satzCentProMwh()));
        zeile(t, "Entlastungsbetrag", euro(b.bruttoCent()));
        zeile(t, "abzüglich Selbstbehalt (§ 9b Abs. 2 Satz 2 StromStG)", euro(b.selbstbehaltCent()));
        zeile(t, "Festgesetzt", euro(b.festgesetztCent()));
        if (b.selbstberechnetCent() != null && b.selbstberechnetCent() != b.festgesetztCent()) {
            zeile(t, "Vom Antragsteller berechnet (abweichend)", euro(b.selbstberechnetCent()));
        }
        return t;
    }

    private static void zeile(PdfPTable t, String text, String wert) {
        PdfPCell links = new PdfPCell(new Paragraph(text, NORMAL));
        PdfPCell rechts = new PdfPCell(new Paragraph(wert, NORMAL));
        rechts.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(links);
        t.addCell(rechts);
    }

    private static void absatz(Document doc, String text, Font font) throws DocumentException {
        doc.add(new Paragraph(text, font));
    }

    static String euro(long cent) {
        return String.format(Locale.GERMANY, "%,.2f Euro", cent / 100.0);
    }

    static String kwh(long kwh) {
        return String.format(Locale.GERMANY, "%,d kWh", kwh);
    }
}
