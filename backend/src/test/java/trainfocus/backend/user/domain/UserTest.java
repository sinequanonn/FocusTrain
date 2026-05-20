package trainfocus.backend.user.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void 유저_생성_성공() {
        User user = User.createNewUser("uid-123", "test@example.com", "테스터");

        assertThat(user.getFirebaseUid()).isEqualTo("uid-123");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getNickname()).isEqualTo("테스터");
    }

    @Test
    void 닉네임_업데이트_성공() {
        User user = User.createNewUser("uid-1", "a@b.com", "기존닉네임");

        user.updateNickname("새닉네임");

        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }
}
