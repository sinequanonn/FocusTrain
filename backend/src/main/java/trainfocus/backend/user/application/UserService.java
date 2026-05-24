package trainfocus.backend.user.application;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trainfocus.backend.auth.application.dto.MeResponse;
import trainfocus.backend.auth.firebase.FirebaseUserInfo;
import trainfocus.backend.common.exception.BusinessException;
import trainfocus.backend.common.exception.ErrorCode;
import trainfocus.backend.station.domain.Station;
import trainfocus.backend.station.domain.repository.StationRepository;
import trainfocus.backend.user.application.dto.UpdateDepartureStationRequest;
import trainfocus.backend.user.application.dto.UpdateNicknameRequest;
import trainfocus.backend.user.domain.User;
import trainfocus.backend.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;

    @Transactional
    public User findOrCreateUser(FirebaseUserInfo firebaseUserInfo) {
        return userRepository.findByFirebaseUid(firebaseUserInfo.uid())
                .orElseGet(() -> {
                    User newUser = User.createNewUser(
                            firebaseUserInfo.uid(),
                            firebaseUserInfo.email(),
                            firebaseUserInfo.name()
                    );
                    return userRepository.save(newUser);
                });
    }

    public User findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public MeResponse updateNickname(UpdateNicknameRequest request, User user) {
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }
        user.updateNickname(request.nickname());
        return MeResponse.from(user);
    }

    @Transactional
    public MeResponse updateDepartureStation(UpdateDepartureStationRequest request, User user) {
        Station station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));
        user.updateDepartureStation(station);
        return MeResponse.from(user);
    }
}
