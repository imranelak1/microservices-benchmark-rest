package ma.imrane.benchmark.web;
import jakarta.ws.rs.ext.*;
import jakarta.ws.rs.core.Response;
import org.springframework.web.server.ResponseStatusException;
@Provider
public class RestErrorMapper implements ExceptionMapper<ResponseStatusException> {
    public Response toResponse(ResponseStatusException error) {return Response.status(error.getStatusCode().value()).entity(java.util.Map.of("error",error.getReason())).build();}
}
