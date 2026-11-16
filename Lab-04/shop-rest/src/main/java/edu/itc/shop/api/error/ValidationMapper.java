package edu.itc.shop.api.error;

import edu.itc.shop.api.dto.Problem;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Provider
public class ValidationMapper implements ExceptionMapper<ConstraintViolationException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "field", lastNode(v.getPropertyPath()),
                        "message", v.getMessage()))
                .sorted(Comparator.comparing(error -> error.get("field")))
                .toList();
        Problem problem = new Problem(
                "about:blank",
                "Invalid request",
                400,
                "Validation failed",
                uriInfo.getRequestUri().getPath(),
                errors);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(Problem.MEDIA_TYPE)
                .entity(problem)
                .build();
    }

    static String lastNode(Path path) {
        String name = null;
        for (Path.Node node : path) {
            name = node.getName();
        }
        return name;
    }
}
