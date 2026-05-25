package trainfocus.backend.admin.application.dto;

import org.springframework.data.domain.Page;
import trainfocus.backend.session.domain.FocusSession;

import java.util.List;

public record AdminSessionsPageResponse(
        List<AdminSessionResponse> sessions,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AdminSessionsPageResponse from(Page<FocusSession> page) {
        return new AdminSessionsPageResponse(
                page.getContent().stream().map(AdminSessionResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
