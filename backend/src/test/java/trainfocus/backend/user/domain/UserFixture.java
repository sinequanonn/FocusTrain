package trainfocus.backend.user.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static User of(Long id, String firebaseUid, String email, String nickname) {
        User user = User.createNewUser(firebaseUid, email, nickname);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static User withId(Long id) {
        return of(id, "uid-" + id, id + "@test.com", "테스터" + id);
    }
}
