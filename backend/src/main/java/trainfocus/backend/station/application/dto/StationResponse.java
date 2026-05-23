package trainfocus.backend.station.application.dto;

import trainfocus.backend.station.domain.Station;

import java.math.BigDecimal;

public record StationResponse(
        Long id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude

) {
    public static StationResponse from(Station station) {
        return new StationResponse(
                station.getId(),
                station.getName(),
                station.getLatitude(),
                station.getLongitude()
        );
    }
}
