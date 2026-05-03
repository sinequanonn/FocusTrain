package trainfocus.backend.session.application;

import lombok.RequiredArgsConstructor;
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

    private static final List<FocusSessionStatus> ACTIVE_STATUSES =
            List.of(FocusSessionStatus.RUNNING, FocusSessionStatus.PAUSED);

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

        FocusSession session = FocusSession.create(
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

    public FocusSessionDetailResponse findById(User user, Long sessionId) {
        FocusSession session = findOwnedSession(user, sessionId);
        return FocusSessionDetailResponse.from(session, LocalDateTime.now());
    }

    public ActiveFocusSessionResponse findActive(User user) {
        LocalDateTime now = LocalDateTime.now();
        return focusSessionRepository
                .findFirstByUserAndStatusIn(user, ACTIVE_STATUSES)
                .map(session -> ActiveFocusSessionResponse.of(session, now))
                .orElseGet(ActiveFocusSessionResponse::empty);
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
