package de.stromentlastung.bescheid.dokument.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import de.stromentlastung.bescheid.dokument.BescheidArt;

public record BescheidResponse(String kennung, String aktenzeichen, String unternehmen, String dienststelle, BescheidArt art,
                               int entnahmejahr, LocalDate datum, LocalDate absendung, LocalDate bekanntgabe,
                               LocalDate einspruchsfristEnde, long betragCent, Long rueckforderungCent, LocalDate faelligkeit,
                               List<String> gruende, List<String> zitate, String hash, String textbausteinVersion,
                               Instant gelesenAm) {
}
