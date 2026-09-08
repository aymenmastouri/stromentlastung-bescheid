package de.stromentlastung.bescheid;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.stromentlastung.bescheid.dokument.BescheidRepository;

/** Die Saat-Bescheide sind nach dem Start gerendert und tragen einen Hash (Architektur A-05). */
@SpringBootTest
class SaatTest {

    @Autowired
    BescheidRepository bescheide;

    @Test
    void saat_ist_gerendert() {
        assertThat(bescheide.count()).isEqualTo(8);
        assertThat(bescheide.findByPdfIsNull()).isEmpty();
        var aenderung = bescheide.findByKennung("HZA-N-9b-2024-000002-B2").orElseThrow();
        assertThat(aenderung.getFaelligkeit()).isEqualTo(LocalDate.of(2026, 3, 13));
        assertThat(aenderung.getHash()).hasSize(64);
        assertThat(bescheide.findByKennung("HZA-N-9b-2025-000001-B1").orElseThrow().getBekanntgabe()).isEqualTo(LocalDate.of(2026, 5, 4));
    }
}
