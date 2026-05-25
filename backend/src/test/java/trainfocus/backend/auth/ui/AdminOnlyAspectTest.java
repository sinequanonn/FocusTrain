package trainfocus.backend.auth.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.Role;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminOnlyAspectTest {

    @Mock
    UserService userService;

    @InjectMocks
    AdminOnlyAspect adminOnlyAspect;

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void 인증_정보가_없으면_AUTH_TOKEN_MISSING() {
        bindRequest(null);

        assertThatThrownBy(adminOnlyAspect::checkAdmin)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUTH_TOKEN_MISSING);
    }

    @Test
    void 일반_사용자면_AUTH_FORBIDDEN_ADMIN_ONLY() {
        bindRequest(new FirebaseUserInfo("uid-1", "u@b.com", "유저"));
        given(userService.findByFirebaseUid("uid-1")).willReturn(UserFixture.withId(1L));

        assertThatThrownBy(adminOnlyAspect::checkAdmin)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUTH_FORBIDDEN_ADMIN_ONLY);
    }

    @Test
    void ADMIN_사용자면_통과() {
        bindRequest(new FirebaseUserInfo("uid-2", "a@b.com", "관리자"));
        User admin = UserFixture.withId(2L);
        ReflectionTestUtils.setField(admin, "role", Role.ADMIN);
        given(userService.findByFirebaseUid("uid-2")).willReturn(admin);

        assertThatCode(adminOnlyAspect::checkAdmin).doesNotThrowAnyException();
    }

    private void bindRequest(FirebaseUserInfo info) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (info != null) {
            request.setAttribute(FirebaseAuthFilter.FIREBASE_USER_ATTRIBUTE, info);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
