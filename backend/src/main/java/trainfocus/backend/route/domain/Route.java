package trainfocus.backend.route.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;
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
}
