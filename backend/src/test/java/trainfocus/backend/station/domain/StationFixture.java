package trainfocus.backend.station.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class StationFixture {

    public static Station withName(String name) {
        return Station.createNewStation(name);
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
}
