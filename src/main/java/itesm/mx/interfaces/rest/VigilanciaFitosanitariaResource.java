package itesm.mx.interfaces.rest;

import itesm.mx.application.dto.CreateVigilanciaFitosanitariaDto;
import itesm.mx.application.dto.GetVigilanciaFitosanitariaResponseDto;
import itesm.mx.application.dto.UpdateVigilanciaFitosanitariaDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.vigilancia.CreateVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.DeleteVigilanciaFitosanitariaUseCase;
import itesm.mx.application.usecase.vigilancia.GetAllVigilanciasFitosanitariasUseCase;
import itesm.mx.application.usecase.vigilancia.GetVigilanciaFitosanitariaByIdUseCase;
import itesm.mx.application.usecase.vigilancia.UpdateVigilanciaFitosanitariaUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/vigilancias-fitosanitarias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VigilanciaFitosanitariaResource {

    private static final Integer ADMIN_ROLE_ID = 1;

    @Inject
    GetAllVigilanciasFitosanitariasUseCase getAllVigilanciasFitosanitariasUseCase;

    @Inject
    GetVigilanciaFitosanitariaByIdUseCase getVigilanciaFitosanitariaByIdUseCase;

    @Inject
    CreateVigilanciaFitosanitariaUseCase createVigilanciaFitosanitariaUseCase;

    @Inject
    UpdateVigilanciaFitosanitariaUseCase updateVigilanciaFitosanitariaUseCase;

    @Inject
    DeleteVigilanciaFitosanitariaUseCase deleteVigilanciaFitosanitariaUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @GET
    public Response getAllVigilanciasFitosanitarias() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            List<GetVigilanciaFitosanitariaResponseDto> vigilancias = getAllVigilanciasFitosanitariasUseCase.execute();
            return Response.ok(vigilancias).build();
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    @Path("/{id}")
    public Response getVigilanciaFitosanitariaById(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        try {
            GetVigilanciaFitosanitariaResponseDto vigilancia = getVigilanciaFitosanitariaByIdUseCase.execute(id);
            return Response.ok(vigilancia).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    public Response createVigilanciaFitosanitaria(@Valid CreateVigilanciaFitosanitariaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede crear vigilancias fitosanitarias");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            GetVigilanciaFitosanitariaResponseDto created = createVigilanciaFitosanitariaUseCase.execute(dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateVigilanciaFitosanitaria(@PathParam("id") Long id, @Valid UpdateVigilanciaFitosanitariaDto dto) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede actualizar vigilancias fitosanitarias");
        }
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            GetVigilanciaFitosanitariaResponseDto updated = updateVigilanciaFitosanitariaUseCase.execute(id, dto);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteVigilanciaFitosanitaria(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }
        if (!ADMIN_ROLE_ID.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede eliminar vigilancias fitosanitarias");
        }

        try {
            deleteVigilanciaFitosanitariaUseCase.execute(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}