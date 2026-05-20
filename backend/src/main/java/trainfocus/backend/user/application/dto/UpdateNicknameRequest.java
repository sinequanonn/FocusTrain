package trainfocus.backend.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요.")
        @Pattern(regexp = "^[a-zA-z0-9가-힣_-]+$",
                message = "닉네임은 한글, 영문, 숫자, 언더스코어(_), 하이픈(-)만 사용 가능합니다."
        )
        String nickname
) {
}
