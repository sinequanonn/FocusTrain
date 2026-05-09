package trainfocus.backend.route.domain.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import trainfocus.backend.common.config.JpaConfig;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.RouteFixture;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RouteRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void 출발역_도착역_id로_노선_조회_성공() {
        // given
        Station departure = persistStation("강남");
        Station arrival = persistStation("서울역");
        Route route = persistRoute(departure, arrival, 30);

        // when
        Optional<Route> found = routeRepository.findRouteByDepartureStationIdAndArrivalStationId(
                departure.getId(), arrival.getId()
        );

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getDurationMinutes()).isEqualTo(30);
        assertThat(found.get().getId()).isEqualTo(route.getId());
    }

    @Test
    void 없는_노선_조회_시_Optional_empty() {
        // given
        Station departure = persistStation("강남");
        Station arrival = persistStation("잠실");

        // when
        Optional<Route> found = routeRepository.findRouteByDepartureStationIdAndArrivalStationId(
                departure.getId(), arrival.getId()
        );

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 출발_도착이_바뀌면_다른_노선으로_취급() {
        // given
        Station gangnam = persistStation("강남");
        Station seoul = persistStation("서울역");
        persistRoute(gangnam, seoul, 30);

        // when (반대 방향 조회)
        Optional<Route> reversed = routeRepository.findRouteByDepartureStationIdAndArrivalStationId(
                seoul.getId(), gangnam.getId()
        );

        // then
        assertThat(reversed).isEmpty();
    }

    @Test
    void 노선_저장_시_생성일_수정일_자동_설정() {
        // given
        Station departure = persistStation("강남");
        Station arrival = persistStation("서울역");

        // when
        Route saved = persistRoute(departure, arrival, 25);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    private Station persistStation(String name) {
        Station station = StationFixture.withName(name);
        entityManager.persist(station);
        return station;
    }

    private Route persistRoute(Station departure, Station arrival, int durationMinutes) {
        return routeRepository.save(RouteFixture.of(departure, arrival, durationMinutes));
    }
}
