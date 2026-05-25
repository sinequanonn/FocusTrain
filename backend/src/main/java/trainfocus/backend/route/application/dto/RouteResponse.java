package trainfocus.backend.route.application.dto;

import trainfocus.backend.route.domain.Route;

public record RouteResponse(
        Long id,
        Long departureStationId,
        String departureStationName,
        Long arrivalStationId,
        String arrivalStationName,
        Integer durationMinutes
) {
    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getDepartureStation().getId(),
                route.getDepartureStation().getName(),
                route.getArrivalStation().getId(),
                route.getArrivalStation().getName(),
                route.getDurationMinutes()
        );
    }
}
