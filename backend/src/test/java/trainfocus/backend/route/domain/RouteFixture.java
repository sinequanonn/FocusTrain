package trainfocus.backend.route.domain;

import trainfocus.backend.station.domain.Station;

public class RouteFixture {

    public static Route of(Station departure, Station arrival, int durationMinutes) {
        return Route.createNewRoute(departure, arrival, durationMinutes);
    }
}
