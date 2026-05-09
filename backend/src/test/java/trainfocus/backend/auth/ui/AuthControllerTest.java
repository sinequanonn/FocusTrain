package trainfocus.backend.auth.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import trainfocus.backend.auth.application.AuthService;
import trainfocus.backend.auth.application.dto.LoginRequest;
import trainfocus.backend.auth.application.dto.LoginResponse;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;

import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    FirebaseAuthClient firebaseAuthClient;  // FirebaseAuthFilter 의존성

    @MockitoBean
    UserService userService;               // LoginUserArgumentResolver 의존성

    @Test
    void 로그인_성공_200() throws Exception {
        LoginResponse loginResponse = new LoginResponse(1L, "a@b.com", "이름");
        given(authService.login(any())).willReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("valid-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("a@b.com"))
                .andExpect(jsonPath("$.data.nickname").value("이름"));
    }

    @Test
    void 내_정보_조회_200() throws Exception {
        FirebaseUserInfo info = new FirebaseUserInfo("uid-1", "a@b.com", "이름");
        User user = User.createNewUser("uid-1", "a@b.com", "이름");
        given(firebaseAuthClient.verifyToken("valid-token")).willReturn(info);
        given(userService.findByFirebaseUid("uid-1")).willReturn(user);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("a@b.com"));
    }

    @Test
    void 로그인_idToken_없으면_400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    void Firebase_검증_실패시_401() throws Exception {
        given(authService.login(any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_TOKEN_INVALID));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("bad-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_TOKEN_INVALID"));
    }

    @Test
    void 토큰_없이_내_정보_조회_401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
