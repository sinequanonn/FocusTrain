package trainfocus.backend.route.application.dto;

import trainfocus.backend.route.domain.Route;

import java.util.List;

public record RoutesResponse(List<RouteResponse> routes) {
    public static RoutesResponse from(List<Route> routes) {
        return new RoutesResponse(routes.stream().map(RouteResponse::from).toList());
    }
}
