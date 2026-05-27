package trainfocus.backend.station.ui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import trainfocus.backend.auth.firebase.FirebaseAuthClient;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.station.application.StationService;
import trainfocus.backend.station.application.dto.StationResponse;
import trainfocus.backend.station.application.dto.StationsResponse;
import trainfocus.backend.user.application.UserService;
import trainfocus.backend.user.domain.UserFixture;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StationController.class)
class StationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StationService stationService;

    @MockitoBean
    FirebaseAuthClient firebaseAuthClient;  // FirebaseAuthFilter 의존성

    @MockitoBean
    UserService userService;                // LoginUserArgumentResolver 의존성

    @Test
    void 전체_역_조회_200() throws Exception {
        // given
        StationsResponse response = new StationsResponse(List.of(
                new StationResponse(1L, "강남", null, null),
                new StationResponse(2L, "서울역", null, null)
        ));
        given(firebaseAuthClient.verifyToken("valid-token"))
                .willReturn(new FirebaseUserInfo("uid-1", "a@b.com", "이름"));
        given(userService.findByFirebaseUid("uid-1"))
                .willReturn(UserFixture.withId(1L));
        given(stationService.findAllStationsNameAsc()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/stations")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stations[0].id").value(1L))
                .andExpect(jsonPath("$.data.stations[0].name").value("강남"))
                .andExpect(jsonPath("$.data.stations[1].name").value("서울역"));
    }

    @Test
    void 토큰_없이_역_조회_401() throws Exception {
        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_TOKEN_MISSING"));
    }
}
