package itesm.mx.interfaces.rest;

import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.users.subUsers.GetAllAdministratorsUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllFarmersUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllTechnicalSellersUseCase;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.TechnicalSeller;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sub-users", description = "Role-specific user listings")
public class SubUsersResource {

    private static final Integer ADMIN_ROLE_ID = 1;

    @Inject
    GetAllFarmersUseCase getAllFarmersUseCase;

    @Inject
    GetAllTechnicalSellersUseCase getAllTechnicalSellersUseCase;

    @Inject
    GetAllAdministratorsUseCase getAllAdministratorsUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/farmers")
    @Operation(summary = "List farmers", description = "Returns all farmer records. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Farmers returned", content = @Content(schema = @Schema(implementation = Farmer[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllFarmers() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede listar agricultores");
        }

        try {
            List<Farmer> farmers = getAllFarmersUseCase.execute();
            return Response.ok(farmers).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/technical-sellers")
    @Operation(summary = "List technical sellers", description = "Returns all technical seller records. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Technical sellers returned", content = @Content(schema = @Schema(implementation = TechnicalSeller[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllTechnicalSellers() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede listar técnicos vendedores");
        }

        try {
            List<TechnicalSeller> sellers = getAllTechnicalSellersUseCase.execute();
            return Response.ok(sellers).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/administrators")
    @Operation(summary = "List administrators", description = "Returns all administrator records. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Administrators returned", content = @Content(schema = @Schema(implementation = Administrator[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllAdministrators() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede listar administradores");
        }

        try {
            List<Administrator> administrators = getAllAdministratorsUseCase.execute();
            return Response.ok(administrators).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
