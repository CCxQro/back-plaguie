package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.StatusResponseDto;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.OffsetDateTime;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/status")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Status", description = "Deployment health and uptime endpoint")
public class StatusResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Operation(summary = "Check application status", description = "Returns whether the service and its database are reachable.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Service is healthy", content = @Content(schema = @Schema(implementation = StatusResponseDto.class))),
            @APIResponse(responseCode = "503", description = "Service or database is unavailable", content = @Content(schema = @Schema(implementation = StatusResponseDto.class)))
    })
    public Response getStatus() {
        boolean databaseUp = isDatabaseReachable();
        String status = databaseUp ? "UP" : "DOWN";
        StatusResponseDto response = new StatusResponseDto(
                status,
                databaseUp ? "UP" : "DOWN",
                "back-plaguie",
                OffsetDateTime.now().toString()
        );

        if (databaseUp) {
            return Response.ok(response).build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(response).build();
    }

    private boolean isDatabaseReachable() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}