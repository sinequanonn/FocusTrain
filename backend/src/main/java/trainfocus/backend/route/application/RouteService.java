package trainfocus.backend.route.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.route.application.dto.DurationResponse;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.station.domain.repository.StationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    public DurationResponse findDurationMinutes(
            Long departureStationId, Long arrivalStationId
    ) {
        if (departureStationId.equals(arrivalStationId)) {
            throw new BusinessException(ErrorCode.ROUTE_SAME_STATION);
        }
        if (!stationRepository.existsById(departureStationId)
                || !stationRepository.existsById(arrivalStationId)) {
            throw new BusinessException(ErrorCode.STATION_NOT_FOUND);
        }

        Route route = routeRepository.findRouteByDepartureStationIdAndArrivalStationId(departureStationId, arrivalStationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        return DurationResponse.from(route);
    }
}
