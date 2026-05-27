package trainfocus.backend.auth.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import trainfocus.backend.auth.application.dto.LoginRequest;
import trainfocus.backend.auth.application.dto.LoginResponse;
import trainfocus.backend.auth.application.dto.SignupRequest;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;
import trainfocus.backend.user.domain.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    FirebaseAuthClient firebaseAuthClient;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthService authService;

    @Test
    void 로그인_성공() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        User user = UserFixture.of(1L, "uid-1", "a@b.com", "이름");
        given(firebaseAuthClient.verifyToken("valid-token")).willReturn(info);
        given(userRepository.findByFirebaseUid("uid-1")).willReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest("valid-token"));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("a@b.com");
        assertThat(response.nickname()).isEqualTo("이름");
    }

    @Test
    void 로그인_시_가입되지_않은_유저면_USER_NOT_FOUND_예외() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-99", "x@y.com", "이름");
        given(firebaseAuthClient.verifyToken("valid-token")).willReturn(info);
        given(userRepository.findByFirebaseUid("uid-99")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("valid-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
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

    @Test
    void 회원가입_성공() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        SignupRequest request = new SignupRequest("새닉네임");
        given(userRepository.existsByFirebaseUid("uid-1")).willReturn(false);
        given(userRepository.existsByNickname("새닉네임")).willReturn(false);
        User saved = UserFixture.of(1L, "uid-1", "a@b.com", "새닉네임");
        given(userRepository.save(any(User.class))).willReturn(saved);

        LoginResponse response = authService.signUp(info, request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("a@b.com");
        assertThat(response.nickname()).isEqualTo("새닉네임");
    }

    @Test
    void 회원가입_시_이미_가입된_firebaseUid면_USER_ALREADY_REGISTERED_예외() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        given(userRepository.existsByFirebaseUid("uid-1")).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(info, new SignupRequest("새닉네임")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_ALREADY_REGISTERED));
        then(userRepository).should(never()).save(any());
    }

    @Test
    void 회원가입_시_닉네임_사전_중복이면_USER_NICKNAME_DUPLICATE_예외() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        given(userRepository.existsByFirebaseUid("uid-1")).willReturn(false);
        given(userRepository.existsByNickname("중복닉")).willReturn(true);

        assertThatThrownBy(() -> authService.signUp(info, new SignupRequest("중복닉")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NICKNAME_DUPLICATE));
        then(userRepository).should(never()).save(any());
    }

    @Test
    void 회원가입_시_save에서_DataIntegrityViolation이면_USER_NICKNAME_DUPLICATE_예외() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        given(userRepository.existsByFirebaseUid("uid-1")).willReturn(false);
        given(userRepository.existsByNickname("새닉네임")).willReturn(false);
        given(userRepository.save(any(User.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> authService.signUp(info, new SignupRequest("새닉네임")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NICKNAME_DUPLICATE));
    }
}
