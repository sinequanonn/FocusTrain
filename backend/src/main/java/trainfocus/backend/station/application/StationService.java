package trainfocus.backend.station.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.station.application.dto.StationsResponse;
import trainfocus.backend.station.domain.repository.StationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stationRepository;

    public StationsResponse findAllStationsNameAsc() {
        return StationsResponse.from(stationRepository.findAllByOrderByNameAsc());
    }
}
