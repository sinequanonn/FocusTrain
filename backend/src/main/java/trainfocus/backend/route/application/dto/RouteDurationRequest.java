package trainfocus.backend.route.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RouteDurationRequest(
        @NotNull @Positive Integer durationMinutes
) {
}
