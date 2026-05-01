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

import java.util.List;

@Path("/api/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocationResource {

    @Inject
    GetAllLocationsUseCase getAllLocationsUseCase;

    @Inject
    RegisterLocationUseCase registerLocationUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
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

    private Response errorResponse(Response.Status status, String message) {
        return Response.status(status)
                .entity(new ErrorResponse(message))
                .build();
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
