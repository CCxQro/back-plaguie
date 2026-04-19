package itesm.mx.infrastructure.security;

import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.security.CurrentUser;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/ruta-protegida")
@Produces(MediaType.APPLICATION_JSON)
public class ProtectedTestResource {

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getProtected() {
        CurrentUser currentUser = authenticatedUserContext.getCurrentUser();
        if (currentUser == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(new ProtectedResponse(currentUser.getFirebaseUuid(), currentUser.getEmail())).build();
    }

    public static class ProtectedResponse {
        public String firebaseUuid;
        public String email;

        public ProtectedResponse(String firebaseUuid, String email) {
            this.firebaseUuid = firebaseUuid;
            this.email = email;
        }
    }
}
