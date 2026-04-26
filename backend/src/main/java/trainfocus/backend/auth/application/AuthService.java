package trainfocus.backend.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import trainfocus.backend.auth.application.dto.LoginRequest;
import trainfocus.backend.auth.application.dto.LoginResponse;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FirebaseAuthClient firebaseAuthClient;
    private final UserService userService;

    public LoginResponse login(LoginRequest request) {
        FirebaseUserInfo firebaseUserInfo = firebaseAuthClient.verifyToken(request.idToken());
        User user = userService.findOrCreateUser(firebaseUserInfo);
        return LoginResponse.from(user);
    }
}
