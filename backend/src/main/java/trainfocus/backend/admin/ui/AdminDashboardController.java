package trainfocus.backend.admin.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trainfocus.backend.admin.application.AdminDashboardService;
import trainfocus.backend.admin.application.dto.DashboardStatsResponse;
import trainfocus.backend.auth.ui.AdminOnly;
import trainfocus.backend.common.ui.ApiResponse;

@AdminOnly
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.of(adminDashboardService.getStats()));
    }
}
