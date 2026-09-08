package de.stromentlastung.bescheid.dokument;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.stromentlastung.bescheid.dokument.dto.BescheidAnlage;
import de.stromentlastung.bescheid.dokument.dto.BescheidResponse;
import jakarta.validation.Valid;

@RestController
public class BescheidController {

    private final BescheidService bescheide;

    public BescheidController(BescheidService bescheide) {
        this.bescheide = bescheide;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ZEICHNUNG', 'SACHBEARBEITUNG')")
    @ResponseStatus(HttpStatus.CREATED)
    public BescheidResponse erlasse(@Valid @RequestBody BescheidAnlage anlage) {
        return bescheide.erlasse(anlage);
    }

    @GetMapping
    public List<BescheidResponse> zumAktenzeichen(@RequestParam String aktenzeichen) {
        return bescheide.zumAktenzeichen(aktenzeichen);
    }

    @GetMapping("/postfach")
    @PreAuthorize("hasRole('ANTRAGSTELLER')")
    public List<BescheidResponse> postfach() {
        return bescheide.postfach();
    }

    @GetMapping("/dienststelle")
    @PreAuthorize("hasAnyRole('SACHBEARBEITUNG', 'ZEICHNUNG', 'PRUEFDIENST')")
    public List<BescheidResponse> derDienststelle() {
        return bescheide.derDienststelle();
    }

    @GetMapping("/{kennung}")
    public BescheidResponse einer(@PathVariable String kennung) {
        return bescheide.einer(kennung);
    }

    @GetMapping(value = "/{kennung}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable String kennung) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + kennung + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bescheide.pdf(kennung));
    }

    @PostMapping("/{kennung}/gelesen")
    @PreAuthorize("hasRole('ANTRAGSTELLER')")
    public BescheidResponse gelesen(@PathVariable String kennung) {
        return bescheide.markiereGelesen(kennung);
    }
}
