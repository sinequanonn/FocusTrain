package trainfocus.backend.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.auth.application.dto.LoginRequest;
import trainfocus.backend.auth.application.dto.LoginResponse;
import trainfocus.backend.auth.application.dto.SignupRequest;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.domain.repository.StationRepository;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final FirebaseAuthClient firebaseAuthClient;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;

    public LoginResponse login(LoginRequest request) {
        FirebaseUserInfo firebaseUserInfo = firebaseAuthClient.verifyToken(request.idToken());
        User user = userRepository.findByFirebaseUid(firebaseUserInfo.uid())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return LoginResponse.from(user);
    }

    @Transactional
    public LoginResponse signUp(FirebaseUserInfo firebaseUserInfo,
                                SignupRequest request) {
        if (userRepository.existsByFirebaseUid(firebaseUserInfo.uid())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_REGISTERED);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        User newUser = User.createNewUser(
                firebaseUserInfo.uid(),
                firebaseUserInfo.email(),
                request.nickname()
        );

        try {
            User saved = userRepository.save(newUser);
            return LoginResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }
    }
}
