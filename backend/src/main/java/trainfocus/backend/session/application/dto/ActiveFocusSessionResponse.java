package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.FocusSession;

import java.time.LocalDateTime;

public record ActiveFocusSessionResponse(
        Boolean hasActiveSession,
        FocusSessionDetailResponse session
) {
    // TODO: null로 반환하는 게 적절할까?
    public static ActiveFocusSessionResponse empty() {
        return new ActiveFocusSessionResponse(false, null);
    }

    public static ActiveFocusSessionResponse of(FocusSession session, LocalDateTime now) {
        return new ActiveFocusSessionResponse(
                true,
                FocusSessionDetailResponse.from(session, now)
        );
    }
}
