package itesm.mx.infrastructure.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.io.FileInputStream;
import java.io.InputStream;


@Startup
@ApplicationScoped
@UnlessBuildProfile("test")
public class FirebaseConfig {

    private static final Logger LOG = Logger.getLogger(FirebaseConfig.class);

    @ConfigProperty(name = "firebase.service-account-location")
    String path;

    @PostConstruct
    void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                try (InputStream serviceAccount = new FileInputStream(path)) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                    LOG.info("Firebase inicializado");
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize Firebase", e);
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }
}
