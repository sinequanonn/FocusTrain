package trainfocus.backend.route.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.route.application.RouteService;
import trainfocus.backend.route.application.dto.DurationResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/duration")
    public ResponseEntity<ApiResponse<DurationResponse>> findRouteDuration(
            @RequestParam("departureStationId") Long departureStationId,
            @RequestParam("arrivalStationId") Long arrivalStationId
    ) {
        DurationResponse response = routeService.findDurationMinutes(departureStationId, arrivalStationId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
