package trainfocus.backend.session.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.session.domain.FocusSessionStatus;
import trainfocus.backend.user.domain.User;

import java.util.Collection;
import java.util.Optional;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    boolean existsByUserAndStatusIn(User user, Collection<FocusSessionStatus> status);

    Optional<FocusSession> findFirstByUserAndStatusIn(User user, Collection<FocusSessionStatus> statuses);
}
