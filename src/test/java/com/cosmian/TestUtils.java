package com.cosmian;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.cosmian.utils.RestClient;
import com.cosmian.utils.RestException;

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
            return Optional.of(
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlRWdk5xTEtoUHhUSGdhYUNGRGRoSSJ9.eyJnaXZlbl9uYW1lIjoiTGFldGl0aWEiLCJmYW1pbHlfbmFtZSI6Ikxhbmdsb2lzIiwibmlja25hbWUiOiJsYWV0aXRpYS5sYW5nbG9pcyIsIm5hbWUiOiJMYWV0aXRpYSBMYW5nbG9pcyIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQVRYQUp5UEJsSnpqRzNuMWZLLXNyS0ptdUVkYklUX29QRmhVbTd2T2dVWD1zOTYtYyIsImxvY2FsZSI6ImZyIiwidXBkYXRlZF9hdCI6IjIwMjEtMTItMjFUMDk6MjE6NDkuMDgxWiIsImVtYWlsIjoibGFldGl0aWEubGFuZ2xvaXNAY29zbWlhbi5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6Ly9kZXYtMW1ic2JtaW4udXMuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTA4NTgwMDU3NDAwMjkxNDc5ODQyIiwiYXVkIjoiYUZqSzJvTnkwR1RnNWphV3JNYkJBZzV0bjRIV3VJN1ciLCJpYXQiOjE2NDAwNzg1MTQsImV4cCI6MTY0MDExNDUxNCwibm9uY2UiOiJha0poV2xoMlRsTm1lRTVtVFc0NFJHSk5VVEl5WW14aVJUTnVRblV1VEVwa2RrTnFVa2R5WkdoWFdnPT0ifQ.Q4tCzvJTNxmDhIYOJbjsqupdQkWg29Ny0B8njEfSrLVXNaRMFE99eSXedCBaXSMBnZ9GuCV2Z1MAZL8ZjTxqPP_VYCnc2QufG1k1XZg--6Q48pPdpUBXu2Ny1eatwiDrRvgQfUHkiM8thUAOb4bXxGLrtQKlO_ePOehDbEOjfd11aVm3pwyVqj1v6Ki1D5QJsOHtkkpLMinmmyGDtmdHH2YXseZNHGUY7PWZ6DelpJaxI48W5FNDY4b0sJlzaJqdIcoOX7EeP1pfFoHVeZAo5mWyuDev2OaPYKeqpga4PjqHcFT0m1rQoWQHmfGr3EkA3w8NXmKnZmEbQcLLgcCATw");
            // return Optional.empty();
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
            if (e.getMessage().contains("401")) {
                return true;
            }
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }
    }

    public static boolean portAvailable(int port) {
        System.out.println("--------------Testing port " + port);
        Socket s = null;
        try {
            s = new Socket("localhost", port);
            // If the code makes it this far without an exception it means
            // something is using the port and has responded.
            System.out.println("--------------Port " + port + " is not available");
            return false;
        } catch (IOException e) {
            System.out.println("--------------Port " + port + " is available");
            return true;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error not handled.", e);
                }
            }
        }
    }

    public static String redisHostname() {
        String v = System.getenv("REDIS_HOSTNAME");
        if (v == null) {
            return "localhost";
        }
        return v;
    }

    public static int redisPort() {
        String v = System.getenv("REDIS_PORT");
        if (v == null) {
            return 6379;
        }
        return Integer.parseInt(v);
    }

    public static String redisPassword() {
        String v = System.getenv("REDIS_PASSWORD");
        return v;
    }
}
