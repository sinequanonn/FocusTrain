package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;

import java.time.LocalDateTime;

public record FocusSessionProgressResponse(
        Long sessionId,
        String status,
        Integer accumulatedSeconds,
        Integer remainingSeconds
) {
    public static FocusSessionProgressResponse from(FocusSession session, LocalDateTime now) {
        return new FocusSessionProgressResponse(
                session.getId(),
                session.getStatus().name(),
                session.accumulatedSeconds(now),
                session.remainingSeconds(now)
        );
    }

}
