package trainfocus.backend.admin.ui;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trainfocus.backend.auth.ui.AdminOnly;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.station.application.StationService;
import trainfocus.backend.station.application.dto.StationRequest;
import trainfocus.backend.station.application.dto.StationResponse;

@AdminOnly
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/station")
public class AdminStationController {

    private final StationService stationService;

    @PostMapping
    public ResponseEntity<ApiResponse<StationResponse>> createStation(
            @Valid @RequestBody StationRequest request
    ) {
        StationResponse response = stationService.createStation(request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PutMapping("{stationId}")
    public ResponseEntity<ApiResponse<StationResponse>> updateStation(
            @PathVariable Long stationId,
            @Valid @RequestBody StationRequest request
    ) {
        StationResponse response = stationService.updateStation(stationId, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
