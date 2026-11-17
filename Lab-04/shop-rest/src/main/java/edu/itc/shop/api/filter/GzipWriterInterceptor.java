package edu.itc.shop.api.filter;

import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

@Provider
@Compress 
public class GzipWriterInterceptor implements WriterInterceptor {
    
    @Context 
    HttpHeaders requestHeaders;

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException {
        String enc = requestHeaders.getHeaderString(HttpHeaders.ACCEPT_ENCODING);
        

        if (enc != null && enc.contains("gzip")) {

            ctx.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");

            ctx.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
            

            ctx.setOutputStream(new GZIPOutputStream(ctx.getOutputStream()));
        }
        

        ctx.proceed();
    }
}