package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.station.application.dto.StationResponse;

import java.time.LocalDateTime;
import java.util.List;

public record FocusSessionHistoryDetailResponse(
        SessionInfo session,
        List<LegResponse> legs
) {
    public static FocusSessionHistoryDetailResponse from(FocusSession session) {
        return new FocusSessionHistoryDetailResponse(
                SessionInfo.from(session),
                session.getLegs().stream()
                        .map(LegResponse::from)
                        .toList()
        );
    }

    public record SessionInfo(
            Long sessionId,
            String status,
            StationResponse departure,
            StationResponse arrival,
            Integer totalTargetSeconds,
            Integer totalFocusSeconds,
            LocalDateTime startedAt,
            LocalDateTime plannedEndAt,
            LocalDateTime endedAt
    ) {
        public static SessionInfo from(FocusSession session) {
            return new SessionInfo(
                    session.getId(),
                    session.getStatus().name(),
                    StationResponse.from(session.getDepartureStation()),
                    StationResponse.from(session.getArrivalStation()),
                    session.totalTargetSeconds(),
                    session.accumulatedSeconds(),
                    session.getStartedAt(),
                    session.getPlannedEndAt(),
                    session.getEndedAt()
            );
        }
    }
}
