package edu.itc.shop.api.filter;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

public class CorsFilter {

    @Provider
    @PreMatching
    public static class PreflightRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext ctx) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(ctx.getMethod())) {
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
            resCtx.getHeaders().add("Access-Control-Expose-Headers", "Location,ETag,Link,X-Total-Count,X-Correlation-Id");
        }
    }
}