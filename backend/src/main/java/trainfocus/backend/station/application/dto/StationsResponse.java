package trainfocus.backend.station.application.dto;

import trainfocus.backend.station.domain.Station;

import java.util.List;

public record StationsResponse(
        List<StationResponse> stations
) {
    public static StationsResponse from(List<Station> stations) {
        return new StationsResponse(
                stations.stream()
                        .map(StationResponse::from)
                        .toList()
        );
    }
}
