package trainfocus.backend.station.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trainfocus.backend.station.application.dto.StationsResponse;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;
import trainfocus.backend.station.domain.repository.StationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    StationRepository stationRepository;

    @InjectMocks
    StationService stationService;

    @Test
    void 전체_역을_이름_오름차순으로_반환() {
        // given
        Station gangnam = StationFixture.of(1L, "강남");
        Station seoul = StationFixture.of(2L, "서울역");
        given(stationRepository.findAllByOrderByNameAsc())
                .willReturn(List.of(gangnam, seoul));

        // when
        StationsResponse response = stationService.findAllStationsNameAsc();

        // then
        assertThat(response.stations()).hasSize(2);
        assertThat(response.stations()).extracting("name")
                .containsExactly("강남", "서울역");
        assertThat(response.stations()).extracting("id")
                .containsExactly(1L, 2L);
    }

    @Test
    void 역이_없으면_빈_리스트_응답() {
        // given
        given(stationRepository.findAllByOrderByNameAsc()).willReturn(List.of());

        // when
        StationsResponse response = stationService.findAllStationsNameAsc();

        // then
        assertThat(response.stations()).isEmpty();
    }
}
