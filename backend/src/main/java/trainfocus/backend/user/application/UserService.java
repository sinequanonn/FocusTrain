package trainfocus.backend.user.application;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreateUser(FirebaseUserInfo firebaseUserInfo) {
        return userRepository.findByFirebaseUid(firebaseUserInfo.uid())
                .orElseGet(() -> {
                    User newUser = User.createNewUser(
                            firebaseUserInfo.uid(),
                            firebaseUserInfo.email(),
                            firebaseUserInfo.name()
                    );
                    return userRepository.save(newUser);
                });
    }

    public User findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
