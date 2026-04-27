package trainfocus.backend.route.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trainfocus.backend.route.domain.Route;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findRouteByDepartureStationIdAndArrivalStationId(
            Long departureStation_id,
            Long arrivalStation_id);
}
