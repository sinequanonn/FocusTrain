package trainfocus.backend.session.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.session.application.dto.*;
import trainfocus.backend.session.domain.FocusSession;
import trainfocus.backend.session.domain.FocusSessionStatus;
import trainfocus.backend.session.domain.repository.FocusSessionRepository;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.repository.StationRepository;
import trainfocus.backend.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusSessionService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final List<FocusSessionStatus> ACTIVE_STATUSES =
            List.of(FocusSessionStatus.RUNNING, FocusSessionStatus.PAUSED);
    private static final List<FocusSessionStatus> ENDED_STATUSES =
            List.of(FocusSessionStatus.COMPLETED, FocusSessionStatus.ABORTED);


    private final FocusSessionRepository focusSessionRepository;
    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    @Transactional
    public FocusSessionCreatedResponse create(User user, FocusSessionCreatedRequest request) {
        if (focusSessionRepository.existsByUserAndStatusIn(user, ACTIVE_STATUSES)) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_ACTIVE);
        }

        Station departure = stationRepository.findById(request.departureStationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));
        Station arrival = stationRepository.findById(request.arrivalStationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));

        Route route = routeRepository
                .findRouteByDepartureStationIdAndArrivalStationId(departure.getId(), arrival.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));

        FocusSession session = FocusSession.createNewFocusSession(
                user, departure, arrival,
                route.getDurationMinutes(),
                request.delayMinutes(),
                LocalDateTime.now()
        );
        return FocusSessionCreatedResponse.from(focusSessionRepository.save(session));
    }

    @Transactional
    public FocusSessionProgressResponse pause(User user, Long sessionId) {
        LocalDateTime now = LocalDateTime.now();
        FocusSession session = findOwnedSession(user, sessionId);
        session.pause(now);
        return FocusSessionProgressResponse.from(session, now);
    }

    @Transactional
    public FocusSessionProgressResponse resume(User user, Long sessionId) {
        LocalDateTime now = LocalDateTime.now();
        FocusSession session = findOwnedSession(user, sessionId);
        session.resume(now);
        return FocusSessionProgressResponse.from(session, now);
    }

    @Transactional
    public FocusSessionEndedResponse complete(User user, Long sessionId) {
        LocalDateTime now = LocalDateTime.now();
        FocusSession session = findOwnedSession(user, sessionId);
        session.complete(now);
        return FocusSessionEndedResponse.from(session);
    }

    @Transactional
    public FocusSessionEndedResponse abort(User user, Long sessionId) {
        LocalDateTime now = LocalDateTime.now();
        FocusSession session = findOwnedSession(user, sessionId);
        session.abort(now);
        return FocusSessionEndedResponse.from(session);
    }

    @Transactional
    public FocusSessionDetailResponse findById(User user, Long sessionId) {
        LocalDateTime now = LocalDateTime.now();
        FocusSession session = findOwnedSession(user, sessionId);
        session.autoCompleteIfTargetReached(now);
        return FocusSessionDetailResponse.from(session, LocalDateTime.now());
    }

    @Transactional
    public ActiveFocusSessionResponse findActive(User user) {
        LocalDateTime now = LocalDateTime.now();
        return focusSessionRepository
                .findFirstByUserAndStatusIn(user, ACTIVE_STATUSES)
                .map(session -> {
                    session.autoCompleteIfTargetReached(now);
                    if (session.getStatus().isEnded()) {
                        return ActiveFocusSessionResponse.empty();
                    }
                    return ActiveFocusSessionResponse.of(session, now);
                })
                .orElseGet(ActiveFocusSessionResponse::empty);
    }

    public FocusSessionHistoryPageResponse findHistory(
            User user, int page, int size, FocusSessionStatus status
    ) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(
                page, safeSize, Sort.by(Sort.Direction.DESC, "endedAt")
        );

        List<FocusSessionStatus> statuses;
        if (status == null) {
            statuses = ENDED_STATUSES;
        } else if (ENDED_STATUSES.contains(status)) {
            statuses = List.of(status);
        } else {
            throw new BusinessException(ErrorCode.COMMON_INVALID_PARAMETER);
        }

        Page<FocusSession> result = focusSessionRepository
                .findByUserAndStatusIn(user, statuses, pageRequest);
        return FocusSessionHistoryPageResponse.from(result);
    }

    public FocusSessionHistoryDetailResponse findDetail(User user, Long sessionId) {
        FocusSession session = focusSessionRepository.findDetailById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        if (!session.isOwnedBy(user)) {
            throw new BusinessException(ErrorCode.SESSION_FORBIDDEN);
        }
        return FocusSessionHistoryDetailResponse.from(session);
    }


    private FocusSession findOwnedSession(User user, Long sessionId) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        if (!session.isOwnedBy(user)) {
            throw new BusinessException(ErrorCode.SESSION_FORBIDDEN);
        }
        return session;
    }
}
