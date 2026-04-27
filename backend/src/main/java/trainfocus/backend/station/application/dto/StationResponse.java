package trainfocus.backend.station.application.dto;

import trainfocus.backend.station.domain.Station;

public record StationResponse(
        Long id,
        String name
) {
    public static StationResponse from(Station station) {
        return new StationResponse(station.getId(), station.getName());
    }
}
