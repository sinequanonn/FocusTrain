package trainfocus.backend.auth.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoginUserArgumentResolverTest {

    @Mock
    NativeWebRequest webRequest;

    @InjectMocks
    LoginUserArgumentResolver resolver;

    @Test
    void supportsParameter_LoginUser_애노테이션이_붙은_User_파라미터만_지원() throws Exception {
        MethodParameter loginUserParam = methodParam("withLoginUser", 0);
        MethodParameter normalUserParam = methodParam("withoutLoginUser", 0);
        MethodParameter loginUserStringParam = methodParam("withLoginUserButString", 0);

        assertThat(resolver.supportsParameter(loginUserParam)).isTrue();
        assertThat(resolver.supportsParameter(normalUserParam)).isFalse();
        assertThat(resolver.supportsParameter(loginUserStringParam)).isFalse();
    }

    @Test
    void resolveArgument_LOGIN_USER_ATTRIBUTE_에서_User_를_꺼낸다() {
        User user = UserFixture.withId(1L);
        given(webRequest.getAttribute(FirebaseAuthFilter.LOGIN_USER_ATTRIBUTE, 0))
                .willReturn(user);

        Object result = resolver.resolveArgument(null, null, webRequest, null);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void resolveArgument_attribute_없으면_IllegalStateException() {
        given(webRequest.getAttribute(FirebaseAuthFilter.LOGIN_USER_ATTRIBUTE, 0))
                .willReturn(null);

        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("로그인 사용자");
    }

    private MethodParameter methodParam(String methodName, int index) throws Exception {
        Method method = TestController.class.getDeclaredMethod(methodName, parameterTypeFor(methodName));
        return new MethodParameter(method, index);
    }

    private Class<?> parameterTypeFor(String methodName) {
        return switch (methodName) {
            case "withLoginUser", "withoutLoginUser" -> User.class;
            case "withLoginUserButString" -> String.class;
            default -> throw new IllegalArgumentException(methodName);
        };
    }

    @SuppressWarnings("unused")
    static class TestController {
        public void withLoginUser(@LoginUser User user) {}
        public void withoutLoginUser(User user) {}
        public void withLoginUserButString(@LoginUser String value) {}
    }
}
