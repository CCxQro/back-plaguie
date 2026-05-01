package itesm.mx.interfaces.rest.utils;

import jakarta.ws.rs.core.Response;

public final class ErrorResponseUtils {

    private ErrorResponseUtils() {
    }

    public static Response errorResponse(Response.Status status, String message) {
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
