package trainfocus.backend.session.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "session_legs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_legs_session_number",
                columnNames = {"session_id", "leg_number"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leg extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private FocusSession session;

    @Column(nullable = false)
    private Integer legNumber;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Integer durationSeconds;

    public static Leg start(FocusSession session, int legNumber, LocalDateTime startedAt) {
        return new Leg(session, legNumber, startedAt);
    }

    private Leg(FocusSession session, Integer legNumber, LocalDateTime startedAt) {
        this.session = session;
        this.legNumber = legNumber;
        this.startedAt = startedAt;
    }

    public void end(LocalDateTime endedAt) {
        this.endedAt = endedAt;
        this.durationSeconds = (int) Duration.between(this.startedAt, endedAt).getSeconds();
    }

    public boolean isRunning() {
        return this.endedAt == null;
    }

    public int elapsedSeconds(LocalDateTime now) {
        return (int) Duration.between(this.startedAt, now).getSeconds();
    }
}
