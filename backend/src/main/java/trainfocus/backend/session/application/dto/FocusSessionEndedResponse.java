package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;

import java.time.LocalDateTime;

public record FocusSessionEndedResponse(
        Long sessionId,
        String status,
        Integer totalFocusSeconds,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Long newDepartureStationId,
        String newDepartureStationName
) {
    public static FocusSessionEndedResponse from(FocusSession session) {
        return new FocusSessionEndedResponse(
                session.getId(),
                session.getStatus().name(),
                session.accumulatedSeconds(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getArrivalStation().getId(),
                session.getArrivalStation().getName()
        );
    }
}
