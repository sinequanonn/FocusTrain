package trainfocus.backend.user.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateDepartureStationRequest(
        @NotNull(message = "출발역 ID는 필수입니다.")
        Long stationId
) {
}
