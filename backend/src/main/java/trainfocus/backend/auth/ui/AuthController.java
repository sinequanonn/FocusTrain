package trainfocus.backend.auth.ui;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import trainfocus.backend.auth.application.AuthService;
import trainfocus.backend.auth.application.dto.LoginRequest;
import trainfocus.backend.auth.application.dto.LoginResponse;
import trainfocus.backend.auth.application.dto.MeResponse;
import trainfocus.backend.auth.application.dto.SignupRequest;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.ui.ApiResponse;
import trainfocus.backend.user.domain.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signUp(HttpServletRequest httpRequest,
                                                             @Valid @RequestBody SignupRequest request) {
        FirebaseUserInfo firebaseUserInfo = (FirebaseUserInfo) httpRequest.getAttribute(FirebaseAuthFilter.FIREBASE_USER_ATTRIBUTE);
        if (firebaseUserInfo == null) {
            throw new IllegalStateException("인증 정보가 존재하지 않습니다.");
        }
        LoginResponse response = authService.signUp(firebaseUserInfo, request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(@LoginUser User user) {
        return ResponseEntity.ok(ApiResponse.of(MeResponse.from(user)));
    }
}
