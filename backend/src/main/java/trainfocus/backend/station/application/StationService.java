package trainfocus.backend.station.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.application.dto.StationRequest;
import trainfocus.backend.station.application.dto.StationResponse;
import trainfocus.backend.station.application.dto.StationsResponse;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.repository.StationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stationRepository;

    public StationsResponse findAllStationsNameAsc() {
        return StationsResponse.from(stationRepository.findAllByOrderByNameAsc());
    }

    @Transactional
    public StationResponse createStation(StationRequest request) {
        if (stationRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.STATION_NAME_DUPLICATE);
        }
        Station station = Station.createNewStation(request.name(), request.latitude(), request.longitude());
        Station newStation = stationRepository.save(station);
        return StationResponse.from(newStation);
    }

    @Transactional
    public StationResponse updateStation(Long stationId, StationRequest request) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));
        if (stationRepository.existsByNameAndIdNot(request.name(), stationId)) {
            throw new BusinessException(ErrorCode.STATION_NAME_DUPLICATE);
        }
        station.update(request.name(), request.latitude(), request.longitude());
        return StationResponse.from(station);
    }
}
