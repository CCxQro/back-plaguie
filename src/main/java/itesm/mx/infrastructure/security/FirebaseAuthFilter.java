package itesm.mx.infrastructure.security;

import com.google.firebase.auth.FirebaseAuthException;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.security.CurrentUser;
import itesm.mx.domain.models.user.User;
import itesm.mx.domain.repository.user.UserRepository;
import itesm.mx.infrastructure.firebase.FirebaseTokenVerifier;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthFilter implements ContainerRequestFilter {
    @Inject
    UserRepository userRepository;
    @Inject
    AuthenticatedUserContext authenticatedUserContext;
    @Inject
    FirebaseTokenVerifier firebaseTokenVerifier;


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }
        
        String path = requestContext.getUriInfo().getPath();
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        if ("api/auth/login".equals(normalizedPath) || "api/auth/signup".equals(normalizedPath) || "api/status".equals(normalizedPath)) {
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
            String token = authHeader.substring(7);
            String uid = firebaseTokenVerifier.verifyTokenAndGetUid(token);
            Optional<User> userOptional = userRepository.findByFirebaseUuid(uid);
            if (userOptional.isEmpty()) {
                requestContext.abortWith(
                        Response.status(401).build()
                );
                return;
            }
            User user = userOptional.get();
            if (Boolean.FALSE.equals(user.getActive())) {
                requestContext.abortWith(Response.status(403).build());
                return;
            }
            CurrentUser currentUser = new CurrentUser(
                    user.getUserId(), user.getFirebaseUuid(), user.getName(), user.getEmail(), user.getRoleId()
            );
            authenticatedUserContext.setCurrentUser(currentUser);
        } catch (FirebaseAuthException | IllegalArgumentException e) {
            requestContext.abortWith(
                    Response.status(401).build()
            );
        }

    }
}
