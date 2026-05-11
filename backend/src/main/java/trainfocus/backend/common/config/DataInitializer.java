package trainfocus.backend.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.repository.StationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    private static final List<String> STATION_NAMES = List.of(
            "서울", "강릉", "대전", "대구", "광주", "부산", "울산", "목포", "제주"
    );

    /**
     * 실제 KTX 운행 시간(직통) + 환승/항공 추정 시간 (분 단위)
     * 한쪽만 정의해도 양방향으로 생성됨
     *
     * 분류:
     *   - 경부선 KTX 직통: 서울/대전/대구/부산/울산 상호
     *   - 호남선 KTX 직통: 서울/대전 ↔ 광주/목포, 광주↔목포
     *   - 강릉선 KTX 직통: 서울 ↔ 강릉
     *   - 환승 구간: 직통 KTX 없음 (보통 서울 경유 추정 합산)
     *   - 항공: 제주는 KTX 미운행 (체크인+이동 포함 추정)
     */
    private static final List<RouteSeed> ROUTE_SEEDS = List.of(
            // === 서울 ↔ 각 도시 ===
            new RouteSeed("서울", "대전", 60),    // KTX 경부선
            new RouteSeed("서울", "대구", 100),   // KTX 경부선
            new RouteSeed("서울", "부산", 150),   // KTX 경부선
            new RouteSeed("서울", "울산", 125),   // KTX 경부선
            new RouteSeed("서울", "광주", 95),    // KTX 호남선
            new RouteSeed("서울", "목포", 130),   // KTX 호남선
            new RouteSeed("서울", "강릉", 110),   // KTX 강릉선
            new RouteSeed("서울", "제주", 130),   // 항공

            // === 대전 ↔ ===
            new RouteSeed("대전", "대구", 50),    // KTX 경부선
            new RouteSeed("대전", "부산", 100),   // KTX 경부선
            new RouteSeed("대전", "울산", 80),    // KTX 경부선
            new RouteSeed("대전", "광주", 70),    // KTX 호남선
            new RouteSeed("대전", "목포", 110),   // KTX 호남선
            new RouteSeed("대전", "강릉", 180),   // 환승 (서울 경유)
            new RouteSeed("대전", "제주", 165),   // 항공

            // === 대구 ↔ ===
            new RouteSeed("대구", "부산", 50),    // KTX 경부선
            new RouteSeed("대구", "울산", 25),    // KTX 경부선
            new RouteSeed("대구", "광주", 150),   // 환승
            new RouteSeed("대구", "목포", 185),   // 환승
            new RouteSeed("대구", "강릉", 230),   // 환승
            new RouteSeed("대구", "제주", 130),   // 항공

            // === 부산 ↔ ===
            new RouteSeed("부산", "울산", 25),    // KTX 경부선
            new RouteSeed("부산", "광주", 200),   // 환승
            new RouteSeed("부산", "목포", 235),   // 환승
            new RouteSeed("부산", "강릉", 280),   // 환승
            new RouteSeed("부산", "제주", 110),   // 항공

            // === 울산 ↔ ===
            new RouteSeed("울산", "광주", 170),   // 환승
            new RouteSeed("울산", "목포", 210),   // 환승
            new RouteSeed("울산", "강릉", 240),   // 환승
            new RouteSeed("울산", "제주", 140),   // 항공

            // === 광주 ↔ ===
            new RouteSeed("광주", "목포", 35),    // KTX 호남선
            new RouteSeed("광주", "강릉", 205),   // 환승
            new RouteSeed("광주", "제주", 100),   // 항공

            // === 목포 ↔ ===
            new RouteSeed("목포", "강릉", 235),   // 환승
            new RouteSeed("목포", "제주", 145),   // 항공 (광주 경유)

            // === 강릉 ↔ ===
            new RouteSeed("강릉", "제주", 240)    // 환승+항공
    );

    @Override
    @Transactional
    public void run(String... args) {
        seedStations();
        seedRoutes();
    }

    private void seedStations() {
        if (stationRepository.count() > 0) {
            log.info("[DataInitializer] 역 데이터 이미 존재. 시드 스킵");
            return;
        }
        STATION_NAMES.forEach(name ->
                stationRepository.save(Station.createNewStation(name))
        );
        log.info("[DataInitializer] 역 {}개 시드 완료", STATION_NAMES.size());
    }

    private void seedRoutes() {
        if (routeRepository.count() > 0) {
            log.info("[DataInitializer] 노선 데이터 이미 존재. 시드 스킵");
            return;
        }
        Map<String, Station> stationByName = new HashMap<>();
        stationRepository.findAll().forEach(s -> stationByName.put(s.getName(), s));

        int count = 0;
        for (RouteSeed seed : ROUTE_SEEDS) {
            Station from = stationByName.get(seed.from);
            Station to = stationByName.get(seed.to);
            if (from == null || to == null) continue;

            // 양방향 생성
            routeRepository.save(Route.createNewRoute(from, to, seed.durationMinutes));
            routeRepository.save(Route.createNewRoute(to, from, seed.durationMinutes));
            count += 2;
        }
        log.info("[DataInitializer] 노선 {}개 시드 완료 (양방향)", count);
    }

    private record RouteSeed(String from, String to, int durationMinutes) {
    }
}
