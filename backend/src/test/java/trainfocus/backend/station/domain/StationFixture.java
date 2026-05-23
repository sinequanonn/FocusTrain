package trainfocus.backend.station.domain;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class StationFixture {

    private static final BigDecimal DEFAULT_LAT = new BigDecimal("37.5547");
    private static final BigDecimal DEFAULT_LNG = new BigDecimal("126.9707");

    public static Station withName(String name) {
        return Station.createNewStation(name, DEFAULT_LAT, DEFAULT_LNG);
    }

    /**
     * persisted 상태를 흉내내야 하는 단위 테스트용 (서비스 테스트의 mock 응답 등).
     * Repository 테스트에서는 persist/save가 알아서 ID를 채우므로 이 메서드 대신 withName을 쓴다.
     */
    public static Station of(Long id, String name) {
        Station station = withName(name);
        ReflectionTestUtils.setField(station, "id", id);
        return station;
    }

    public static Station of(Long id, String name, BigDecimal latitude, BigDecimal longitude) {
        Station station = Station.createNewStation(name, latitude, longitude);
        ReflectionTestUtils.setField(station, "id", id);
        return station;
    }
}
