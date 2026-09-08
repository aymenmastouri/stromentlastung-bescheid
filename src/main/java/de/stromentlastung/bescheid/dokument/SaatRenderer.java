package de.stromentlastung.bescheid.dokument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Rendert beim Start die PDF-Dateien der gesäten Bescheide (Architektur A-05). */
@Component
public class SaatRenderer {

    private static final Logger log = LoggerFactory.getLogger(SaatRenderer.class);

    private final BescheidService bescheide;

    public SaatRenderer(BescheidService bescheide) {
        this.bescheide = bescheide;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void render() {
        int anzahl = bescheide.renderFehlende();
        if (anzahl > 0) {
            log.info("{} Saat-Bescheide gerendert", anzahl);
        }
    }
}
