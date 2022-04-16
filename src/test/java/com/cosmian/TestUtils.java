package com.cosmian;

import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class TestUtils {

    public static void initLogging() {
        final Logger logger = Logger.getLogger("com.cosmian");
        logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.fine("Logger was setup");
    }

    public static String kmsServerUrl() {
        String v = System.getenv("COSMIAN_SERVER_URL");
        if (v == null) {
            return "http://localhost:9998";
        }
        return v;
    }

    public static Optional<String> apiKey() {
        String v = System.getenv("COSMIAN_API_KEY");
        if (v == null) {
            return Optional.empty();
        }
        return Optional.of(v);
    }

    public static boolean serverAvailable(String kmsServerUrl) {
        try {
            new RestClient(kmsServerUrl, Optional.empty()).json_get("/");

            return true;
        } catch (RestException e) {
            if (e.getMessage().contains("404")) {
                return true;
            }
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }
    }

}
