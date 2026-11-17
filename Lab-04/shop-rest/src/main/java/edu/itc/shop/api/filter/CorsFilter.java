package edu.itc.shop.api.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

public class CorsFilter {

    @Provider
    @PreMatching
    public static class PreflightRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext ctx) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ctx.getMethod())) {
                // Abort the chain and answer immediately with 200 OK
                ctx.abortWith(Response.ok().build());
            }
        }
    }

    @Provider
    public static class ResponseHeadersFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext reqCtx, ContainerResponseContext resCtx) throws IOException {
            resCtx.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
            resCtx.getHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE");
            resCtx.getHeaders().add("Access-Control-Allow-Headers", "Content-Type,X-Api-Key,X-Correlation-Id,If-Match");
            resCtx.getHeaders().add("Expose-Headers", "Location,ETag,Link,X-Total-Count,X-Correlation-Id");
        }
    }
}