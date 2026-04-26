package trainfocus.backend.auth.firebase;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthClient {

    private final FirebaseAuth firebaseAuth;

    public FirebaseUserInfo verifyToken(String idToken) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            return FirebaseUserInfo.from(decodedToken);
        } catch (FirebaseAuthException e) {
            log.warn("Firebase 토큰 검증 실패: {} ({})", e.getMessage(), e.getAuthErrorCode());
            if (e.getAuthErrorCode() == AuthErrorCode.EXPIRED_ID_TOKEN) {
                throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
            }
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        } catch (Exception e) {
            log.error("Firebase Admin SDK 오류", e);
            throw new BusinessException(ErrorCode.AUTH_FIREBASE_ERROR);
        }
    }
}
