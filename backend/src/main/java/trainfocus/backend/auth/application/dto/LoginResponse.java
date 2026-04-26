package trainfocus.backend.auth.application.dto;

import trainfocus.backend.user.domain.User;

public record LoginResponse(
        Long userId,
        String email,
        String nickname
) {
    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname());
    }
}
