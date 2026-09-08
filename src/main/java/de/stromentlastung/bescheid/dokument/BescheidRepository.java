package de.stromentlastung.bescheid.dokument;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BescheidRepository extends JpaRepository<Bescheid, Long> {

    Optional<Bescheid> findByKennung(String kennung);

    long countByAktenzeichen(String aktenzeichen);

    List<Bescheid> findByAktenzeichenOrderByDatumAscIdAsc(String aktenzeichen);

    List<Bescheid> findByUnternehmenKennungOrderByDatumDescIdDesc(String unternehmenKennung);

    List<Bescheid> findByDienststelleOrderByDatumDescIdDesc(String dienststelle);

    List<Bescheid> findByPdfIsNull();
}
