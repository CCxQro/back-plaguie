package itesm.mx.infrastructure.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.security.CurrentUser;
import itesm.mx.domain.models.User;
import itesm.mx.domain.repository.UserRepository;
import itesm.mx.infrastructure.firebase.FirebaseConfig;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import io.quarkus.arc.profile.UnlessBuildProfile;
import java.io.IOException;
import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
@UnlessBuildProfile("test")
public class FirebaseAuthFilter implements ContainerRequestFilter {
    @Inject
    UserRepository userRepository;
    @Inject
    AuthenticatedUserContext authenticatedUserContext;


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        if ("api/auth/login".equals(normalizedPath)) {
            return;
        }
        String authHeader = requestContext.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(
                    Response.status(401).build()
            );
            return;
        }
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(authHeader.replace("Bearer ", ""), true);
            Optional<User> userOptional = userRepository.findByFirebaseUuid(decodedToken.getUid());
            if (userOptional.isEmpty()) {
                requestContext.abortWith(
                        Response.status(401).build()
                );
                return;
            }
            User user = userOptional.get();
            CurrentUser currentUser = new CurrentUser(
                    user.getUserId(), user.getFirebaseUuid(), user.getName(), user.getEmail(), user.getRoleId()
            );
            authenticatedUserContext.setCurrentUser(currentUser);
        } catch (FirebaseAuthException e) {
            requestContext.abortWith(
                    Response.status(401).build()
            );
        }

    }
}
