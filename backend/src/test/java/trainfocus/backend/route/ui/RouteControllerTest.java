package trainfocus.backend.route.ui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.route.application.RouteService;
import trainfocus.backend.route.application.dto.DurationResponse;
import trainfocus.backend.user.application.UserService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
class RouteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RouteService routeService;

    @MockitoBean
    FirebaseAuthClient firebaseAuthClient;
    @MockitoBean
    UserService userService;

    @Test
    void 노선_소요시간_조회_200() throws Exception {
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));
        given(routeService.findDurationMinutes(1L, 2L))
                .willReturn(new DurationResponse(1L, 2L, 30));

        mockMvc.perform(get("/api/routes/duration")
                        .header("Authorization", "Bearer valid-token")
                        .param("departureStationId", "1")
                        .param("arrivalStationId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departureStationId").value(1L))
                .andExpect(jsonPath("$.data.arrivalStationId").value(2L))
                .andExpect(jsonPath("$.data.durationMinutes").value(30));
    }

    @Test
    void 출발역과_도착역_같으면_400() throws Exception {
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));
        given(routeService.findDurationMinutes(anyLong(), anyLong()))
                .willThrow(new BusinessException(ErrorCode.ROUTE_SAME_STATION));

        mockMvc.perform(get("/api/routes/duration")
                        .header("Authorization", "Bearer valid-token")
                        .param("departureStationId", "1")
                        .param("arrivalStationId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ROUTE_SAME_STATION"));
    }

    @Test
    void 역이_존재하지_않으면_404() throws Exception {
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));
        given(routeService.findDurationMinutes(anyLong(), anyLong()))
                .willThrow(new BusinessException(ErrorCode.STATION_NOT_FOUND));

        mockMvc.perform(get("/api/routes/duration")
                        .header("Authorization", "Bearer valid-token")
                        .param("departureStationId", "999")
                        .param("arrivalStationId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STATION_NOT_FOUND"));
    }

    @Test
    void 노선이_없으면_404() throws Exception {
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));
        given(routeService.findDurationMinutes(anyLong(), anyLong()))
                .willThrow(new BusinessException(ErrorCode.ROUTE_NOT_FOUND));

        mockMvc.perform(get("/api/routes/duration")
                        .header("Authorization", "Bearer valid-token")
                        .param("departureStationId", "1")
                        .param("arrivalStationId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ROUTE_NOT_FOUND"));
    }

    @Test
    void 토큰_없이_요청하면_401() throws Exception {
        mockMvc.perform(get("/api/routes/duration")
                        .param("departureStationId", "1")
                        .param("arrivalStationId", "2"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_TOKEN_MISSING"));
    }

    @Test
    void 필수_파라미터_누락_시_400() throws Exception {
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));

        mockMvc.perform(get("/api/routes/duration")
                        .header("Authorization", "Bearer valid-token")
                        .param("departureStationId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_INVALID_PARAMETER"));
    }

    @Test
    void 파라미터_타입_불일치_시_400() throws Exception {
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));

        mockMvc.perform(get("/api/routes/duration")
                        .header("Authorization", "Bearer valid-token")
                        .param("departureStationId", "abc")
                        .param("arrivalStationId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_INVALID_PARAMETER"));
    }
}
