package trainfocus.backend.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "ID Token은 필수입니다.")
        String idToken
) {
}
