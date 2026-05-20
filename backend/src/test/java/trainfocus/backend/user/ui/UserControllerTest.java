package trainfocus.backend.user.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import trainfocus.backend.auth.application.dto.MeResponse;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.application.dto.UpdateNicknameRequest;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @MockitoBean
    FirebaseAuthClient firebaseAuthClient;

    private static final String BEARER = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        User user = UserFixture.withId(1L);
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "1@test.com", "테스터1"));
        given(userService.findByFirebaseUid("uid-1")).willReturn(user);
    }

    @Test
    void 닉네임_업데이트_성공_200() throws Exception {
        given(userService.updateNickname(any(UpdateNicknameRequest.class), any(User.class)))
                .willReturn(new MeResponse(1L, "1@test.com", "새닉네임", null));

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("새닉네임"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    void 닉네임_중복이면_400() throws Exception {
        given(userService.updateNickname(any(), any()))
                .willThrow(new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE));

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("중복닉네임"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("USER_NICKNAME_DUPLICATE"));
    }

    @Test
    void 닉네임_빈값이면_400() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    void 닉네임_1자이면_400() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("A"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    void 닉네임_21자이면_400() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("a".repeat(21)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    void 허용되지_않는_문자이면_400() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("닉네임!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    void 토큰_없으면_401() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("새닉네임"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_TOKEN_MISSING"));
    }
}
