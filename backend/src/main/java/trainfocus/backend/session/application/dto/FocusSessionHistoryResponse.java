package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.station.application.dto.StationResponse;

import java.time.LocalDateTime;

public record FocusSessionHistoryResponse(
        Long sessionId,
        String status,
        StationResponse departure,
        StationResponse arrival,
        Integer totalFocusSeconds,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static FocusSessionHistoryResponse from(FocusSession session) {
        return new FocusSessionHistoryResponse(
                session.getId(),
                session.getStatus().name(),
                StationResponse.from(session.getDepartureStation()),
                StationResponse.from(session.getArrivalStation()),
                session.accumulatedSeconds(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
