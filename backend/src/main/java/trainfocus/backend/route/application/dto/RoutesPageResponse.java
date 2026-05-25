package trainfocus.backend.route.application.dto;

import org.springframework.data.domain.Page;
import trainfocus.backend.route.domain.Route;

import java.util.List;

public record RoutesPageResponse(
        List<RouteResponse> routes,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static RoutesPageResponse from(Page<Route> page) {
        return new RoutesPageResponse(
                page.getContent().stream().map(RouteResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
