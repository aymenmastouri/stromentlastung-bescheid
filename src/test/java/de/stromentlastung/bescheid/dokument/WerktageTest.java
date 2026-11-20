package de.stromentlastung.bescheid.dokument;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Fachkonzept F-00, F-03, F-05 und Architektur A-09. */
class WerktageTest {

    @Test
    void ostern_2026_ist_der_fuenfte_april() {
        assertThat(Werktage.ostersonntag(2026)).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(Werktage.ostersonntag(2024)).isEqualTo(LocalDate.of(2024, 3, 31));
    }

    @Test
    void der_vierte_tag_am_ersten_mai_wird_auf_montag_verschoben() {
        assertThat(Werktage.bekanntgabe(LocalDate.of(2026, 4, 27))).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(Werktage.bekanntgabe(LocalDate.of(2026, 2, 9))).isEqualTo(LocalDate.of(2026, 2, 13));
    }

    @Test
    void die_antragsfrist_2022_endete_am_zweiten_januar_2024() {
        assertThat(Werktage.naechsterWerktag(LocalDate.of(2023, 12, 31))).isEqualTo(LocalDate.of(2024, 1, 2));
    }

    @Test
    void monatsfrist_klemmt_am_monatsende_und_verschiebt_aufs_wochenende_folgend() {
        assertThat(Werktage.monatsfrist(LocalDate.of(2026, 2, 13))).isEqualTo(LocalDate.of(2026, 3, 13));
        assertThat(Werktage.monatsfrist(LocalDate.of(2026, 1, 31))).isEqualTo(LocalDate.of(2026, 3, 2));
    }
}
