package trainfocus.backend.user.ui;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trainfocus.backend.auth.application.dto.MeResponse;
import trainfocus.backend.auth.ui.LoginUser;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.application.dto.UpdateDepartureStationRequest;
import trainfocus.backend.user.application.dto.UpdateNicknameRequest;
import trainfocus.backend.user.domain.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> updateNickname(
            @Valid @RequestBody UpdateNicknameRequest request,
            @LoginUser User user) {
        MeResponse response = userService.updateNickname(request, user);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PutMapping("/me/departure-station")
    public ResponseEntity<ApiResponse<MeResponse>> updateDepartureStation(
            @Valid @RequestBody UpdateDepartureStationRequest request,
            @LoginUser User user) {
        MeResponse response = userService.updateDepartureStation(request, user);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
