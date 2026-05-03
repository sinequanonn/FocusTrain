package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.station.application.dto.StationResponse;

import java.time.LocalDateTime;

public record FocusSessionCreatedResponse(
        Long sessionId,
        String status,
        StationResponse departure,
        StationResponse arrival,
        Integer baseDurationMinutes,
        Integer delayMinutes,
        Integer totalTargetMinutes,
        LocalDateTime startedAt,
        LocalDateTime plannedEndAt
) {
    public static FocusSessionCreatedResponse from(FocusSession session) {
        return new FocusSessionCreatedResponse(
                session.getId(),
                session.getStatus().name(),
                StationResponse.from(session.getDepartureStation()),
                StationResponse.from(session.getArrivalStation()),
                session.getBaseDurationMinutes(),
                session.getDelayMinutes(),
                session.getTotalTargetMinutes(),
                session.getStartedAt(),
                session.getPlannedEndAt()
        );
    }
}
