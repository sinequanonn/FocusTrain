package trainfocus.backend.auth.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthFilterTest {

    @Mock
    FirebaseAuthClient firebaseAuthClient;

    @Mock
    FilterChain filterChain;

    private FirebaseAuthFilter filter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        filter = new FirebaseAuthFilter(firebaseAuthClient, objectMapper);
    }

    @Test
    void Authorization_헤더_없으면_401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sessions");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    void Bearer_아닌_헤더면_401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sessions");
        request.addHeader("Authorization", "Basic some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void 유효한_토큰이면_attribute_설정_후_다음_필터로_진행() throws Exception {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sessions");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(firebaseAuthClient.verifyToken("valid-token")).willReturn(info);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute(FirebaseAuthFilter.FIREBASE_USER_ATTRIBUTE)).isEqualTo(info);
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    void 만료된_토큰이면_401_응답_본문에_에러코드_포함() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sessions");
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(firebaseAuthClient.verifyToken("expired-token"))
                .willThrow(new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH_TOKEN_EXPIRED");
    }

    @Test
    void 로그인_경로는_필터_적용_제외() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void 로그인_외_경로는_필터_적용() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }
}
