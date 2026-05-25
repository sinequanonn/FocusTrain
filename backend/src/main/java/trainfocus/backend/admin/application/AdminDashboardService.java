package trainfocus.backend.admin.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.admin.application.dto.DashboardStatsResponse;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.session.domain.FocusSessionStatus;
import trainfocus.backend.session.domain.repository.FocusSessionRepository;
import trainfocus.backend.session.domain.repository.LegRepository;
import trainfocus.backend.station.domain.repository.StationRepository;
import trainfocus.backend.user.domain.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final List<FocusSessionStatus> ACTIVE_STATUSES =
            List.of(FocusSessionStatus.RUNNING, FocusSessionStatus.PAUSED);

    private final UserRepository userRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final LegRepository legRepository;
    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    public DashboardStatsResponse getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long totalUsers = userRepository.count();
        long activeSessions = focusSessionRepository.countByStatusIn(ACTIVE_STATUSES);
        long todayStartedSessions = focusSessionRepository.countByStartedAtGreaterThanEqual(todayStart);
        long todayFocusSeconds = legRepository.sumDurationSecondsSince(todayStart);
        long todayFocusMinutes = todayFocusSeconds / 60;
        long totalStations = stationRepository.count();
        long totalRoutes = routeRepository.count();

        return new DashboardStatsResponse(
                totalUsers,
                activeSessions,
                todayStartedSessions,
                todayFocusMinutes,
                totalStations,
                totalRoutes
        );
    }
}
