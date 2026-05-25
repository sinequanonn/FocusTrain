package trainfocus.backend.station.domain.repository;

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
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StationRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    StationRepository stationRepository;

    @Test
    void 이름_오름차순으로_전체_역_조회() {
        // given
        stationRepository.save(StationFixture.withName("강남"));
        stationRepository.save(StationFixture.withName("서울역"));
        stationRepository.save(StationFixture.withName("건대입구"));

        // when
        List<Station> stations = stationRepository.findAllByOrderByNameAsc();

        // then
        assertThat(stations).extracting(Station::getName)
                .containsExactly("강남", "건대입구", "서울역");
    }

    @Test
    void 역이_없으면_빈_리스트_반환() {
        // when
        List<Station> stations = stationRepository.findAllByOrderByNameAsc();

        // then
        assertThat(stations).isEmpty();
    }

    @Test
    void 역_저장_시_생성일_수정일_자동_설정() {
        // given
        Station station = StationFixture.withName("잠실");

        // when
        Station saved = stationRepository.save(station);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void existsByName_등록된_이름이면_true_아니면_false() {
        stationRepository.save(StationFixture.withName("강남"));

        assertThat(stationRepository.existsByName("강남")).isTrue();
        assertThat(stationRepository.existsByName("없는역")).isFalse();
    }

    @Test
    void existsByNameAndIdNot_자기_자신은_제외() {
        Station gangnam = stationRepository.save(StationFixture.withName("강남"));
        Station seoul = stationRepository.save(StationFixture.withName("서울역"));

        assertThat(stationRepository.existsByNameAndIdNot("강남", gangnam.getId())).isFalse();
        assertThat(stationRepository.existsByNameAndIdNot("강남", seoul.getId())).isTrue();
    }
}
