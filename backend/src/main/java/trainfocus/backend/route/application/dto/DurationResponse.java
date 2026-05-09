package trainfocus.backend.route.application.dto;

import trainfocus.backend.route.domain.Route;

public record DurationResponse(
        Long departureStationId,
        Long arrivalStationId,
        Integer durationMinutes
) {
    public static DurationResponse from(Route route) {
        return new DurationResponse(
                route.getDepartureStation().getId(),
                route.getArrivalStation().getId(),
                route.getDurationMinutes()
        );
    }
}
