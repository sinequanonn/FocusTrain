package trainfocus.backend.auth.firebase;

import com.google.firebase.auth.FirebaseToken;

public record FirebaseUserInfo(
        String uid,
        String email,
        String name
) {
    public static FirebaseUserInfo from(FirebaseToken token) {
        return new FirebaseUserInfo(
                token.getUid(),
                token.getEmail(),
                token.getName()
        );
    }
}
