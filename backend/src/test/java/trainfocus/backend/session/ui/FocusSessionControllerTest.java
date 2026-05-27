package trainfocus.backend.session.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.session.application.FocusSessionService;
import trainfocus.backend.session.application.dto.*;
import trainfocus.backend.station.application.dto.StationResponse;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.UserFixture;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FocusSessionController.class)
class FocusSessionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FocusSessionService focusSessionService;

    @MockitoBean
    FirebaseAuthClient firebaseAuthClient;

    @MockitoBean
    UserService userService;

    private static final String BEARER = "Bearer valid-token";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 9, 10, 0);

    @BeforeEach
    void setUp() {
        User user = UserFixture.withId(1L);
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "1@test.com", "테스터"));
        given(userService.findByFirebaseUid("uid-1")).willReturn(user);
    }

    // ===================== POST /api/sessions =====================

    @Test
    void 세션_생성_201() throws Exception {
        FocusSessionCreatedRequest request = new FocusSessionCreatedRequest(10L, 20L, 5);
        FocusSessionCreatedResponse response = new FocusSessionCreatedResponse(
                100L, "RUNNING",
                new StationResponse(10L, "강남", null, null),
                new StationResponse(20L, "서울역", null, null),
                30, 5, 35, NOW, NOW.plusMinutes(35)
        );
        given(focusSessionService.create(any(User.class), any(FocusSessionCreatedRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sessionId").value(100L))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.totalTargetMinutes").value(35));
    }

    @Test
    void 세션_생성_시_필수값_누락_400() throws Exception {
        String invalidJson = "{\"delayMinutes\": 0}";

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"));
    }

    @Test
    void 세션_생성_시_이미_활성_세션이면_409() throws Exception {
        FocusSessionCreatedRequest request = new FocusSessionCreatedRequest(10L, 20L, 0);
        given(focusSessionService.create(any(), any()))
                .willThrow(new BusinessException(ErrorCode.SESSION_ALREADY_ACTIVE));

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SESSION_ALREADY_ACTIVE"));
    }

    // ===================== alight / reboard / complete / abort =====================

    @Test
    void 세션_하차_200() throws Exception {
        given(focusSessionService.pause(any(), eq(100L)))
                .willReturn(new FocusSessionProgressResponse(100L, "PAUSED", 60, 1740));

        mockMvc.perform(post("/api/sessions/100/alight")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"))
                .andExpect(jsonPath("$.data.accumulatedSeconds").value(60));
    }

    @Test
    void 세션_재승차_200() throws Exception {
        given(focusSessionService.resume(any(), eq(100L)))
                .willReturn(new FocusSessionProgressResponse(100L, "RUNNING", 60, 1740));

        mockMvc.perform(post("/api/sessions/100/reboard")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    @Test
    void 세션_완료_200() throws Exception {
        given(focusSessionService.complete(any(), eq(100L)))
                .willReturn(new FocusSessionEndedResponse(100L,
                        "COMPLETED",
                        1800,
                        NOW,
                        NOW.plusMinutes(30),
                        7L, "부산"));

        mockMvc.perform(post("/api/sessions/100/complete")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalFocusSeconds").value(1800))
                .andExpect(jsonPath("$.data.newDepartureStationId").value(7L))
                .andExpect(jsonPath("$.data.newDepartureStationName").value("부산"));;
    }

    @Test
    void 세션_중단_200() throws Exception {
        given(focusSessionService.abort(any(), eq(100L)))
                .willReturn(new FocusSessionEndedResponse(100L,
                        "ABORTED",
                        600,
                        NOW,
                        NOW.plusMinutes(10),
                        7L, "부산"));

        mockMvc.perform(post("/api/sessions/100/abort")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ABORTED"));
    }

    @Test
    void 세션_조작_시_타인_세션이면_403() throws Exception {
        given(focusSessionService.pause(any(), eq(100L)))
                .willThrow(new BusinessException(ErrorCode.SESSION_FORBIDDEN));

        mockMvc.perform(post("/api/sessions/100/alight")
                        .header("Authorization", BEARER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("SESSION_FORBIDDEN"));
    }

    @Test
    void 세션_조작_시_없는_세션이면_404() throws Exception {
        given(focusSessionService.pause(any(), eq(100L)))
                .willThrow(new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        mockMvc.perform(post("/api/sessions/100/alight")
                        .header("Authorization", BEARER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
    }

    @Test
    void 완료_시_누적시간_미달_422() throws Exception {
        given(focusSessionService.complete(any(), eq(100L)))
                .willThrow(new BusinessException(ErrorCode.SESSION_TARGET_NOT_REACHED));

        mockMvc.perform(post("/api/sessions/100/complete")
                        .header("Authorization", BEARER))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("SESSION_TARGET_NOT_REACHED"));
    }

    // ===================== GET =====================

    @Test
    void 세션_상세_200() throws Exception {
        FocusSessionDetailResponse response = new FocusSessionDetailResponse(
                100L, "RUNNING",
                new StationResponse(10L, "강남", null, null),
                new StationResponse(20L, "서울역", null, null),
                1800, 60, 1740,
                NOW, NOW.plusMinutes(30), null
        );
        given(focusSessionService.findById(any(), eq(100L))).willReturn(response);

        mockMvc.perform(get("/api/sessions/100")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(100L))
                .andExpect(jsonPath("$.data.totalTargetSeconds").value(1800));
    }

    @Test
    void 활성_세션_조회_200_있음() throws Exception {
        FocusSessionDetailResponse detail = new FocusSessionDetailResponse(
                100L, "RUNNING",
                new StationResponse(10L, "강남", null, null),
                new StationResponse(20L, "서울역", null, null),
                1800, 60, 1740, NOW, NOW.plusMinutes(30), null);
        given(focusSessionService.findActive(any()))
                .willReturn(new ActiveFocusSessionResponse(true, detail));

        mockMvc.perform(get("/api/sessions/active")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasActiveSession").value(true))
                .andExpect(jsonPath("$.data.session.sessionId").value(100L));
    }

    @Test
    void 활성_세션_조회_200_없음() throws Exception {
        given(focusSessionService.findActive(any()))
                .willReturn(new ActiveFocusSessionResponse(false, null));

        mockMvc.perform(get("/api/sessions/active")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasActiveSession").value(false))
                .andExpect(jsonPath("$.data.session").doesNotExist());
    }

    @Test
    void 세션_히스토리_조회_200() throws Exception {
        FocusSessionHistoryResponse item = new FocusSessionHistoryResponse(
                100L, "COMPLETED",
                new StationResponse(10L, "강남", null, null),
                new StationResponse(20L, "서울역", null, null),
                1800, NOW, NOW.plusMinutes(30)
        );
        FocusSessionHistoryPageResponse page = new FocusSessionHistoryPageResponse(
                List.of(item), 0, 20, 1L, 1, false
        );
        given(focusSessionService.findHistory(any(), eq(0), eq(20), eq(null)))
                .willReturn(page);

        mockMvc.perform(get("/api/sessions/history")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sessionId").value(100L));
    }

    @Test
    void 세션_히스토리_status_파라미터_전달() throws Exception {
        FocusSessionHistoryPageResponse page = new FocusSessionHistoryPageResponse(
                List.of(), 0, 20, 0L, 0, false
        );
        given(focusSessionService.findHistory(any(), eq(0), eq(20),
                eq(trainfocus.backend.session.domain.FocusSessionStatus.ABORTED)))
                .willReturn(page);

        mockMvc.perform(get("/api/sessions/history")
                        .header("Authorization", BEARER)
                        .param("status", "ABORTED"))
                .andExpect(status().isOk());
    }

    @Test
    void 세션_상세_히스토리_200() throws Exception {
        FocusSessionHistoryDetailResponse response = new FocusSessionHistoryDetailResponse(
                new FocusSessionHistoryDetailResponse.SessionInfo(
                        100L, "COMPLETED",
                        new StationResponse(10L, "강남", null, null),
                        new StationResponse(20L, "서울역", null, null),
                        1800, 1800, NOW, NOW.plusMinutes(30), NOW.plusMinutes(30)
                ),
                List.of(new LegResponse(1, NOW, NOW.plusMinutes(30), 1800))
        );
        given(focusSessionService.findDetail(any(), eq(100L))).willReturn(response);

        mockMvc.perform(get("/api/sessions/100/detail")
                        .header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.session.sessionId").value(100L))
                .andExpect(jsonPath("$.data.legs[0].legNumber").value(1));
    }

    // ===================== auth =====================

    @Test
    void 토큰_없이_요청하면_401() throws Exception {
        mockMvc.perform(get("/api/sessions/100"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_TOKEN_MISSING"));
    }
}
