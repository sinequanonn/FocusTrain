package trainfocus.backend.route.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.route.application.dto.*;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.repository.StationRepository;

import java.util.ArrayList;
import java.util.List;

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

    public RoutesPageResponse findAllRoutes(String keyword, Pageable pageable) {
        return RoutesPageResponse.from(routeRepository.findAllForAdmin(keyword, pageable));
    }

    @Transactional
    public RoutesResponse createRoutes(RouteRequest request) {
        Long depId = request.departureStationId();
        Long arrId = request.arrivalStationId();
        if (depId.equals(arrId)) {
            throw new BusinessException(ErrorCode.ROUTE_SAME_STATION);
        }

        Station departure = stationRepository.findById(depId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));
        Station arrival = stationRepository.findById(arrId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));

        if (routeRepository.existsByDepartureStationIdAndArrivalStationId(depId, arrId)) {
            throw new BusinessException(ErrorCode.ROUTE_DUPLICATE);
        }
        boolean bidirectional = Boolean.TRUE.equals(request.bidirectional());
        if (bidirectional && routeRepository.existsByDepartureStationIdAndArrivalStationId(arrId, depId)) {
            throw new BusinessException(ErrorCode.ROUTE_DUPLICATE);
        }

        List<Route> savedRoutes = new ArrayList<>();
        savedRoutes.add(routeRepository.save(Route.createNewRoute(departure, arrival, request.durationMinutes())));
        if (bidirectional) {
            savedRoutes.add(routeRepository.save(Route.createNewRoute(arrival, departure, request.durationMinutes())));
        }
        return RoutesResponse.from(savedRoutes);
    }

    @Transactional
    public RouteResponse updateDuration(Long routeId, RouteDurationRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
        route.updateDurationMinutes(request.durationMinutes());
        return RouteResponse.from(route);
    }

    @Transactional
    public void deleteRoute(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new BusinessException(ErrorCode.ROUTE_NOT_FOUND);
        }
        routeRepository.deleteById(routeId);
    }
}
