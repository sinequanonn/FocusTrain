package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.station.application.dto.StationResponse;

import java.time.LocalDateTime;

public record FocusSessionDetailResponse(
        Long sessionId,
        String status,
        StationResponse departure,
        StationResponse arrival,
        Integer totalTargetSeconds,
        Integer accumulatedSeconds,
        Integer remainingSeconds,
        LocalDateTime startedAt,
        LocalDateTime plannedEndAt,
        LocalDateTime endedAt
) {
    public static FocusSessionDetailResponse from(FocusSession session, LocalDateTime now) {
        return new FocusSessionDetailResponse(
                session.getId(),
                session.getStatus().name(),
                StationResponse.from(session.getDepartureStation()),
                StationResponse.from(session.getArrivalStation()),
                session.totalTargetSeconds(),
                session.accumulatedSeconds(now),
                session.remainingSeconds(now),
                session.getStartedAt(),
                session.getPlannedEndAt(),
                session.getEndedAt()
        );
    }
}
