package trainfocus.backend.station.domain;

import org.junit.jupiter.api.Test;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StationTest {

    private static final BigDecimal SEOUL_LAT = new BigDecimal("37.5547");
    private static final BigDecimal SEOUL_LNG = new BigDecimal("126.9707");

    @Test
    void 한반도_범위_안의_좌표로_역_생성_성공() {
        Station station = Station.createNewStation("서울역", SEOUL_LAT, SEOUL_LNG);

        assertThat(station.getName()).isEqualTo("서울역");
        assertThat(station.getLatitude()).isEqualByComparingTo(SEOUL_LAT);
        assertThat(station.getLongitude()).isEqualByComparingTo(SEOUL_LNG);
    }

    @Test
    void 위도가_한반도_남쪽_경계_미만이면_생성_실패() {
        assertThatThrownBy(() -> Station.createNewStation("이상한역", new BigDecimal("32.9"), SEOUL_LNG))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.STATION_COORDINATE_OUT_OF_RANGE);
    }

    @Test
    void 경계값은_포함된다() {
        Station nw = Station.createNewStation("북서끝", new BigDecimal("39.0"), new BigDecimal("124.0"));
        Station se = Station.createNewStation("남동끝", new BigDecimal("33.0"), new BigDecimal("131.0"));

        assertThat(nw.getName()).isEqualTo("북서끝");
        assertThat(se.getName()).isEqualTo("남동끝");
    }

    @Test
    void update_정상_시_이름과_좌표가_모두_갱신된다() {
        Station station = Station.createNewStation("기존역", SEOUL_LAT, SEOUL_LNG);
        BigDecimal newLat = new BigDecimal("35.1796");
        BigDecimal newLng = new BigDecimal("129.0756");

        station.update("부산역", newLat, newLng);

        assertThat(station.getName()).isEqualTo("부산역");
        assertThat(station.getLatitude()).isEqualByComparingTo(newLat);
        assertThat(station.getLongitude()).isEqualByComparingTo(newLng);
    }

    @Test
    void update_시_좌표_범위_벗어나면_실패하고_원본_유지() {
        Station station = Station.createNewStation("기존역", SEOUL_LAT, SEOUL_LNG);

        assertThatThrownBy(() -> station.update("새이름", new BigDecimal("50.0"), SEOUL_LNG))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.STATION_COORDINATE_OUT_OF_RANGE);

        assertThat(station.getName()).isEqualTo("기존역");
        assertThat(station.getLatitude()).isEqualByComparingTo(SEOUL_LAT);
        assertThat(station.getLongitude()).isEqualByComparingTo(SEOUL_LNG);
    }
}
