package itesm.mx.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import itesm.mx.application.dto.LoginDto;
import itesm.mx.application.dto.LoginResponseDto;
import itesm.mx.application.usecase.LoginUseCase;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    LoginUseCase loginUseCase;

    @POST
    @Path("/login")
    public Response login(LoginDto loginDto) {
        try {
            LoginResponseDto response = loginUseCase.execute(loginDto);
            return Response.ok(response).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}