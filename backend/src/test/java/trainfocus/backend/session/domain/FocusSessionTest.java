package trainfocus.backend.session.domain;

import org.junit.jupiter.api.Test;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FocusSessionTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 9, 10, 0, 0);

    private final User user = UserFixture.withId(1L);
    private final Station departure = StationFixture.of(10L, "강남");
    private final Station arrival = StationFixture.of(20L, "서울역");

    @Test
    void create_성공_시_RUNNING_상태와_첫_leg_생성() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 5, START);

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.RUNNING);
        assertThat(session.getBaseDurationMinutes()).isEqualTo(30);
        assertThat(session.getDelayMinutes()).isEqualTo(5);
        assertThat(session.getTotalTargetMinutes()).isEqualTo(35);
        assertThat(session.getStartedAt()).isEqualTo(START);
        assertThat(session.getPlannedEndAt()).isEqualTo(START.plusMinutes(35));
        assertThat(session.getLegs()).hasSize(1);
        assertThat(session.getLegs().get(0).getLegNumber()).isEqualTo(1);
        assertThat(session.getLegs().get(0).isRunning()).isTrue();
    }

    @Test
    void create_시_출발역과_도착역이_같으면_예외() {
        Station same = StationFixture.of(10L, "강남");

        assertThatThrownBy(() -> FocusSession.createNewFocusSession(user, same, same, 30, 0, START))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ROUTE_SAME_STATION));
    }

    @Test
    void create_시_지연시간이_음수면_예외() {
        assertThatThrownBy(() -> FocusSession.createNewFocusSession(user, departure, arrival, 30, -1, START))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_DELAY_NEGATIVE));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {0, -1, -10})
    void create_시_baseDurationMinutes가_0이하면_INVALID_PARAMETER(int invalid) {
        assertThatThrownBy(() -> FocusSession.createNewFocusSession(user, departure, arrival, invalid, 0, START))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COMMON_INVALID_PARAMETER));
    }

    @Test
    void create_시_persist_전_동명_역은_검증_스킵() {
        // id가 둘 다 null이면 같은 역인지 판단할 수 없으므로 검증 스킵.
        // 실서비스 흐름은 항상 영속화된 Station을 받으므로 이 경로는 방어적 케이스.
        Station a = trainfocus.backend.station.domain.StationFixture.withName("강남");
        Station b = trainfocus.backend.station.domain.StationFixture.withName("강남");

        FocusSession session = FocusSession.createNewFocusSession(user, a, b, 30, 0, START);
        assertThat(session).isNotNull();
    }

    @Test
    void pause_시_현재_leg_종료되고_PAUSED_상태() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);

        session.pause(START.plusSeconds(60));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.PAUSED);
        Leg first = session.getLegs().get(0);
        assertThat(first.isRunning()).isFalse();
        assertThat(first.getDurationSeconds()).isEqualTo(60);
    }

    @Test
    void 이미_PAUSED인데_pause_호출_시_예외() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);
        session.pause(START.plusSeconds(60));

        assertThatThrownBy(() -> session.pause(START.plusSeconds(70)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_PAUSED));
    }

    @Test
    void 종료된_세션은_pause_불가() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.complete(START.plusSeconds(60));

        assertThatThrownBy(() -> session.pause(START.plusSeconds(70)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_ENDED));
    }

    @Test
    void resume_시_새_leg_추가되고_RUNNING_상태() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);
        session.pause(START.plusSeconds(60));

        session.resume(START.plusSeconds(120));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.RUNNING);
        assertThat(session.getLegs()).hasSize(2);
        Leg second = session.getLegs().get(1);
        assertThat(second.getLegNumber()).isEqualTo(2);
        assertThat(second.isRunning()).isTrue();
        assertThat(second.getStartedAt()).isEqualTo(START.plusSeconds(120));
    }

    @Test
    void 이미_RUNNING인데_resume_호출_시_예외() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);

        assertThatThrownBy(() -> session.resume(START.plusSeconds(10)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_RUNNING));
    }

    @Test
    void 종료된_세션은_resume_불가() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.complete(START.plusSeconds(60));

        assertThatThrownBy(() -> session.resume(START.plusSeconds(70)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_ENDED));
    }

    @Test
    void complete_시_RUNNING_leg는_종료되고_COMPLETED_상태() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        LocalDateTime now = START.plusSeconds(60);

        session.complete(now);

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(now);
        assertThat(session.getLegs().get(0).isRunning()).isFalse();
        assertThat(session.accumulatedSeconds()).isEqualTo(60);
    }

    @Test
    void complete_시_PAUSED_상태에서도_누적시간_충족하면_정상_종료() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.pause(START.plusSeconds(60));

        session.complete(START.plusSeconds(120));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
    }

    @Test
    void 누적_시간_미달_시_complete_불가() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);

        assertThatThrownBy(() -> session.complete(START.plusSeconds(30)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_TARGET_NOT_REACHED));
    }

    @Test
    void 종료된_세션은_complete_불가() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.abort(START.plusSeconds(30));

        assertThatThrownBy(() -> session.complete(START.plusSeconds(40)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_ENDED));
    }

    @Test
    void abort_시_RUNNING_leg는_종료되고_ABORTED_상태() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);
        LocalDateTime now = START.plusSeconds(45);

        session.abort(now);

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.ABORTED);
        assertThat(session.getEndedAt()).isEqualTo(now);
        assertThat(session.getLegs().get(0).isRunning()).isFalse();
    }

    @Test
    void abort_는_누적시간_미달이어도_정상_처리() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 60, 0, START);

        session.abort(START.plusSeconds(10));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.ABORTED);
    }

    @Test
    void 종료된_세션은_abort_불가() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.complete(START.plusSeconds(60));

        assertThatThrownBy(() -> session.abort(START.plusSeconds(70)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_ENDED));
    }

    @Test
    void totalTargetSeconds는_분단위를_초로_변환() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 5, START);

        assertThat(session.totalTargetSeconds()).isEqualTo(35 * 60);
    }

    @Test
    void accumulatedSeconds_now_는_running_leg_경과시간을_포함() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);

        int total = session.accumulatedSeconds(START.plusSeconds(40));

        assertThat(total).isEqualTo(40);
    }

    @Test
    void accumulatedSeconds_now_는_pause된_leg와_running_leg를_합산() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);
        session.pause(START.plusSeconds(60));   // leg1: 60초 종료
        session.resume(START.plusSeconds(120)); // leg2: 120초부터 시작

        int total = session.accumulatedSeconds(START.plusSeconds(180)); // leg2 = 60초

        assertThat(total).isEqualTo(120);
    }

    @Test
    void accumulatedSeconds_종료된_leg만_합산() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);
        session.pause(START.plusSeconds(60));

        assertThat(session.accumulatedSeconds()).isEqualTo(60);

        session.resume(START.plusSeconds(120));

        // 두 번째 leg는 아직 RUNNING이라 제외
        assertThat(session.accumulatedSeconds()).isEqualTo(60);
    }

    @Test
    void remainingSeconds_는_누적이_목표_초과하면_0() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);

        int remaining = session.remainingSeconds(START.plusSeconds(120));

        assertThat(remaining).isZero();
    }

    @Test
    void remainingSeconds_는_목표에서_누적을_뺀_값() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 2, 0, START); // 120초 목표

        int remaining = session.remainingSeconds(START.plusSeconds(30));

        assertThat(remaining).isEqualTo(90);
    }

    @Test
    void isOwnedBy_는_user_id_기준_비교() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);

        assertThat(session.isOwnedBy(UserFixture.withId(1L))).isTrue();
        assertThat(session.isOwnedBy(UserFixture.withId(2L))).isFalse();
    }

    @Test
    void getLegs는_변경불가_리스트_반환() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, START);

        assertThatThrownBy(() -> session.getLegs().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void autoCompleteIfTargetReached_RUNNING이고_누적이_목표_도달_시_정확한_도달시점으로_완료() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        LocalDateTime now = START.plusSeconds(300);

        session.autoCompleteIfTargetReached(now);

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(START.plusSeconds(60));
        assertThat(session.getLegs().get(0).isRunning()).isFalse();
        assertThat(session.getLegs().get(0).getDurationSeconds()).isEqualTo(60);
    }

    @Test
    void autoCompleteIfTargetReached_누적이_목표_미달이면_변화_없음() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);

        session.autoCompleteIfTargetReached(START.plusSeconds(30));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.RUNNING);
        assertThat(session.getEndedAt()).isNull();
        assertThat(session.getLegs().get(0).isRunning()).isTrue();
    }

    @Test
    void autoCompleteIfTargetReached_PAUSED는_자동완료_대상에서_제외() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.pause(START.plusSeconds(120));

        session.autoCompleteIfTargetReached(START.plusSeconds(180));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.PAUSED);
        assertThat(session.getEndedAt()).isNull();
    }

    @Test
    void autoCompleteIfTargetReached_이미_COMPLETED면_변화_없음() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        LocalDateTime completedAt = START.plusSeconds(70);
        session.complete(completedAt);

        session.autoCompleteIfTargetReached(START.plusSeconds(1000));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(completedAt);
    }

    @Test
    void autoCompleteIfTargetReached_pause_resume_후_누적이_목표_도달하면_정확한_시점으로_완료() {
        // 목표 120s. leg1: 60s (pause). 1시간 뒤 resume → leg2가 60s 채우면 목표 도달.
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 2, 0, START);
        session.pause(START.plusSeconds(60));
        LocalDateTime resumeAt = START.plusSeconds(3600);
        session.resume(resumeAt);

        session.autoCompleteIfTargetReached(resumeAt.plusSeconds(120)); // 충분히 지난 시점

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(resumeAt.plusSeconds(60));
    }

    @Test
    void autoCompleteIfTargetReached_브라우저_종료_24시간_뒤_조회_시_초과누적_방지() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);

        session.autoCompleteIfTargetReached(START.plusDays(1));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(START.plusSeconds(60));
        assertThat(session.accumulatedSeconds()).isEqualTo(60); // 86400이 아닌 60
    }

    @Test
    void autoCompleteIfTargetReached_종료된_leg_합이_이미_목표_초과한_채_RUNNING이면_currentLeg_시작시점으로_완료() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, START);
        session.pause(START.plusSeconds(120));
        LocalDateTime resumeAt = START.plusSeconds(200);
        session.resume(resumeAt);

        session.autoCompleteIfTargetReached(resumeAt.plusSeconds(10));

        assertThat(session.getStatus()).isEqualTo(FocusSessionStatus.COMPLETED);
        assertThat(session.getEndedAt()).isEqualTo(resumeAt);
        assertThat(session.getLegs().get(1).getDurationSeconds()).isZero();
    }
}
