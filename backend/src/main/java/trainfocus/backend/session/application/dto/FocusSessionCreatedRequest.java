package trainfocus.backend.session.application.dto;

import jakarta.validation.constraints.NotNull;

public record FocusSessionCreatedRequest(
        @NotNull Long departureStationId,
        @NotNull Long arrivalStationId,
        Integer delayMinutes
) {
    public FocusSessionCreatedRequest {
        if (delayMinutes == null) {
            delayMinutes = 0;
        }
    }
}
