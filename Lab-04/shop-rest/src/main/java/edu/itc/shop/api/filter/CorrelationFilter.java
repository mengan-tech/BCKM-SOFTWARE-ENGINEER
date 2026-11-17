package edu.itc.shop.api.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(Priorities.USER)
public class CorrelationFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final System.Logger LOG = System.getLogger("ShopApi");
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public void filter(ContainerRequestContext reqCtx) throws IOException {

        String correlationId = reqCtx.getHeaderString(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        

        reqCtx.setProperty("correlationId", correlationId);
        reqCtx.setProperty("startTime", System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext reqCtx, ContainerResponseContext resCtx) throws IOException {

        String correlationId = (String) reqCtx.getProperty("correlationId");
        Long startTime = (Long) reqCtx.getProperty("startTime");

        if (correlationId != null) {
            resCtx.getHeaders().add(CORRELATION_HEADER, correlationId);
        }
        

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            LOG.log(System.Logger.Level.INFO, 
                String.format("%s %s -> %d [%s] %d ms",
                    reqCtx.getMethod(),
                    reqCtx.getUriInfo().getPath(),
                    resCtx.getStatus(),
                    correlationId,
                    duration));
        }
    }
}