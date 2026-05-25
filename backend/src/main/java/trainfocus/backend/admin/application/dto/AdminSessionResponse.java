package trainfocus.backend.admin.application.dto;

import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.session.domain.FocusSessionStatus;
import trainfocus.backend.user.domain.User;

import java.time.LocalDateTime;

public record AdminSessionResponse(
        Long id,
        Long userId,
        String userNickname,
        String userEmail,
        String departureStationName,
        String arrivalStationName,
        FocusSessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime plannedEndAt,
        Integer totalTargetMinutes
) {
    public static AdminSessionResponse from(FocusSession session) {
        User user = session.getUser();
        return new AdminSessionResponse(
                session.getId(),
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                session.getDepartureStation().getName(),
                session.getArrivalStation().getName(),
                session.getStatus(),
                session.getStartedAt(),
                session.getPlannedEndAt(),
                session.getTotalTargetMinutes()
        );
    }
}
