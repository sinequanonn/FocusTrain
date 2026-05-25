package trainfocus.backend.admin.application.dto;

public record DashboardStatsResponse(
        long totalUsers,
        long activeSessions,
        long todayStartedSessions,
        long todayFocusMinutes,
        long totalStations,
        long totalRoutes
) {
}
