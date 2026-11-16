package edu.itc.shop.api.error;

import edu.itc.shop.api.dto.Problem;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.UUID;

@Provider
public class ProblemMapper implements ExceptionMapper<Throwable> {
    private static final System.Logger LOGGER = System.getLogger("api");

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        if (ex instanceof WebApplicationException webException) {
            Response original = webException.getResponse();
            String title = original.getStatusInfo().getReasonPhrase();
            Problem problem = new Problem(
                    "about:blank",
                    title,
                    original.getStatus(),
                    webException.getMessage(),
                    uriInfo.getRequestUri().getPath(),
                    null);
            return Response.fromResponse(original)
                    .type(Problem.MEDIA_TYPE)
                    .entity(problem)
                    .build();
        }

        String reference = UUID.randomUUID().toString();
        LOGGER.log(System.Logger.Level.ERROR, "Unhandled exception reference " + reference, ex);
        Problem problem = new Problem(
                "about:blank",
                "Internal Server Error",
                500,
                "reference " + reference,
                uriInfo.getRequestUri().getPath(),
                null);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(Problem.MEDIA_TYPE)
                .entity(problem)
                .build();
    }
}
