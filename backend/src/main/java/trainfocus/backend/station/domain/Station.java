package trainfocus.backend.station.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "stations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Station extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50, nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    private Station(String name, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Station createNewStation(String name, BigDecimal latitude, BigDecimal longitude) {
        return new Station(name, latitude, longitude);
    }
}
