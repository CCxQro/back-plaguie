package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.dto.RegisterLocationDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.location.location.GetAllLocationsUseCase;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Locations", description = "Location catalog and geospatial endpoints")
public class LocationResource {

    @Inject
    GetAllLocationsUseCase getAllLocationsUseCase;

    @Inject
    RegisterLocationUseCase registerLocationUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Operation(summary = "List locations", description = "Returns all registered locations for authenticated users.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Locations returned", content = @Content(schema = @Schema(implementation = GetLocationResponseDto[].class))),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllLocations() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        try {
            List<GetLocationResponseDto> locations = getAllLocationsUseCase.execute();
            return Response.ok(locations).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Operation(summary = "Register location", description = "Creates or reuses a location record for authenticated users.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RegisterLocationDto.class)))
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Location created", content = @Content(schema = @Schema(implementation = GetLocationResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Invalid or missing request body"),
            @APIResponse(responseCode = "401", description = "Authentication required"),
            @APIResponse(responseCode = "409", description = "Location already exists or business conflict"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response registerLocation(RegisterLocationDto registerLocationDto) {
        if (registerLocationDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }

        try {
            GetLocationResponseDto response = registerLocationUseCase.execute(
                    LocationDtoMapper.toLocationData(registerLocationDto)
            );
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
