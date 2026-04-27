package trainfocus.backend.station.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trainfocus.backend.station.domain.Station;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findAllByOrderByNameAsc();
}
