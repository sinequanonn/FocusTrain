package trainfocus.backend.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.repository.StationRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String STATIONS_CSV = "data/stations.csv";
    private static final String ROUTES_CSV = "data/routes.csv";

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        seedStations();
        seedRoutes();
    }

    private void seedStations() throws IOException {
        long existing = stationRepository.count();
        if (existing > 0) {
            log.info("[DataInitializer] 역 데이터 이미 존재 (count={}). 시드 스킵", existing);
            return;
        }
        int count = 0;
        try (BufferedReader reader = openCsv(STATIONS_CSV)) {
            reader.readLine(); // skip header: name,latitude,longitude
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(",");
                String name = cols[0].trim();
                BigDecimal lat = new BigDecimal(cols[1].trim());
                BigDecimal lng = new BigDecimal(cols[2].trim());
                stationRepository.save(Station.createNewStation(name, lat, lng));
                count++;
            }
        }
        log.info("[DataInitializer] 역 {}개 시드 완료", count);
    }

    private void seedRoutes() throws IOException {
        long existing = routeRepository.count();
        if (existing > 0) {
            log.info("[DataInitializer] 노선 데이터 이미 존재 (count={}). 시드 스킵", existing);
            return;
        }
        Map<String, Station> stationByName = new HashMap<>();
        stationRepository.findAll().forEach(s -> stationByName.put(s.getName(), s));

        int count = 0;
        int skipped = 0;
        try (BufferedReader reader = openCsv(ROUTES_CSV)) {
            reader.readLine(); // skip header: departure_name,arrival_name,duration_minutes
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(",");
                String depName = cols[0].trim();
                String arrName = cols[1].trim();
                int duration = Integer.parseInt(cols[2].trim());

                Station dep = stationByName.get(depName);
                Station arr = stationByName.get(arrName);
                if (dep == null || arr == null) {
                    log.warn("[DataInitializer] routes.csv 역 누락: dep={}, arr={}", depName, arrName);
                    skipped++;
                    continue;
                }
                routeRepository.save(Route.createNewRoute(dep, arr, duration));
                count++;
            }
        }
        log.info("[DataInitializer] 노선 {}개 시드 완료 (skipped={})", count, skipped);
    }

    private BufferedReader openCsv(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("CSV 파일 없음: " + path);
        }
        return new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    }
}
