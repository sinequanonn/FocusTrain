package trainfocus.backend.route.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.domain.Station;

@Getter
@Entity
@Table(
        name = "routes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_routes_dep_arr",
                columnNames = {"departure_station_id", "arrival_station_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_station_id", nullable = false)
    private Station departureStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_station_id", nullable = false)
    private Station arrivalStation;

    @Column(nullable = false)
    private Integer durationMinutes;

    private Route(Station departureStation, Station arrivalStation, Integer durationMinutes) {
        if (departureStation.getId() != null
                && departureStation.getId().equals(arrivalStation.getId())) {
            throw new BusinessException(ErrorCode.ROUTE_SAME_STATION);
        }
        validateDurationMinutes(durationMinutes);
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.durationMinutes = durationMinutes;
    }

    public static Route createNewRoute(Station departureStation, Station arrivalStation, Integer durationMinutes) {
        return new Route(departureStation, arrivalStation, durationMinutes);
    }

    public void updateDurationMinutes(Integer durationMinutes) {
        validateDurationMinutes(durationMinutes);
        this.durationMinutes = durationMinutes;
    }

    private void validateDurationMinutes(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new BusinessException(ErrorCode.ROUTE_DURATION_NOT_POSITIVE);
        }
    }
}
