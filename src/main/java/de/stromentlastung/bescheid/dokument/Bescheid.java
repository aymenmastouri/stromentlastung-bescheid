package de.stromentlastung.bescheid.dokument;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/** Ein erlassener Bescheid: unveränderlich, mit seiner PDF-Datei und deren Hash (Fachkonzept Kap. 8). */
@Entity
@Table(name = "bescheid")
public class Bescheid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String kennung;
    @Column(nullable = false)
    private String aktenzeichen;
    @Column(name = "unternehmen_kennung", nullable = false)
    private String unternehmenKennung;
    @Column(nullable = false)
    private String dienststelle;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BescheidArt art;
    @Column(nullable = false)
    private int entnahmejahr;
    @Column(nullable = false)
    private LocalDate datum;
    @Column(nullable = false)
    private LocalDate absendung;
    @Column(nullable = false)
    private LocalDate bekanntgabe;
    @Column(name = "betrag_cent", nullable = false)
    private long betragCent;
    @Column(name = "rueckforderung_cent")
    private Long rueckforderungCent;
    private LocalDate faelligkeit;
    @Column(nullable = false)
    private String gruende;
    @Column(nullable = false)
    private String zitate;
    @Lob
    @Column(nullable = false)
    private String daten;
    @Column(name = "textbaustein_version", nullable = false)
    private String textbausteinVersion;
    @Lob
    private byte[] pdf;
    private String hash;
    @Column(name = "gelesen_am")
    private Instant gelesenAm;
    @Column(name = "erstellt_am", nullable = false)
    private Instant erstelltAm;

    protected Bescheid() {
    }

    public Bescheid(String kennung, String aktenzeichen, String unternehmenKennung, String dienststelle, BescheidArt art,
                    int entnahmejahr, LocalDate absendung, LocalDate bekanntgabe, long betragCent, Long rueckforderungCent,
                    LocalDate faelligkeit, String gruende, String zitate, String daten, String textbausteinVersion, Instant erstelltAm) {
        this.kennung = kennung;
        this.aktenzeichen = aktenzeichen;
        this.unternehmenKennung = unternehmenKennung;
        this.dienststelle = dienststelle;
        this.art = art;
        this.entnahmejahr = entnahmejahr;
        this.datum = absendung;
        this.absendung = absendung;
        this.bekanntgabe = bekanntgabe;
        this.betragCent = betragCent;
        this.rueckforderungCent = rueckforderungCent;
        this.faelligkeit = faelligkeit;
        this.gruende = gruende;
        this.zitate = zitate;
        this.daten = daten;
        this.textbausteinVersion = textbausteinVersion;
        this.erstelltAm = erstelltAm;
    }

    public void hinterlegePdf(byte[] pdf, String hash) {
        this.pdf = pdf;
        this.hash = hash;
    }

    public void markiereGelesen(Instant jetzt) {
        if (gelesenAm == null) {
            gelesenAm = jetzt;
        }
    }

    public Long getId() { return id; }
    public String getKennung() { return kennung; }
    public String getAktenzeichen() { return aktenzeichen; }
    public String getUnternehmenKennung() { return unternehmenKennung; }
    public String getDienststelle() { return dienststelle; }
    public BescheidArt getArt() { return art; }
    public int getEntnahmejahr() { return entnahmejahr; }
    public LocalDate getDatum() { return datum; }
    public LocalDate getAbsendung() { return absendung; }
    public LocalDate getBekanntgabe() { return bekanntgabe; }
    public long getBetragCent() { return betragCent; }
    public Long getRueckforderungCent() { return rueckforderungCent; }
    public LocalDate getFaelligkeit() { return faelligkeit; }
    public String getGruende() { return gruende; }
    public String getZitate() { return zitate; }
    public String getDaten() { return daten; }
    public String getTextbausteinVersion() { return textbausteinVersion; }
    public byte[] getPdf() { return pdf; }
    public String getHash() { return hash; }
    public Instant getGelesenAm() { return gelesenAm; }
    public Instant getErstelltAm() { return erstelltAm; }
}
