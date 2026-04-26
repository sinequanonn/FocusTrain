package trainfocus.backend.auth.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        FirebaseUserInfo userInfo = (FirebaseUserInfo) webRequest
                .getAttribute(FirebaseAuthFilter.FIREBASE_USER_ATTRIBUTE, 0);
        if (userInfo == null) {
            throw new IllegalStateException("인증 정보가 존재하지 않습니다.");
        }
        return userService.findByFirebaseUid(userInfo.uid());
    }
}
