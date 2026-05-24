package trainfocus.backend.auth.ui;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminOnlyAspect {

    private final UserService userService;

    @Before("@annotation(trainfocus.backend.auth.ui.AdminOnly)" +
            "|| @within(trainfocus.backend.auth.ui.AdminOnly)")
    public void checkAdmin() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        FirebaseUserInfo userInfo = (FirebaseUserInfo) request.getAttribute(FirebaseAuthFilter.FIREBASE_USER_ATTRIBUTE);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_MISSING);
        }

        User user = userService.findByFirebaseUid(userInfo.uid());
        if (!user.isAdmin()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN_ADMIN_ONLY);
        }
    }
}
