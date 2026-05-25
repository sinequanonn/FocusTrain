package trainfocus.backend.station.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "stations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Station extends BaseEntity {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("33.0");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("39.0");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("124.0");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("131.0");

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
        validateCoordinate(latitude, longitude);
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Station createNewStation(String name, BigDecimal latitude, BigDecimal longitude) {
        return new Station(name, latitude, longitude);
    }

    public void update(String name, BigDecimal latitude, BigDecimal longitude) {
        validateCoordinate(latitude, longitude);
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void validateCoordinate(BigDecimal latitude, BigDecimal longitude) {
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0
                || longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new BusinessException(ErrorCode.STATION_COORDINATE_OUT_OF_RANGE);
        }
    }
}
