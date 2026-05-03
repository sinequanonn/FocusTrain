package trainfocus.backend.session.ui;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trainfocus.backend.auth.ui.LoginUser;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.session.application.FocusSessionService;
import trainfocus.backend.session.application.dto.*;
import trainfocus.backend.session.domain.FocusSessionStatus;
import trainfocus.backend.user.domain.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<FocusSessionCreatedResponse>> create(
            @LoginUser User user,
            @Valid @RequestBody FocusSessionCreatedRequest request
    ) {
        FocusSessionCreatedResponse response = focusSessionService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @PostMapping("/{id}/alight")
    public ResponseEntity<ApiResponse<FocusSessionProgressResponse>> alight(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(focusSessionService.pause(user, id)));
    }

    @PostMapping("/{id}/reboard")
    public ResponseEntity<ApiResponse<FocusSessionProgressResponse>> reboard(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(focusSessionService.resume(user, id)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<FocusSessionEndedResponse>> complete(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(focusSessionService.complete(user, id)));
    }

    @PostMapping("/{id}/abort")
    public ResponseEntity<ApiResponse<FocusSessionEndedResponse>> abort(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(focusSessionService.abort(user, id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FocusSessionDetailResponse>> findById(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(focusSessionService.findById(user, id)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ActiveFocusSessionResponse>> active(
            @LoginUser User user
    ) {
        return ResponseEntity.ok(ApiResponse.of(focusSessionService.findActive(user)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<FocusSessionHistoryPageResponse>> history(
            @LoginUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) FocusSessionStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.of(focusSessionService.findHistory(user, page, size, status))
        );
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<FocusSessionHistoryDetailResponse>> detail(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.of(focusSessionService.findDetail(user, id))
        );
    }
}
