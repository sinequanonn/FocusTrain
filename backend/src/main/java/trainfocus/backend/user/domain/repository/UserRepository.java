package trainfocus.backend.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trainfocus.backend.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.departureStation WHERE u.firebaseUid = :firebaseUid")
    Optional<User> findByFirebaseUidWithDepartureStation(@Param("firebaseUid") String firebaseUid);

    boolean existsByNickname(String nickname);
}
