package itesm.mx.infrastructure.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
@UnlessBuildProfile("test")
public class FirebaseUserManager {

    private static final Logger LOG = Logger.getLogger(FirebaseUserManager.class);

    /**
     * Creates a user in Firebase Auth with email and password.
     * 
     * @param email the user's email
     * @param password the user's password
     * @param displayName the user's display name (optional)
     * @return the Firebase UID of the created user
     * @throws FirebaseAuthException if user creation fails
     */
    public String createFirebaseUser(String email, String password, String displayName) throws FirebaseAuthException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(displayName);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            LOG.infof("Usuario creado en Firebase: %s con UID: %s", email, userRecord.getUid());
            return userRecord.getUid();
        } catch (FirebaseAuthException e) {
            LOG.errorf("Error al crear usuario en Firebase: %s", e.getMessage());
            throw e;
        }
    }

    /**
     * Generates a custom token for a Firebase user.
     * This token can be used to authenticate the user from the client.
     * 
     * @param uid the Firebase UID
     * @return a custom token valid for 1 hour
     * @throws FirebaseAuthException if token generation fails
     */
    public String generateCustomToken(String uid) throws FirebaseAuthException {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("UID is required");
        }

        try {
            String customToken = FirebaseAuth.getInstance().createCustomToken(uid);
            LOG.infof("Custom token generado para UID: %s", uid);
            return customToken;
        } catch (FirebaseAuthException e) {
            LOG.errorf("Error generando custom token para UID %s: %s", uid, e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a Firebase user by UID.
     *
     * @param uid Firebase UID
     * @throws FirebaseAuthException if deletion fails
     */
    public void deleteFirebaseUser(String uid) throws FirebaseAuthException {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("UID is required");
        }

        try {
            FirebaseAuth.getInstance().deleteUser(uid);
            LOG.infof("Usuario de Firebase eliminado para UID: %s", uid);
        } catch (FirebaseAuthException e) {
            LOG.errorf("Error eliminando usuario de Firebase para UID %s: %s", uid, e.getMessage());
            throw e;
        }
    }
}
