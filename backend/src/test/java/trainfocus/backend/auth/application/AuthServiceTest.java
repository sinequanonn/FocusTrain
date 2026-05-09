package trainfocus.backend.auth.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trainfocus.backend.auth.application.dto.LoginRequest;
import trainfocus.backend.auth.application.dto.LoginResponse;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    FirebaseAuthClient firebaseAuthClient;

    @Mock
    UserService userService;

    @InjectMocks
    AuthService authService;

    @Test
    void 로그인_성공() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        User user = User.createNewUser("uid-1", "a@b.com", "이름");
        given(firebaseAuthClient.verifyToken("valid-token")).willReturn(info);
        given(userService.findOrCreateUser(info)).willReturn(user);

        LoginResponse response = authService.login(new LoginRequest("valid-token"));

        assertThat(response.email()).isEqualTo("a@b.com");
        assertThat(response.nickname()).isEqualTo("이름");
    }

    @Test
    void Firebase_검증_실패시_예외_전파() {
        given(firebaseAuthClient.verifyToken("bad-token"))
                .willThrow(new BusinessException(ErrorCode.AUTH_TOKEN_INVALID));

        assertThatThrownBy(() -> authService.login(new LoginRequest("bad-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.AUTH_TOKEN_INVALID));
    }
}
