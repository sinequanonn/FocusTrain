package trainfocus.backend.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trainfocus.backend.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    boolean existsByNickname(String nickname);
}
