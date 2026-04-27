package trainfocus.backend.station.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.station.application.StationService;
import trainfocus.backend.station.application.dto.StationsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<ApiResponse<StationsResponse>> findAllStationsNameAsc() {
        StationsResponse response = stationService.findAllStationsNameAsc();
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
