package lk.thefurniturestore.middleware;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type("application/json")
                .entity("{\"status\":false,\"message\":\"An unexpected server error occurred. Please try again.\"}")
                .build();
    }
}
