package de.stromentlastung.bescheid.dokument;

import java.util.Map;

/**
 * Versionierte Textbausteine der Bescheide (Fachkonzept Kap. 8). Rechtsgrundlagen
 * werden hier nicht formuliert; sie kommen als Zitate aus dem Rechenkern (B-03).
 */
final class Textbausteine {

    static final String VERSION = "1.0";

    static final String HINWEIS = "Referenzverfahren – kein Echtbetrieb. Dieser Bescheid ist zu Demonstrationszwecken erzeugt und entfaltet keine Rechtswirkung.";

    static final String NEBENBESTIMMUNG = "Dieser Bescheid ergeht unter dem Vorbehalt der Nachprüfung (§ 164 Abs. 1 AO). Der Vorbehalt entfällt mit Ablauf der Festsetzungsfrist (§ 164 Abs. 4 AO).";

    private static final Map<String, String> GRUENDE = Map.of(
            "ZUORDNUNG_FEHLT", "Das Unternehmen ist für das Entnahmejahr keinem Abschnitt des Produzierenden Gewerbes und nicht der Land- und Forstwirtschaft zugeordnet (§ 2 Nr. 3 und 5 StromStG, § 15 StromStV).",
            "FESTSETZUNGSVERJAEHRUNG", "Der Antrag ist erst nach Ablauf der Festsetzungsfrist gestellt worden (§ 17b Abs. 1 Satz 3 StromStV in Verbindung mit § 169 Abs. 2 Satz 1 Nr. 1 und § 170 Abs. 1 AO).",
            "ENTLASTUNG_UNTER_SELBSTBEHALT", "Der Entlastungsbetrag übersteigt im Kalenderjahr den Betrag von 250 Euro nicht (§ 9b Abs. 2 Satz 2 StromStG).",
            "BEIHILFEAUSSCHLUSS", "Die beihilferechtlichen Voraussetzungen liegen nicht vor (§ 2a StromStG).",
            "NACHWEIS_UNZUREICHEND", "Die verbrauchte Strommenge und ihr Verwendungszweck sind nicht ausreichend nachgewiesen (§ 17b Abs. 6 StromStV).",
            "RUECKFRAGE_UNBEANTWORTET", "Die Entlastungsvoraussetzungen konnten ohne die angeforderten Angaben nicht festgestellt werden (§ 17b Abs. 3 StromStV).",
            "MENGE_NACH_PRUEFUNG_GEAENDERT", "Nach dem Ergebnis der Prüfung weicht die entlastungsfähige Strommenge von der Anmeldung ab.",
            "ABZUG_NACH_PRUEFUNG_GEAENDERT", "Nach dem Ergebnis der Prüfung sind Strommengen für Elektromobilität oder für Nutzenergie an andere Unternehmen abzuziehen (§ 9b Abs. 1 Satz 2 und 4 StromStG).");

    private Textbausteine() {
    }

    static String titel(BescheidArt art) {
        return switch (art) {
            case ENTLASTUNG -> "Bescheid über die Steuerentlastung für Unternehmen nach § 9b StromStG";
            case ABLEHNUNG -> "Ablehnung des Antrags auf Steuerentlastung nach § 9b StromStG";
            case AENDERUNG -> "Änderungsbescheid zur Steuerentlastung nach § 9b StromStG";
        };
    }

    static String grund(String code) {
        return GRUENDE.getOrDefault(code, code);
    }

    static String rechtsbehelfsbelehrung(String hauptzollamt, String sitz) {
        return "Gegen diesen Bescheid kann innerhalb eines Monats nach Bekanntgabe Einspruch eingelegt werden (§ 355 Abs. 1 AO). "
                + "Der Einspruch ist bei dem " + hauptzollamt + ", " + sitz + ", schriftlich oder elektronisch einzureichen "
                + "oder zur Niederschrift zu erklären (§ 357 Abs. 1 AO).";
    }
}
