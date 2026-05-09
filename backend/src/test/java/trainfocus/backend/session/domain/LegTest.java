package trainfocus.backend.session.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LegTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 9, 10, 0, 0);

    @Test
    void start_시점에는_running_상태() {
        Leg leg = Leg.start(null, 1, START);

        assertThat(leg.isRunning()).isTrue();
        assertThat(leg.getStartedAt()).isEqualTo(START);
        assertThat(leg.getEndedAt()).isNull();
        assertThat(leg.getDurationSeconds()).isNull();
    }

    @Test
    void end_호출_시_종료시간과_지속시간_세팅() {
        Leg leg = Leg.start(null, 1, START);
        LocalDateTime end = START.plusSeconds(125);

        leg.end(end);

        assertThat(leg.isRunning()).isFalse();
        assertThat(leg.getEndedAt()).isEqualTo(end);
        assertThat(leg.getDurationSeconds()).isEqualTo(125);
    }

    @Test
    void elapsedSeconds_는_시작부터_now까지() {
        Leg leg = Leg.start(null, 1, START);

        int elapsed = leg.elapsedSeconds(START.plusSeconds(42));

        assertThat(elapsed).isEqualTo(42);
    }
}
