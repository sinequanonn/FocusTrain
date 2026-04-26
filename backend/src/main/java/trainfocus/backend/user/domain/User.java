package trainfocus.backend.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trainfocus.backend.common.domain.BaseEntity;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 128, nullable = false)
    private String firebaseUid;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 50, nullable = false)
    private String nickname;

    private User(String firebaseUid, String email, String nickname) {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.nickname = nickname;
    }

    public static User createNewUser(String firebaseUid, String email, String nickname) {
        return new User(firebaseUid, email, nickname);
    }
}

