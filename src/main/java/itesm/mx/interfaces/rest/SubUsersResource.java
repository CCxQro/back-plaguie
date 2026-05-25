package itesm.mx.interfaces.rest;

import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.users.subUsers.GetAdministratorByUserIdUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllAdministratorsUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllFarmersUseCase;
import itesm.mx.application.usecase.users.subUsers.GetAllTechnicalSellersUseCase;
import itesm.mx.application.usecase.users.subUsers.GetFarmerByUserIdUseCase;
import itesm.mx.application.usecase.users.subUsers.GetTechnicalSellerByUserIdUseCase;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.TechnicalSeller;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
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
    GetFarmerByUserIdUseCase getFarmerByUserIdUseCase;

    @Inject
    GetTechnicalSellerByUserIdUseCase getTechnicalSellerByUserIdUseCase;

    @Inject
    GetAdministratorByUserIdUseCase getAdministratorByUserIdUseCase;

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

    @GET
    @Path("/farmers/{userId}")
    @Operation(summary = "Get farmer by user id", description = "Returns the farmer record for the given user id. Accessible by administrators or by users whose role matches the endpoint role (farmer).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Farmer returned", content = @Content(schema = @Schema(implementation = Farmer.class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Role not allowed"),
            @APIResponse(responseCode = "404", description = "Farmer not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getFarmerByUserId(@PathParam("userId") Integer userId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        Integer currentRoleId = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!ADMIN_ROLE_ID.equals(currentRoleId) && !RoleConstants.FARMER.equals(currentRoleId)) {
            return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para consultar agricultores");
        }

        try {
            Optional<Farmer> farmer = getFarmerByUserIdUseCase.execute(userId.longValue());
            if (farmer.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Agricultor no encontrado");
            }
            return Response.ok(farmer.get()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/technical-sellers/{userId}")
    @Operation(summary = "Get technical seller by user id", description = "Returns the technical seller record for the given user id. Accessible by administrators or by users whose role matches the endpoint role (seller).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Technical seller returned", content = @Content(schema = @Schema(implementation = TechnicalSeller.class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Role not allowed"),
            @APIResponse(responseCode = "404", description = "Technical seller not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getTechnicalSellerByUserId(@PathParam("userId") Integer userId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        Integer currentRoleId = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!ADMIN_ROLE_ID.equals(currentRoleId) && !RoleConstants.SELLER.equals(currentRoleId)) {
            return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para consultar técnicos vendedores");
        }

        try {
            Optional<TechnicalSeller> seller = getTechnicalSellerByUserIdUseCase.execute(userId.longValue());
            if (seller.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Técnico vendedor no encontrado");
            }
            return Response.ok(seller.get()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/administrators/{userId}")
    @Operation(summary = "Get administrator by user id", description = "Returns the administrator record for the given user id. Admin-only endpoint.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Administrator returned", content = @Content(schema = @Schema(implementation = Administrator.class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "403", description = "Admin role required"),
            @APIResponse(responseCode = "404", description = "Administrator not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAdministratorByUserId(@PathParam("userId") Integer userId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede consultar administradores");
        }

        try {
            Optional<Administrator> administrator = getAdministratorByUserIdUseCase.execute(userId.longValue());
            if (administrator.isEmpty()) {
                return errorResponse(Response.Status.NOT_FOUND, "Administrador no encontrado");
            }
            return Response.ok(administrator.get()).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}