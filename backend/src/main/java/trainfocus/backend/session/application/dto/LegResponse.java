package trainfocus.backend.session.application.dto;

import trainfocus.backend.session.domain.Leg;

import java.time.LocalDateTime;

public record LegResponse(
        Integer legNumber,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer durationSeconds
) {
    public static LegResponse from(Leg leg) {
        return new LegResponse(
                leg.getLegNumber(),
                leg.getStartedAt(),
                leg.getEndedAt(),
                leg.getDurationSeconds()
        );
    }
}
