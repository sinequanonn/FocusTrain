package trainfocus.backend.auth.application.dto;

import trainfocus.backend.user.domain.User;

import java.time.LocalDateTime;

public record MeResponse(
        Long userId,
        String email,
        String nickname,
        Long departureStationId,
        String departureStationName,
        LocalDateTime createdAt
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.hasDepartureStation() ? user.getDepartureStation().getId() : null,
                user.hasDepartureStation() ? user.getDepartureStation().getName() : null,
                user.getCreatedAt()
        );
    }
}
