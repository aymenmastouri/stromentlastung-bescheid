package de.stromentlastung.bescheid.dokument.dto;

import java.time.LocalDate;
import java.util.List;

import de.stromentlastung.bescheid.dokument.BescheidArt;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Alles, was der Bescheid-Dienst zum Erlass braucht — als Daten, nie als Text.
 * Beträge, Mengen, Gründe und die Rechtsgrundlagen-Zitate kommen fertig aus
 * dem Rechenkern des Vorgangs (Fachkonzept B-03, Architektur Kap. 1).
 */
public record BescheidAnlage(
        @NotBlank String aktenzeichen,
        @NotBlank String unternehmenKennung,
        @NotBlank String dienststelle,
        @NotNull BescheidArt art,
        @Min(2000) int entnahmejahr,
        @NotNull @Valid Hauptzollamt hauptzollamt,
        @NotNull @Valid Adressat adressat,
        @NotNull @Valid Berechnung berechnung,
        @NotNull List<String> zitate,
        @NotNull List<String> gruende,
        String begruendung,
        Long bisherFestgesetztCent,
        Long rueckforderungCent,
        @NotNull @Valid Zeichnung zeichnung,
        LocalDate absendung) {

    public record Hauptzollamt(@NotBlank String kennung, @NotBlank String name, @NotBlank String sitz, String iban) {
    }

    public record Adressat(@NotBlank String name, String rechtsform, @NotBlank String strasse, @NotBlank String plz, @NotBlank String ort) {
    }

    public record Berechnung(long versteuertKwh, long elektromobilitaetKwh, boolean elektromobilitaetGeschaetzt,
                             long nutzenergieDritteKwh, long entlastungsfaehigKwh, long satzCentProMwh, long bruttoCent,
                             long selbstbehaltCent, long festgesetztCent, Long selbstberechnetCent) {
    }

    public record Zeichnung(@NotBlank String sachbearbeitung, @NotBlank String freigabe) {
    }
}
