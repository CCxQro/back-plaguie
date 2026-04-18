package itesm.mx.infrastructure.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
@UnlessBuildProfile("test")
public class FirebaseTokenVerifier {

    /**
     * Verifies a Firebase ID token and returns the Firebase UUID.
     * 
     * @param idToken the Firebase ID token to verify
     * @return the Firebase UID extracted from the verified token
     * @throws FirebaseAuthException if token verification fails
     * @throws IllegalArgumentException if token is null or empty
     */
    public String verifyTokenAndGetUid(String idToken) throws FirebaseAuthException {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Firebase token cannot be null or empty");
        }

        FirebaseToken decodedToken = FirebaseAuth.getInstance()
                .verifyIdToken(idToken.trim(), true);
        
        return decodedToken.getUid();
    }
}
