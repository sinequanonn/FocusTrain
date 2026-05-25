package trainfocus.backend.admin.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.station.application.StationService;
import trainfocus.backend.station.application.dto.StationRequest;
import trainfocus.backend.station.application.dto.StationResponse;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.UserFixture;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminStationController.class)
class AdminStationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    StationService stationService;

    @MockitoBean
    FirebaseAuthClient firebaseAuthClient;

    @MockitoBean
    UserService userService;

    @Test
    void 역_등록_정상_200() throws Exception {
        StationRequest request = new StationRequest("강남",
                new BigDecimal("37.4979"),
                new BigDecimal("127.0276"));

        StationResponse response = new StationResponse(1L, "강남", request.latitude(), request.longitude());
        bindAuth();

        given(stationService.createStation(any(StationRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/admin/station")
                        .header("Authorization", "Bearer valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("강남"));
    }

    @Test
    void 역_수정_정상_200() throws Exception {
        StationRequest request = new StationRequest("강남", new BigDecimal("37.5"), new BigDecimal("127.1"));
        StationResponse response = new StationResponse(7L, "강남", request.latitude(), request.longitude());
        bindAuth();
        given(stationService.updateStation(eq(7L), any(StationRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/admin/station/7")
                        .header("Authorization", "Bearer valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7L));
    }

    @Test
    void 토큰_없이_역_등록_401() throws Exception {
        StationRequest request = new StationRequest("강남", new BigDecimal("37.5"), new BigDecimal("127.0"));

        mockMvc.perform(post("/api/admin/station")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_TOKEN_MISSING"));
    }

    private void bindAuth() {
        given(firebaseAuthClient.verifyToken("valid"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "관리자"));
        given(userService.findOrCreateUser(any(FirebaseUserInfo.class)))
                .willReturn(UserFixture.withId(1L));
        given(userService.findByFirebaseUid("uid-1"))
                .willReturn(UserFixture.withId(1L));
    }
}
