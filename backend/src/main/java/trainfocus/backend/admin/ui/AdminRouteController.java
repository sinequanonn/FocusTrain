package trainfocus.backend.admin.ui;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trainfocus.backend.auth.ui.AdminOnly;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.route.application.RouteService;
import trainfocus.backend.route.application.dto.*;

@AdminOnly
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/routes")
public class AdminRouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<ApiResponse<RoutesPageResponse>> findAllRoutes(
            @RequestParam(value = "q", required = false) String q,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        RoutesPageResponse response = routeService.findAllRoutes(q, pageable);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoutesResponse>> create(
            @Valid @RequestBody RouteRequest request
    ) {
        RoutesResponse response = routeService.createRoutes(request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PatchMapping("/{routeId}")
    public ResponseEntity<ApiResponse<RouteResponse>> updateDuration(
            @PathVariable Long routeId,
            @Valid @RequestBody RouteDurationRequest request
    ) {
        RouteResponse response = routeService.updateDuration(routeId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long routeId) {
        routeService.deleteRoute(routeId);
        return ResponseEntity.ok(ApiResponse.of(null));
    }
}
