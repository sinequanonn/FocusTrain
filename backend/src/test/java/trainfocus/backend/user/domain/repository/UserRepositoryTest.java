package trainfocus.backend.user.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import trainfocus.backend.common.config.JpaConfig;
import trainfocus.backend.user.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    UserRepository userRepository;

    @Test
    void firebaseUid로_유저_조회_성공() {
        // given
        User user = User.createNewUser("uid-1", "a@b.com", "이름");
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByFirebaseUid("uid-1");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("a@b.com");
        assertThat(found.get().getNickname()).isEqualTo("이름");
    }

    @Test
    void 없는_uid면_Optional_empty_반환() {
        // given & then
        Optional<User> found = userRepository.findByFirebaseUid("uid-없음");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 유저_저장_시_생성일_수정일_자동_설정() {
        // given
        User user = User.createNewUser("uid-2", "b@b.com", "테스터");

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
