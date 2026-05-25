package trainfocus.backend.session.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trainfocus.backend.session.domain.Leg;

import java.time.LocalDateTime;

public interface LegRepository extends JpaRepository<Leg, Long> {

    @Query("""
            SELECT COALESCE(SUM(l.durationSeconds), 0)
            FROM Leg l
            WHERE l.endedAt >= :since
              AND l.durationSeconds IS NOT NULL
            """)
    long sumDurationSecondsSince(@Param("since") LocalDateTime since);
}
