package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.GetLocationResponseDto;
import itesm.mx.application.mapper.location.LocationDtoMapper;
import itesm.mx.application.usecase.location.location.RegisterLocationUseCase;
import itesm.mx.application.usecase.users.subUsers.RegisterAdministratorUseCase;
import itesm.mx.application.usecase.users.subUsers.RegisterFarmerUseCase;
import itesm.mx.application.usecase.users.subUsers.RegisterTechnicalSellerUseCase;
import itesm.mx.domain.models.location.Location;
import itesm.mx.domain.models.user.Administrator;
import itesm.mx.domain.models.user.Farmer;
import itesm.mx.domain.models.user.RoleConstants;
import itesm.mx.domain.models.user.TechnicalSeller;
import itesm.mx.domain.models.user.User;
import itesm.mx.application.dto.RegisterUserDto;
import itesm.mx.application.dto.RegisterUserResponseDto;
import itesm.mx.application.dto.SignupDto;
import itesm.mx.application.security.AuthenticatedUserContext;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.application.usecase.users.LoginUseCase;
import itesm.mx.application.usecase.users.RegisterUserUseCase;

import static itesm.mx.interfaces.rest.utils.ErrorResponseUtils.errorResponse;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    LoginUseCase loginUseCase;

    @Inject
    RegisterUserUseCase registerUserUseCase;

    @Inject
    RegisterAdministratorUseCase registerAdministratorUseCase;

    @Inject
    RegisterFarmerUseCase registerFarmerUseCase;

    @Inject
    RegisterTechnicalSellerUseCase registerTechnicalSellerUseCase;

    @Inject
    RegisterLocationUseCase registerLocationUseCase;

    @Inject
    AuthenticatedUserContext authenticatedUserContext;

    @POST
    @Path("/login")
    public Response login(LoginDto loginDto) {
        if (loginDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        try {
            LoginResponseDto response = loginUseCase.execute(loginDto);
            return Response.ok(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            return errorResponse(Response.Status.UNAUTHORIZED, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Path("/register")
    public Response register(RegisterUserDto registerUserDto) {
        if (registerUserDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        if (authenticatedUserContext.getCurrentUser() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Se requiere autenticación");
        }

        if (!RoleConstants.ADMIN.equals(authenticatedUserContext.getCurrentUser().getRoleId())) {
            return errorResponse(Response.Status.FORBIDDEN, "Solo un administrador puede registrar usuarios");
        }

        if (!RoleConstants.ADMIN.equals(registerUserDto.roleId) && registerUserDto.location == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "Se requiere la ubicación para este rol");
        }

        try {
            RegisterUserResponseDto response = registerUserUseCase.execute(registerUserDto);

            User createdUser = new User(response.userId, response.firebaseUuid, response.name, response.email, response.roleId, true);

            if (RoleConstants.ADMIN.equals(response.roleId)) {
                registerAdministratorUseCase.execute(new Administrator(null, createdUser, true));
                response.isActive = true;
            } else {
                GetLocationResponseDto locationResponse = registerLocationUseCase.execute(
                        LocationDtoMapper.toLocationData(registerUserDto.location)
                );
                Location location = new Location();
                location.setLocationId(locationResponse.locationId);

                if (RoleConstants.SELLER.equals(response.roleId)) {
                    registerTechnicalSellerUseCase.execute(new TechnicalSeller(null, createdUser, location, true));
                } else if (RoleConstants.FARMER.equals(response.roleId)) {
                    registerFarmerUseCase.execute(new Farmer(null, createdUser, location, true));
                }

                response.isActive = true;
                response.location = locationResponse;
            }

            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (SecurityException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @POST
    @Path("/signup")
    public Response signup(SignupDto signupDto) {
        if (signupDto == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "El cuerpo de la solicitud es requerido");
        }

        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.name = signupDto.name;
        registerUserDto.email = signupDto.email;
        registerUserDto.password = signupDto.password;
        registerUserDto.roleId = RoleConstants.SELLER;

        try {
            RegisterUserResponseDto response = registerUserUseCase.execute(registerUserDto);
            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (SecurityException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }
}
