package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetCategoryResponseDto;
import itesm.mx.application.dto.RegisterCategoryDto;
import itesm.mx.application.dto.UpdateCategoryDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.usecase.marketplace.category.*;
import itesm.mx.domain.models.marketplace.Category;
import itesm.mx.domain.models.marketplace.Color;
import itesm.mx.domain.models.marketplace.Status;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.User;

import java.util.List;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private static final Long STATUS_ACCEPTED = 1L;
    private static final Long STATUS_REVISION = 2L;

    @Inject RegisterCategoryUseCase registerCategoryUseCase;
    @Inject UpdateCategoryUseCase updateCategoryUseCase;
    @Inject DeleteCategoryUseCase deleteCategoryUseCase;
    @Inject GetAllCategoriesUseCase getAllCategoriesUseCase;
    @Inject GetCategoriesByStatusUseCase getCategoriesByStatusUseCase;
    @Inject GetCategoryByIdUseCase getCategoryByIdUseCase;
    @Inject GetCategoriesByUserUseCase getCategoriesByUserUseCase;
    @Inject AuthenticatedUserContext authenticatedUserContext;

    @GET
    @Path("/me")
    public Response getMyCatgories() {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar las categorias");
        }

        try {
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
            List<GetCategoryResponseDto> categories = getCategoriesByUserUseCase.execute(currentUserId)
                    .stream()
                    .map(this::toResponseDto)
                    .toList();
            return Response.ok(categories).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @GET
    public Response getAllCategories(@QueryParam("statusId") Long statusId) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede consultar las categorias");
        }

        try {
            List<GetCategoryResponseDto> categories = (statusId != null
                    ? getCategoriesByStatusUseCase.execute(statusId)
                    : getAllCategoriesUseCase.execute())
                    .stream()
                    .map(this::toResponseDto)
                    .toList();
            return Response.ok(categories).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    public Response registerCategory(RegisterCategoryDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede registrar categorias");
        }

        try {
            Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
            Long statusId = RoleConstants.ADMIN.equals(role) ? STATUS_ACCEPTED : STATUS_REVISION;

            User user = new User();
            user.setUserId(currentUserId);

            Color color = new Color();
            color.setColorId(dto.colorId);

            Status status = new Status();
            status.setStatusId(statusId);

            Category created = registerCategoryUseCase.execute(new Category(null, user, dto.name, color, status));
            return Response.status(Response.Status.CREATED).entity(toResponseDto(created)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateCategory(@PathParam("id") Long id, UpdateCategoryDto dto) {
        if (dto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede actualizar categorias");
        }

        try {
            Category existing = getCategoryByIdUseCase.execute(id)
                    .orElse(null);
            if (existing == null) {
                return errorResponse(Response.Status.NOT_FOUND, "Categoria no encontrada");
            }

            if (!RoleConstants.ADMIN.equals(role)) {
                Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
                boolean isOwner = existing.getUser().getUserId().equals(currentUserId);
                boolean isAccepted = STATUS_ACCEPTED.equals(existing.getStatus().getStatusId());
                if (!isOwner || isAccepted) {
                    return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para actualizar esta categoria");
                }
            }

            Color color = new Color();
            color.setColorId(dto.colorId);

            Category updated = updateCategoryUseCase.execute(id,
                    new Category(null, existing.getUser(), dto.name, color, existing.getStatus()));
            return Response.ok(toResponseDto(updated)).build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCategory(@PathParam("id") Long id) {
        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticacion");
        }
        Integer role = authenticatedUserContext.getCurrentUser().getRoleId();
        if (!RoleConstants.ADMIN.equals(role) && !RoleConstants.SELLER.equals(role)) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador o tecnico vendedor puede eliminar categorias");
        }

        try {
            Category existing = getCategoryByIdUseCase.execute(id)
                    .orElse(null);
            if (existing == null) {
                return errorResponse(Response.Status.NOT_FOUND, "Categoria no encontrada");
            }

            if (!RoleConstants.ADMIN.equals(role)) {
                Long currentUserId = authenticatedUserContext.getCurrentUser().getUserId();
                boolean isOwner = existing.getUser().getUserId().equals(currentUserId);
                boolean isAccepted = STATUS_ACCEPTED.equals(existing.getStatus().getStatusId());
                if (!isOwner || isAccepted) {
                    return errorResponse(Response.Status.FORBIDDEN, "No tienes permiso para eliminar esta categoria");
                }
            }

            deleteCategoryUseCase.execute(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private GetCategoryResponseDto toResponseDto(Category category) {
        return new GetCategoryResponseDto(
                category.getCategoryId(),
                category.getName(),
                category.getUser().getUserId(),
                category.getColor().getColorId(),
                category.getColor().getName(),
                category.getColor().getHexa(),
                category.getStatus().getStatusId(),
                category.getStatus().getName()
        );
    }
}
