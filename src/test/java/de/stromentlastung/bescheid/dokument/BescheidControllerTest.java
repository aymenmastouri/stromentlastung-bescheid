package de.stromentlastung.bescheid.dokument;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.stromentlastung.bescheid.common.CurrentUser;
import de.stromentlastung.bescheid.config.SecurityConfig;
import de.stromentlastung.bescheid.dokument.dto.BescheidResponse;

@WebMvcTest(BescheidController.class)
@Import({SecurityConfig.class, CurrentUser.class})
class BescheidControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    BescheidService bescheide;

    @Test
    void das_postfach_gehoert_dem_antragsteller() throws Exception {
        when(bescheide.postfach()).thenReturn(List.of(new BescheidResponse("HZA-N-9b-2025-000001-B1", "HZA-N-9b-2025-000001", "U-001",
                "HZA-N", BescheidArt.ENTLASTUNG, 2025, LocalDate.of(2026, 4, 27), LocalDate.of(2026, 4, 27), LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 6, 4), 3675000, null, null, List.of(), List.of("§ 9b Abs. 1 StromStG"), "abc", "1.0", null)));
        mvc.perform(get("/postfach").with(jwt().jwt(j -> j.claim("unternehmen", "U-001")).authorities(new SimpleGrantedAuthority("ROLE_ANTRAGSTELLER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kennung").value("HZA-N-9b-2025-000001-B1"))
                .andExpect(jsonPath("$[0].bekanntgabe").value("2026-05-04"));
        mvc.perform(get("/postfach").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SACHBEARBEITUNG"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void antragsteller_erlassen_keine_bescheide() throws Exception {
        String anlage = """
                {"aktenzeichen":"HZA-N-9b-2025-000001","unternehmenKennung":"U-001","dienststelle":"HZA-N","art":"ENTLASTUNG","entnahmejahr":2025,
                 "hauptzollamt":{"kennung":"HZA-N","name":"Hauptzollamt Nord","sitz":"Kiel","iban":"DE02"},
                 "adressat":{"name":"Nordfeld Metallbau GmbH","rechtsform":"GmbH","strasse":"Werftstraße 12","plz":"24103","ort":"Kiel"},
                 "berechnung":{"versteuertKwh":1850000,"elektromobilitaetKwh":0,"elektromobilitaetGeschaetzt":false,"nutzenergieDritteKwh":0,
                   "entlastungsfaehigKwh":1850000,"satzCentProMwh":2000,"bruttoCent":3700000,"selbstbehaltCent":25000,"festgesetztCent":3675000},
                 "zitate":["§ 9b Abs. 1 StromStG"],"gruende":[],"zeichnung":{"sachbearbeitung":"Sabine Wagner","freigabe":"Lars Becker"}}
                """;
        mvc.perform(post("").contentType(MediaType.APPLICATION_JSON).content(anlage)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ANTRAGSTELLER"))))
                .andExpect(status().isForbidden());
    }
}
