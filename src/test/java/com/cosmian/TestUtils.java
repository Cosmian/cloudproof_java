package com.cosmian;

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
        handler.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.fine("Logger was setup");
    }

    public static String cosmianServerUrl() {
        String v = System.getenv("COSMIAN_SERVER_URL");
        if (v == null) {
            return "http://localhost:9999";
        }
        return v;
    }

    public static String apiKey() {
        String v = System.getenv("COSMIAN_API_KEY");
        if (v == null) {
            return "";
        }
        return v;
    }
}
