package itesm.mx.interfaces.rest.admin;

import itesm.mx.application.dto.admin.AdminMetricsDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.admin.GetAdminMetricsUseCase;
import itesm.mx.domain.models.user.RoleConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/admin/metrics")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Admin Metrics", description = "Endpoints for admin dashboard metrics")
public class AdminMetricsResource {

    @Inject
    GetAdminMetricsUseCase getAdminMetricsUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "Get admin metrics", description = "Returns counts and trends across users, products, surveillance records, and orders. Admin only.")
    @APIResponse(
        responseCode = "200",
        description = "Metrics successfully retrieved",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AdminMetricsDto.class))
    )
    @APIResponse(responseCode = "401", description = "Authentication required")
    @APIResponse(responseCode = "403", description = "Admin role required")
    @SecurityRequirement(name = "jwt")
    public Response getMetrics() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo administradores pueden ver las metricas");
        }

        AdminMetricsDto metrics = getAdminMetricsUseCase.execute();
        return Response.ok(metrics).build();
    }
}
