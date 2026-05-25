package trainfocus.backend.route.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RouteRequest(
        @NotNull Long departureStationId,
        @NotNull Long arrivalStationId,
        @NotNull @Positive Integer durationMinutes,
        Boolean bidirectional
) {
}
