package com.movieticket;

import com.movieticket.server.MovieWebServer;
import com.movieticket.storage.DataStore;

public class Main {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("************************************************************");
        System.out.println("*                                                          *");
        System.out.println("*     🎬  CINESHOW - MOVIE TICKET BOOKING SYSTEM  🎬       *");
        System.out.println("*            (College Micro-Project - 2nd Year)            *");
        System.out.println("*                                                          *");
        System.out.println("************************************************************");

        // Initialize and load database
        DataStore.getInstance();

        try {
            MovieWebServer webServer = new MovieWebServer(port);
            webServer.start();

            System.out.println("\n🚀 Application started successfully!");
            System.out.println("👉 Access the website in your browser:");
            System.out.println("   >>> http://localhost:" + port + " <<<\n");
            System.out.println("🔑 Demo Login Credentials (or register a new user):");
            System.out.println("   Email:    demo@example.com");
            System.out.println("   Password: password123\n");
            System.out.println("Press Ctrl + C in this terminal to stop the server.\n");

            // Add graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Server] Shutting down CineShow server...");
                webServer.stop();
                DataStore.getInstance().save();
                System.out.println("[Server] Goodbye!");
            }));

        } catch (Exception e) {
            System.err.println("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

