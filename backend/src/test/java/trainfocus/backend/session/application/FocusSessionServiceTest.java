package trainfocus.backend.session.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;
import trainfocus.backend.station.domain.repository.StationRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.RouteFixture;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.session.application.dto.*;
import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.session.domain.FocusSessionStatus;
import trainfocus.backend.session.domain.repository.FocusSessionRepository;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FocusSessionServiceTest {

    @Mock
    FocusSessionRepository focusSessionRepository;
    @Mock
    StationRepository stationRepository;
    @Mock
    RouteRepository routeRepository;

    @org.mockito.InjectMocks
    FocusSessionService focusSessionService;

    private User user;
    private Station departure;
    private Station arrival;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        user = UserFixture.withId(1L);
        departure = StationFixture.of(10L, "강남");
        arrival = StationFixture.of(20L, "서울역");
    }

    // ===================== create =====================

    @Test
    void create_성공() {
        // given
        Route route = RouteFixture.of(departure, arrival, 30);
        FocusSessionCreatedRequest request = new FocusSessionCreatedRequest(10L, 20L, 5);

        given(focusSessionRepository.existsByUserAndStatusIn(any(), any())).willReturn(false);
        given(stationRepository.findById(10L)).willReturn(Optional.of(departure));
        given(stationRepository.findById(20L)).willReturn(Optional.of(arrival));
        given(routeRepository.findRouteByDepartureStationIdAndArrivalStationId(10L, 20L))
                .willReturn(Optional.of(route));
        given(focusSessionRepository.save(any(FocusSession.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        FocusSessionCreatedResponse response = focusSessionService.create(user, request);

        // then
        assertThat(response.status()).isEqualTo("RUNNING");
        assertThat(response.baseDurationMinutes()).isEqualTo(30);
        assertThat(response.delayMinutes()).isEqualTo(5);
        assertThat(response.totalTargetMinutes()).isEqualTo(35);
        assertThat(response.departure().id()).isEqualTo(10L);
        assertThat(response.arrival().id()).isEqualTo(20L);
    }

    @Test
    void create_시_이미_활성_세션이_있으면_예외() {
        FocusSessionCreatedRequest request = new FocusSessionCreatedRequest(10L, 20L, 0);
        given(focusSessionRepository.existsByUserAndStatusIn(any(), any())).willReturn(true);

        assertThatThrownBy(() -> focusSessionService.create(user, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_ALREADY_ACTIVE));
    }

    @Test
    void create_시_출발역이_없으면_STATION_NOT_FOUND() {
        FocusSessionCreatedRequest request = new FocusSessionCreatedRequest(10L, 20L, 0);
        given(focusSessionRepository.existsByUserAndStatusIn(any(), any())).willReturn(false);
        given(stationRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> focusSessionService.create(user, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.STATION_NOT_FOUND));
    }

    @Test
    void create_시_노선이_없으면_ROUTE_NOT_FOUND() {
        FocusSessionCreatedRequest request = new FocusSessionCreatedRequest(10L, 20L, 0);
        given(focusSessionRepository.existsByUserAndStatusIn(any(), any())).willReturn(false);
        given(stationRepository.findById(10L)).willReturn(Optional.of(departure));
        given(stationRepository.findById(20L)).willReturn(Optional.of(arrival));
        given(routeRepository.findRouteByDepartureStationIdAndArrivalStationId(10L, 20L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> focusSessionService.create(user, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ROUTE_NOT_FOUND));
    }

    // ===================== pause / resume / complete / abort =====================

    @Test
    void pause_성공() {
        FocusSession session = createRunningSession();
        given(focusSessionRepository.findById(100L)).willReturn(Optional.of(session));

        FocusSessionProgressResponse response = focusSessionService.pause(user, 100L);

        assertThat(response.status()).isEqualTo("PAUSED");
    }

    @Test
    void resume_성공() {
        FocusSession session = createRunningSession();
        session.pause(LocalDateTime.now().plusSeconds(10));
        given(focusSessionRepository.findById(100L)).willReturn(Optional.of(session));

        FocusSessionProgressResponse response = focusSessionService.resume(user, 100L);

        assertThat(response.status()).isEqualTo("RUNNING");
    }

    @Test
    void complete_성공() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0,
                LocalDateTime.now().minusMinutes(5));
        setSessionId(session, 100L);
        given(focusSessionRepository.findById(100L)).willReturn(Optional.of(session));

        FocusSessionEndedResponse response = focusSessionService.complete(user, 100L);

        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void abort_성공() {
        FocusSession session = createRunningSession();
        given(focusSessionRepository.findById(100L)).willReturn(Optional.of(session));

        FocusSessionEndedResponse response = focusSessionService.abort(user, 100L);

        assertThat(response.status()).isEqualTo("ABORTED");
    }

    @Test
    void 세션이_없으면_SESSION_NOT_FOUND() {
        given(focusSessionRepository.findById(100L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> focusSessionService.pause(user, 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    void 타인의_세션_조작_시_SESSION_FORBIDDEN() {
        FocusSession session = createRunningSession();
        User other = UserFixture.withId(999L);
        given(focusSessionRepository.findById(100L)).willReturn(Optional.of(session));

        assertThatThrownBy(() -> focusSessionService.pause(other, 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_FORBIDDEN));
    }

    // ===================== findById / findActive =====================

    @Test
    void findById_성공() {
        FocusSession session = createRunningSession();
        given(focusSessionRepository.findById(100L)).willReturn(Optional.of(session));

        FocusSessionDetailResponse response = focusSessionService.findById(user, 100L);

        assertThat(response.status()).isEqualTo("RUNNING");
        assertThat(response.departure().id()).isEqualTo(10L);
    }

    @Test
    void findActive_활성_세션이_있으면_hasActiveSession_true() {
        FocusSession session = createRunningSession();
        given(focusSessionRepository.findFirstByUserAndStatusIn(any(), any()))
                .willReturn(Optional.of(session));

        ActiveFocusSessionResponse response = focusSessionService.findActive(user);

        assertThat(response.hasActiveSession()).isTrue();
        assertThat(response.session()).isNotNull();
    }

    @Test
    void findActive_활성_세션이_없으면_empty() {
        given(focusSessionRepository.findFirstByUserAndStatusIn(any(), any()))
                .willReturn(Optional.empty());

        ActiveFocusSessionResponse response = focusSessionService.findActive(user);

        assertThat(response.hasActiveSession()).isFalse();
        assertThat(response.session()).isNull();
    }

    // ===================== findHistory =====================

    @Test
    void findHistory_status_null이면_종료된_상태들로_조회() {
        FocusSession s = createCompletedSession();
        Page<FocusSession> page = new PageImpl<>(List.of(s), PageRequest.of(0, 20), 1);
        given(focusSessionRepository.findByUserAndStatusIn(any(), any(), any(Pageable.class)))
                .willReturn(page);

        FocusSessionHistoryPageResponse response = focusSessionService.findHistory(user, 0, 20, null);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void findHistory_COMPLETED_필터_조회() {
        Page<FocusSession> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(focusSessionRepository.findByUserAndStatusIn(any(), any(), any(Pageable.class)))
                .willReturn(page);

        FocusSessionHistoryPageResponse response =
                focusSessionService.findHistory(user, 0, 20, FocusSessionStatus.COMPLETED);

        assertThat(response.totalElements()).isZero();
    }

    @Test
    void findHistory_RUNNING_상태_필터링은_INVALID_PARAMETER() {
        assertThatThrownBy(() ->
                focusSessionService.findHistory(user, 0, 20, FocusSessionStatus.RUNNING))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COMMON_INVALID_PARAMETER));
    }

    @Test
    void findHistory_size는_MAX_PAGE_SIZE_초과_시_clamp() {
        Page<FocusSession> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        given(focusSessionRepository.findByUserAndStatusIn(any(), any(), any(Pageable.class)))
                .willReturn(page);

        FocusSessionHistoryPageResponse response =
                focusSessionService.findHistory(user, 0, 999, null);

        assertThat(response.size()).isEqualTo(100);
    }

    // ===================== findDetail =====================

    @Test
    void findDetail_성공() {
        FocusSession session = createCompletedSession();
        given(focusSessionRepository.findDetailById(100L)).willReturn(Optional.of(session));

        FocusSessionHistoryDetailResponse response = focusSessionService.findDetail(user, 100L);

        assertThat(response.session().sessionId()).isEqualTo(100L);
        assertThat(response.legs()).isNotEmpty();
    }

    @Test
    void findDetail_세션없으면_SESSION_NOT_FOUND() {
        given(focusSessionRepository.findDetailById(100L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> focusSessionService.findDetail(user, 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    void findDetail_타인_세션이면_SESSION_FORBIDDEN() {
        FocusSession session = createCompletedSession();
        User other = UserFixture.withId(999L);
        given(focusSessionRepository.findDetailById(100L)).willReturn(Optional.of(session));

        assertThatThrownBy(() -> focusSessionService.findDetail(other, 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_FORBIDDEN));
    }

    // ===================== helpers =====================

    private FocusSession createRunningSession() {
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 30, 0, LocalDateTime.now());
        setSessionId(session, 100L);
        return session;
    }

    private FocusSession createCompletedSession() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        FocusSession session = FocusSession.createNewFocusSession(user, departure, arrival, 1, 0, start);
        session.complete(start.plusSeconds(60));
        setSessionId(session, 100L);
        return session;
    }

    private void setSessionId(FocusSession session, Long id) {
        ReflectionTestUtils.setField(session, "id", id);
    }
}
