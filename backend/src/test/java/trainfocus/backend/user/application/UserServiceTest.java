package trainfocus.backend.user.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void 이미_가입된_유저는_저장_없이_반환() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        User existing = User.createNewUser("uid-1", "a@b.com", "이름");
        given(userRepository.findByFirebaseUid("uid-1")).willReturn(Optional.of(existing));

        User result = userService.findOrCreateUser(info);

        assertThat(result).isEqualTo(existing);
        then(userRepository).should(never()).save(any());
    }

    @Test
    void 신규_유저면_저장_후_반환() {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-2", "new@b.com", "신규");
        User saved = User.createNewUser("uid-2", "new@b.com", "신규");
        given(userRepository.findByFirebaseUid("uid-2")).willReturn(Optional.empty());
        given(userRepository.save(any())).willReturn(saved);

        User result = userService.findOrCreateUser(info);

        assertThat(result.getFirebaseUid()).isEqualTo("uid-2");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void firebaseUid로_유저_조회_성공() {
        User user = User.createNewUser("uid-1", "a@b.com", "이름");
        given(userRepository.findByFirebaseUid("uid-1")).willReturn(Optional.of(user));

        User result = userService.findByFirebaseUid("uid-1");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void firebaseUid로_유저_없으면_USER_NOT_FOUND_예외() {
        given(userRepository.findByFirebaseUid("uid-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByFirebaseUid("uid-999"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
