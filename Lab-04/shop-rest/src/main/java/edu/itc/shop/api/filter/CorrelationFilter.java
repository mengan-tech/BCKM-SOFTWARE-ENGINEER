package edu.itc.shop.api.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class CorrelationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final System.Logger LOG = System.getLogger("ShopApi");
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String ID_PROPERTY = "correlationId";
    private static final String START_PROPERTY = "startTime";

    @Override
    public void filter(ContainerRequestContext reqCtx) throws IOException {
        reqCtx.setProperty(ID_PROPERTY, idFrom(reqCtx));
        reqCtx.setProperty(START_PROPERTY, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext reqCtx, ContainerResponseContext resCtx) throws IOException {
        String correlationId = (String) reqCtx.getProperty(ID_PROPERTY);
        if (correlationId == null) {
            correlationId = idFrom(reqCtx);
        }
        resCtx.getHeaders().putSingle(CORRELATION_HEADER, correlationId);

        Long startTime = (Long) reqCtx.getProperty(START_PROPERTY);
        String duration = startTime == null
                ? "-"
                : Long.toString(System.currentTimeMillis() - startTime);

        LOG.log(System.Logger.Level.INFO,
                String.format("%-4s %s -> %d [%s] %s ms",
                        reqCtx.getMethod(),
                        reqCtx.getUriInfo().getRequestUri().getPath(),
                        resCtx.getStatus(),
                        correlationId,
                        duration));
    }

    private static String idFrom(ContainerRequestContext reqCtx) {
        String id = reqCtx.getHeaderString(CORRELATION_HEADER);
        return (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
    }
}