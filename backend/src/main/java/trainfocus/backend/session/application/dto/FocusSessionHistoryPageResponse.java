package trainfocus.backend.session.application.dto;

import org.springframework.data.domain.Page;
import trainfocus.backend.session.domain.FocusSession;

import java.util.List;

public record FocusSessionHistoryPageResponse(
        List<FocusSessionHistoryResponse> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean hasNext
) {
    public static FocusSessionHistoryPageResponse from(Page<FocusSession> page) {
        return new FocusSessionHistoryPageResponse(
                page.getContent().stream()
                        .map(FocusSessionHistoryResponse::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
