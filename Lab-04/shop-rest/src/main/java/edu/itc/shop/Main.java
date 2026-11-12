package edu.itc.shop;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Grizzly bootstrap. Start with:  mvn -q compile exec:java   (stop with Ctrl+C)
 */
public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/";

    /** Shared by main() and the JerseyTest classes. Scans edu.itc.shop.api and its sub-packages. */
    public static ResourceConfig createApp() {
        return new ResourceConfig().packages("edu.itc.shop.api");
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), createApp());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        System.out.println("shop-rest running at " + BASE_URI + "  (Ctrl+C to stop)");
        Thread.currentThread().join();
    }
}
