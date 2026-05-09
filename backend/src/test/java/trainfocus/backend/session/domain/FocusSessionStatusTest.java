package trainfocus.backend.session.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class FocusSessionStatusTest {

    @ParameterizedTest
    @EnumSource(value = FocusSessionStatus.class, names = {"COMPLETED", "ABORTED"})
    void 종료_상태는_isEnded_true(FocusSessionStatus status) {
        assertThat(status.isEnded()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = FocusSessionStatus.class, names = {"RUNNING", "PAUSED"})
    void 진행_또는_일시정지는_isEnded_false(FocusSessionStatus status) {
        assertThat(status.isEnded()).isFalse();
    }
}
