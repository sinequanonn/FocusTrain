package trainfocus.backend.station.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.application.dto.StationRequest;
import trainfocus.backend.station.application.dto.StationResponse;
import trainfocus.backend.station.application.dto.StationsResponse;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;
import trainfocus.backend.station.domain.repository.StationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void createStation_정상_등록() {
        StationRequest request = new StationRequest("강남", new BigDecimal("37.4979"), new BigDecimal("127.0276"));
        given(stationRepository.existsByName("강남")).willReturn(false);
        given(stationRepository.save(any(Station.class)))
                .willAnswer(invocation -> {
                    Station saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 10L);
                    return saved;
                });

        StationResponse response = stationService.createStation(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("강남");
        assertThat(response.latitude()).isEqualByComparingTo(request.latitude());
        assertThat(response.longitude()).isEqualByComparingTo(request.longitude());
    }

    @Test
    void createStation_중복된_이름이면_STATION_NAME_DUPLICATE() {
        StationRequest request = new StationRequest("강남", new BigDecimal("37.4979"), new BigDecimal("127.0276"));
        given(stationRepository.existsByName("강남")).willReturn(true);

        assertThatThrownBy(() -> stationService.createStation(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.STATION_NAME_DUPLICATE);
    }

    @Test
    void updateStation_존재하지_않는_id면_STATION_NOT_FOUND() {
        StationRequest request = new StationRequest("강남", new BigDecimal("37.5"), new BigDecimal("127.0"));
        given(stationRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> stationService.updateStation(99L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.STATION_NOT_FOUND);
    }

    @Test
    void updateStation_다른_역의_이름과_중복이면_STATION_NAME_DUPLICATE() {
        Station station = StationFixture.of(1L, "강남");
        given(stationRepository.findById(1L)).willReturn(Optional.of(station));
        given(stationRepository.existsByNameAndIdNot("서울역", 1L)).willReturn(true);

        assertThatThrownBy(() -> stationService.updateStation(1L,
                new StationRequest("서울역", new BigDecimal("37.5"), new BigDecimal("127.0"))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.STATION_NAME_DUPLICATE);
    }

    @Test
    void updateStation_자기_자신_이름_그대로_좌표만_변경하면_성공() {
        Station station = StationFixture.of(1L, "강남");
        given(stationRepository.findById(1L)).willReturn(Optional.of(station));
        given(stationRepository.existsByNameAndIdNot("강남", 1L)).willReturn(false);
        BigDecimal newLat = new BigDecimal("37.5");
        BigDecimal newLng = new BigDecimal("127.1");

        StationResponse response = stationService.updateStation(1L,
                new StationRequest("강남", newLat, newLng));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("강남");
        assertThat(response.latitude()).isEqualByComparingTo(newLat);
        assertThat(response.longitude()).isEqualByComparingTo(newLng);
    }
}
