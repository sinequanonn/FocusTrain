package trainfocus.backend.route.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trainfocus.backend.route.domain.Route;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findRouteByDepartureStationIdAndArrivalStationId(
            Long departureStation_id,
            Long arrivalStation_id);

    boolean existsByDepartureStationIdAndArrivalStationId(
            Long departureStation_id,
            Long arrivalStation_id);

    @Query(value = """
            SELECT r FROM Route r
            JOIN FETCH r.departureStation d
            JOIN FETCH r.arrivalStation a
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR d.name LIKE CONCAT('%', :keyword, '%')
                   OR a.name LIKE CONCAT('%', :keyword, '%'))
            ORDER BY d.name ASC, a.name ASC
            """,
            countQuery = """
            SELECT COUNT(r) FROM Route r
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR r.departureStation.name LIKE CONCAT('%', :keyword, '%')
                   OR r.arrivalStation.name LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<Route> findAllForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
