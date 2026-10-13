package com.movieticket.server;

import com.movieticket.model.*;
import com.movieticket.storage.DataStore;
import com.movieticket.util.SimpleJson;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

public class MovieWebServer {
    private final int port;
    private HttpServer server;
    private final DataStore dataStore;

    public MovieWebServer(int port) {
        this.port = port;
        this.dataStore = DataStore.getInstance();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // API routes
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/movies", new MoviesHandler());
        server.createContext("/api/showtimes", new ShowtimeHandler());
        server.createContext("/api/bookings", new BookingsHandler());

        // Static files handler (serves HTML, CSS, JS, etc.)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("=================================================");
        System.out.println("  CineShow - Movie Ticket Booking Web Server");
        System.out.println("=================================================");
        System.out.println("  Server is RUNNING at: http://localhost:" + port);
        System.out.println("  Open your browser and visit: http://localhost:" + port);
        System.out.println("=================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // -----------------------------------------------------------
    // Auth Handlers
    // -----------------------------------------------------------
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpHandlerHelper.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpHandlerHelper.sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                String body = HttpHandlerHelper.readRequestBody(exchange);
                Map<String, Object> req = SimpleJson.parseObject(body);
                String name = (String) req.get("name");
                String email = (String) req.get("email");
                String password = (String) req.get("password");
                String phone = (String) req.get("phone");

                if (name == null || name.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                    HttpHandlerHelper.sendError(exchange, 400, "Name, email, and password are required.");
                    return;
                }

                User user = dataStore.registerUser(name.trim(), email.trim(), password.trim(), phone != null ? phone.trim() : "");
                if (user == null) {
                    HttpHandlerHelper.sendError(exchange, 409, "An account with this email already exists.");
                    return;
                }

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "Registration successful!");
                resp.put("user", user.toMap(false));
                HttpHandlerHelper.sendJsonResponse(exchange, 201, resp);
            } catch (Exception e) {
                HttpHandlerHelper.sendError(exchange, 500, "Registration failed: " + e.getMessage());
            }
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpHandlerHelper.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpHandlerHelper.sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                String body = HttpHandlerHelper.readRequestBody(exchange);
                Map<String, Object> req = SimpleJson.parseObject(body);
                String email = (String) req.get("email");
                String password = (String) req.get("password");

                if (email == null || password == null) {
                    HttpHandlerHelper.sendError(exchange, 400, "Email and password are required.");
                    return;
                }

                User user = dataStore.authenticate(email.trim(), password.trim());
                if (user == null) {
                    HttpHandlerHelper.sendError(exchange, 401, "Invalid email or password.");
                    return;
                }

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("message", "Login successful!");
                resp.put("user", user.toMap(false));
                HttpHandlerHelper.sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                HttpHandlerHelper.sendError(exchange, 500, "Login failed: " + e.getMessage());
            }
        }
    }

    // -----------------------------------------------------------
    // Movie & Showtime Handlers
    // -----------------------------------------------------------
    private class MoviesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpHandlerHelper.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpHandlerHelper.sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath(); // e.g., /api/movies or /api/movies/mov-1 or /api/movies/mov-1/showtimes
            String[] parts = path.split("/");

            // Case 1: GET /api/movies/{id}/showtimes
            if (parts.length >= 5 && "showtimes".equalsIgnoreCase(parts[4])) {
                String movieId = parts[3];
                Map<String, String> query = HttpHandlerHelper.parseQueryParams(exchange);
                String date = query.get("date");

                List<Showtime> showtimes = dataStore.getShowtimesByMovieAndDate(movieId, date);
                List<Map<String, Object>> result = new ArrayList<>();
                for (Showtime s : showtimes) {
                    result.add(s.toMap());
                }
                HttpHandlerHelper.sendJsonResponse(exchange, 200, result);
                return;
            }

            // Case 2: GET /api/movies/{id}
            if (parts.length >= 4 && !parts[3].isEmpty()) {
                String movieId = parts[3];
                Movie movie = dataStore.getMovieById(movieId);
                if (movie == null) {
                    HttpHandlerHelper.sendError(exchange, 404, "Movie not found");
                    return;
                }
                HttpHandlerHelper.sendJsonResponse(exchange, 200, movie.toMap());
                return;
            }

            // Case 3: GET /api/movies (list with search and filter)
            Map<String, String> query = HttpHandlerHelper.parseQueryParams(exchange);
            String search = query.get("q");
            String genre = query.get("genre");
            String language = query.get("language");

            List<Movie> all = dataStore.getAllMovies();
            List<Map<String, Object>> filtered = new ArrayList<>();

            for (Movie m : all) {
                boolean match = true;
                if (search != null && !search.trim().isEmpty()) {
                    String s = search.toLowerCase();
                    if (!m.getTitle().toLowerCase().contains(s) &&
                        !m.getGenre().toLowerCase().contains(s) &&
                        !m.getCast().toLowerCase().contains(s)) {
                        match = false;
                    }
                }
                if (match && genre != null && !genre.trim().isEmpty() && !genre.equalsIgnoreCase("All")) {
                    if (!m.getGenre().toLowerCase().contains(genre.toLowerCase())) {
                        match = false;
                    }
                }
                if (match && language != null && !language.trim().isEmpty() && !language.equalsIgnoreCase("All")) {
                    if (!m.getLanguage().toLowerCase().contains(language.toLowerCase())) {
                        match = false;
                    }
                }
                if (match) {
                    filtered.add(m.toMap());
                }
            }

            HttpHandlerHelper.sendJsonResponse(exchange, 200, filtered);
        }
    }

    private class ShowtimeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpHandlerHelper.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpHandlerHelper.sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length >= 4 && !parts[3].isEmpty()) {
                String showId = parts[3];
                Showtime s = dataStore.getShowtimeById(showId);
                if (s == null) {
                    HttpHandlerHelper.sendError(exchange, 404, "Showtime not found");
                    return;
                }
                HttpHandlerHelper.sendJsonResponse(exchange, 200, s.toMap());
                return;
            }

            HttpHandlerHelper.sendError(exchange, 400, "Showtime ID is required");
        }
    }

    // -----------------------------------------------------------
    // Bookings Handlers
    // -----------------------------------------------------------
    private class BookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpHandlerHelper.enableCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String[] parts = path.split("/");

            // POST /api/bookings/{id}/cancel
            if ("POST".equalsIgnoreCase(method) && parts.length >= 5 && "cancel".equalsIgnoreCase(parts[4])) {
                String bookingId = parts[3];
                try {
                    String body = HttpHandlerHelper.readRequestBody(exchange);
                    Map<String, Object> req = SimpleJson.parseObject(body);
                    String userId = (String) req.get("userId");
                    if (userId == null) {
                        HttpHandlerHelper.sendError(exchange, 400, "userId is required to cancel a booking.");
                        return;
                    }

                    boolean ok = dataStore.cancelBooking(bookingId, userId);
                    if (ok) {
                        Map<String, Object> resp = new LinkedHashMap<>();
                        resp.put("success", true);
                        resp.put("message", "Booking cancelled successfully. Refund initiated to original payment mode.");
                        HttpHandlerHelper.sendJsonResponse(exchange, 200, resp);
                    } else {
                        HttpHandlerHelper.sendError(exchange, 400, "Could not cancel booking (already cancelled or invalid user).");
                    }
                } catch (Exception e) {
                    HttpHandlerHelper.sendError(exchange, 500, "Cancel failed: " + e.getMessage());
                }
                return;
            }

            // GET /api/bookings?userId={userId}
            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> query = HttpHandlerHelper.parseQueryParams(exchange);
                String userId = query.get("userId");
                if (userId == null || userId.isEmpty()) {
                    HttpHandlerHelper.sendError(exchange, 400, "userId query parameter is required.");
                    return;
                }

                List<Booking> list = dataStore.getBookingsByUserId(userId);
                List<Map<String, Object>> resp = new ArrayList<>();
                for (Booking b : list) {
                    resp.add(b.toMap());
                }
                HttpHandlerHelper.sendJsonResponse(exchange, 200, resp);
                return;
            }

            // POST /api/bookings (Create booking)
            if ("POST".equalsIgnoreCase(method)) {
                try {
                    String body = HttpHandlerHelper.readRequestBody(exchange);
                    Map<String, Object> req = SimpleJson.parseObject(body);

                    String userId = (String) req.get("userId");
                    String showtimeId = (String) req.get("showtimeId");
                    String seatCategory = (String) req.get("seatCategory");
                    String paymentMethod = (String) req.get("paymentMethod");

                    Object seatsObj = req.get("seats");
                    List<String> seats = new ArrayList<>();
                    if (seatsObj instanceof List) {
                        for (Object s : (List<?>) seatsObj) {
                            if (s != null) seats.add(s.toString().toUpperCase());
                        }
                    }

                    double baseAmount = 0.0;
                    if (req.get("baseAmount") instanceof Number) {
                        baseAmount = ((Number) req.get("baseAmount")).doubleValue();
                    }

                    double convenienceFee = 0.0;
                    if (req.get("convenienceFee") instanceof Number) {
                        convenienceFee = ((Number) req.get("convenienceFee")).doubleValue();
                    }

                    double totalAmount = 0.0;
                    if (req.get("totalAmount") instanceof Number) {
                        totalAmount = ((Number) req.get("totalAmount")).doubleValue();
                    }

                    if (userId == null || showtimeId == null || seats.isEmpty()) {
                        HttpHandlerHelper.sendError(exchange, 400, "Invalid booking request. Missing user, showtime, or seats.");
                        return;
                    }

                    Booking booking = dataStore.createBooking(
                        userId,
                        showtimeId,
                        seats,
                        seatCategory != null ? seatCategory : "Prime",
                        baseAmount,
                        convenienceFee,
                        totalAmount,
                        paymentMethod
                    );

                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("message", "Ticket booked successfully!");
                    resp.put("booking", booking.toMap());
                    HttpHandlerHelper.sendJsonResponse(exchange, 201, resp);
                } catch (IllegalStateException e) {
                    // Seat already booked conflict
                    HttpHandlerHelper.sendError(exchange, 409, e.getMessage());
                } catch (IllegalArgumentException e) {
                    HttpHandlerHelper.sendError(exchange, 400, e.getMessage());
                } catch (Exception e) {
                    HttpHandlerHelper.sendError(exchange, 500, "Booking failed: " + e.getMessage());
                }
                return;
            }

            HttpHandlerHelper.sendError(exchange, 405, "Method Not Allowed");
        }
    }

    // -----------------------------------------------------------
    // Static File Handler (Serves Frontend UI)
    // -----------------------------------------------------------
    private static class StaticFileHandler implements HttpHandler {
        private static final String STATIC_DIR = "src/main/resources/static";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            HttpHandlerHelper.enableCORS(exchange);
            String rawPath = exchange.getRequestURI().getPath();
            if (rawPath == null || rawPath.isEmpty() || rawPath.equals("/")) {
                rawPath = "/index.html";
            }

            // Normalize path to prevent directory traversal attack
            String cleanPath = rawPath.replace("..", "").replace("//", "/");
            Path filePath = Paths.get(STATIC_DIR, cleanPath);

            byte[] content = null;
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                content = Files.readAllBytes(filePath);
            } else {
                // Fallback check in classpath resources
                try (InputStream is = getClass().getResourceAsStream("/static" + cleanPath)) {
                    if (is != null) {
                        content = is.readAllBytes();
                    }
                }
            }

            if (content == null) {
                // 404 fallback: serve index.html for single-page routing or error
                Path indexPath = Paths.get(STATIC_DIR, "index.html");
                if (Files.exists(indexPath)) {
                    content = Files.readAllBytes(indexPath);
                    cleanPath = "/index.html";
                } else {
                    String msg = "404 Not Found: " + cleanPath;
                    if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                        exchange.sendResponseHeaders(404, -1);
                        return;
                    }
                    exchange.sendResponseHeaders(404, msg.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg.getBytes());
                    }
                    return;
                }
            }

            String mimeType = getMimeType(cleanPath);
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }
}

