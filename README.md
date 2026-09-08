# stromentlastung-bescheid – Bescheide

Teil des Referenzverfahrens Stromentlastung (§ 9b StromStG); Fachkonzept und Architektur liegen im Repository `stromentlastung-platform`.

Entlastungs-, Ablehnungs- und Änderungsbescheide aus versionierten Textbausteinen als PDF (Fachkonzept Kap. 8), Bekanntgabe am vierten Tag nach Absendung mit Werktagsverschiebung, Fälligkeit des Leistungsgebots, Postfach des Unternehmens. Rechtsgrundlagen-Zitate kommen als Daten aus dem Rechenkern des Vorgangs; die Saat-Bescheide werden beim Start gerendert.

## Technik

Spring Boot 3.4.5 auf Java 17, H2 mit Flyway (`V1__schema.sql`, `V2__saat.sql`), Resource Server gegen den Keycloak-Realm `stromentlastung`, OpenAPI unter `/api/bescheide/v3/api-docs`. Regelverletzungen antworten mit 409 und `{"code": "<REGELCODE>", "message": "<Diagnose>"}`; die Oberfläche übersetzt den Code. Beträge in Cent, Mengen in Kilowattstunden, Datumsangaben ohne Zeitanteil.

## Bauen und starten

```bash
./mvnw test                      # Kern ohne Datenbank und Uhr, Endpunkte mit nachgestelltem JWT, Migrationen gegen H2 im Speicher
./mvnw spring-boot:run           # Port 8093, Kontextpfad /api/bescheide, H2-Datei unter ./data
```

Das „Heute“ des Dienstes lässt sich mit `STROMENTLASTUNG_HEUTE=2026-09-08` einfrieren (Fachkonzept Kap. 10). Im Containerbetrieb zeigen `STROMENTLASTUNG_ISSUER` und `STROMENTLASTUNG_JWKS_URI` auf den öffentlichen Aussteller und den internen Schlüsselabruf.
