package trainfocus.backend.admin.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trainfocus.backend.admin.application.dto.AdminSessionsPageResponse;
import trainfocus.backend.auth.ui.AdminOnly;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.session.application.FocusSessionService;

@AdminOnly
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
public class AdminSessionController {

    private final FocusSessionService focusSessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminSessionsPageResponse>> findActives(@PageableDefault(size = 20) Pageable pageable) {
        AdminSessionsPageResponse response = focusSessionService.findActivesForAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
