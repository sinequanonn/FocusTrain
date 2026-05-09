package trainfocus.backend.route.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.StationFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouteTest {

    @Test
    void createNewRoute_정상_생성() {
        Station departure = StationFixture.of(1L, "강남");
        Station arrival = StationFixture.of(2L, "서울역");

        Route route = Route.createNewRoute(departure, arrival, 30);

        assertThat(route.getDepartureStation()).isEqualTo(departure);
        assertThat(route.getArrivalStation()).isEqualTo(arrival);
        assertThat(route.getDurationMinutes()).isEqualTo(30);
    }

    @Test
    void 출발역과_도착역의_id가_같으면_ROUTE_SAME_STATION() {
        Station departure = StationFixture.of(1L, "강남");
        Station arrival = StationFixture.of(1L, "강남");

        assertThatThrownBy(() -> Route.createNewRoute(departure, arrival, 30))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ROUTE_SAME_STATION));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void durationMinutes가_0이하면_INVALID_PARAMETER(int invalid) {
        Station departure = StationFixture.of(1L, "강남");
        Station arrival = StationFixture.of(2L, "서울역");

        assertThatThrownBy(() -> Route.createNewRoute(departure, arrival, invalid))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COMMON_INVALID_PARAMETER));
    }

    @Test
    void durationMinutes가_null이면_INVALID_PARAMETER() {
        Station departure = StationFixture.of(1L, "강남");
        Station arrival = StationFixture.of(2L, "서울역");

        assertThatThrownBy(() -> Route.createNewRoute(departure, arrival, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.COMMON_INVALID_PARAMETER));
    }

    @Test
    void persist_전_id가_null이면_같은_역_검증은_스킵() {
        // JPA persist 전 단계 (id 없음): 검증 스킵 — id 기반 동등성을 쓸 수 없으므로
        // 실제 같은 역인지는 DB unique constraint에 위임한다.
        Station a = StationFixture.withName("강남");
        Station b = StationFixture.withName("강남");

        // 예외 없이 생성됨
        Route route = Route.createNewRoute(a, b, 30);
        assertThat(route).isNotNull();
    }
}
