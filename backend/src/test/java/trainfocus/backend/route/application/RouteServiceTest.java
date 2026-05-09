package trainfocus.backend.route.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.route.application.dto.DurationResponse;
import trainfocus.backend.route.domain.Route;
import trainfocus.backend.route.domain.RouteFixture;
import trainfocus.backend.route.domain.repository.RouteRepository;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;
import trainfocus.backend.station.domain.repository.StationRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    RouteRepository routeRepository;

    @Mock
    StationRepository stationRepository;

    @InjectMocks
    RouteService routeService;

    @Test
    void 노선_소요시간_조회_성공() {
        // given
        Station departure = StationFixture.of(1L, "강남");
        Station arrival = StationFixture.of(2L, "서울역");
        Route route = RouteFixture.of(departure, arrival, 30);

        given(stationRepository.existsById(1L)).willReturn(true);
        given(stationRepository.existsById(2L)).willReturn(true);
        given(routeRepository.findRouteByDepartureStationIdAndArrivalStationId(1L, 2L))
                .willReturn(Optional.of(route));

        // when
        DurationResponse response = routeService.findDurationMinutes(1L, 2L);

        // then
        assertThat(response.durationMinutes()).isEqualTo(30);
        assertThat(response.departureStationId()).isEqualTo(1L);
        assertThat(response.arrivalStationId()).isEqualTo(2L);
    }

    @Test
    void 출발역과_도착역이_같으면_ROUTE_SAME_STATION_예외() {
        assertThatThrownBy(() -> routeService.findDurationMinutes(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ROUTE_SAME_STATION));
    }

    @Test
    void 출발역이_없으면_STATION_NOT_FOUND_예외() {
        // given
        given(stationRepository.existsById(1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> routeService.findDurationMinutes(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.STATION_NOT_FOUND));
    }

    @Test
    void 도착역이_없으면_STATION_NOT_FOUND_예외() {
        // given
        given(stationRepository.existsById(1L)).willReturn(true);
        given(stationRepository.existsById(2L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> routeService.findDurationMinutes(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.STATION_NOT_FOUND));
    }

    @Test
    void 노선이_등록되지_않았으면_ROUTE_NOT_FOUND_예외() {
        // given
        given(stationRepository.existsById(1L)).willReturn(true);
        given(stationRepository.existsById(2L)).willReturn(true);
        given(routeRepository.findRouteByDepartureStationIdAndArrivalStationId(1L, 2L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> routeService.findDurationMinutes(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ROUTE_NOT_FOUND));
    }
}
