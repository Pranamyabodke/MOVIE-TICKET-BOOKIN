package com.movieticket.storage;

import com.movieticket.model.*;
import com.movieticket.util.SimpleJson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final String DATA_FILE = "data/database.json";

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Movie> movies = new ConcurrentHashMap<>();
    private final Map<String, Showtime> showtimes = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    private static DataStore instance;

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
            instance.loadOrInit();
        }
        return instance;
    }

    private DataStore() {}

    public synchronized void loadOrInit() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                Map<String, Object> root = SimpleJson.parseObject(json);
                if (root != null && !root.isEmpty()) {
                    loadFromMap(root);
                    System.out.println("[DataStore] Loaded database with " + movies.size() + " movies, " 
                        + showtimes.size() + " showtimes, " + users.size() + " users, " + bookings.size() + " bookings.");
                    return;
                }
            } catch (Exception e) {
                System.err.println("[DataStore] Error loading data, initializing seeds: " + e.getMessage());
            }
        }
        // Seed default initial data
        seedDefaults();
        save();
    }

    private void loadFromMap(Map<String, Object> root) {
        users.clear();
        movies.clear();
        showtimes.clear();
        bookings.clear();

        Object uList = root.get("users");
        if (uList instanceof List) {
            for (Object item : (List<?>) uList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    User u = User.fromMap((Map<String, Object>) item);
                    if (u != null && u.getId() != null) users.put(u.getId(), u);
                }
            }
        }

        Object mList = root.get("movies");
        if (mList instanceof List) {
            for (Object item : (List<?>) mList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Movie m = Movie.fromMap((Map<String, Object>) item);
                    if (m != null && m.getId() != null) movies.put(m.getId(), m);
                }
            }
        }

        Object sList = root.get("showtimes");
        if (sList instanceof List) {
            for (Object item : (List<?>) sList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Showtime s = Showtime.fromMap((Map<String, Object>) item);
                    if (s != null && s.getId() != null) showtimes.put(s.getId(), s);
                }
            }
        }

        Object bList = root.get("bookings");
        if (bList instanceof List) {
            for (Object item : (List<?>) bList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Booking b = Booking.fromMap((Map<String, Object>) item);
                    if (b != null && b.getId() != null) bookings.put(b.getId(), b);
                }
            }
        }
    }

    public synchronized void save() {
        try {
            File dir = new File("data");
            if (!dir.exists()) dir.mkdirs();

            Map<String, Object> root = new LinkedHashMap<>();

            List<Map<String, Object>> uList = new ArrayList<>();
            for (User u : users.values()) uList.add(u.toMap(true));
            root.put("users", uList);

            List<Map<String, Object>> mList = new ArrayList<>();
            for (Movie m : movies.values()) mList.add(m.toMap());
            root.put("movies", mList);

            List<Map<String, Object>> sList = new ArrayList<>();
            for (Showtime s : showtimes.values()) sList.add(s.toMap());
            root.put("showtimes", sList);

            List<Map<String, Object>> bList = new ArrayList<>();
            for (Booking b : bookings.values()) bList.add(b.toMap());
            root.put("bookings", bList);

            String json = SimpleJson.stringify(root);
            Files.writeString(Paths.get(DATA_FILE), json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[DataStore] Failed to save database: " + e.getMessage());
        }
    }

    private void seedDefaults() {
        System.out.println("[DataStore] Seeding initial BookMyShow movie and theater data...");

        // 1. Default Demo User
        User demoUser = new User(
            "user-demo-01",
            "Rahul Sharma",
            "demo@example.com",
            "password123",
            "+91 9876543210"
        );
        users.put(demoUser.getId(), demoUser);

        // 2. Movies
        Movie m1 = new Movie(
            "mov-1",
            "Jawan",
            "Hindi",
            "Action, Thriller",
            8.9,
            "310K votes",
            "2h 49m",
            "UA 16+",
            "07 Sep, 2023",
            "A high-octane action thriller outlining the emotional journey of a man who is set to rectify the wrongs in society.",
            "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=1200&auto=format&fit=crop&q=80",
            "Shah Rukh Khan, Nayanthara, Vijay Sethupathi, Deepika Padukone",
            "https://www.youtube.com/watch?v=MWOlnZSnXpc"
        );

        Movie m2 = new Movie(
            "mov-2",
            "Kalki 2898 AD",
            "Telugu, Hindi",
            "Sci-Fi, Action, Mythology",
            8.7,
            "420K votes",
            "3h 01m",
            "UA 16+",
            "27 Jun, 2024",
            "Set in the post-apocalyptic world of Kashi in 2898 AD, a chosen one emerges to protect the unborn avatar of Vishnu, Kalki.",
            "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            "Prabhas, Amitabh Bachchan, Kamal Haasan, Deepika Padukone",
            "https://www.youtube.com/watch?v=kQDd1AhGIHk"
        );

        Movie m3 = new Movie(
            "mov-3",
            "Inception",
            "English",
            "Sci-Fi, Thriller, Mind-Bending",
            9.1,
            "890K votes",
            "2h 28m",
            "UA 13+",
            "16 Jul, 2010",
            "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
            "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&auto=format&fit=crop&q=80",
            "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page, Tom Hardy",
            "https://www.youtube.com/watch?v=YoHD9XEInc0"
        );

        Movie m4 = new Movie(
            "mov-4",
            "Stree 2: Sarkate Ka Aatank",
            "Hindi",
            "Comedy, Horror",
            8.6,
            "280K votes",
            "2h 27m",
            "UA 16+",
            "15 Aug, 2024",
            "The town of Chanderi is being haunted again, this time by a headless entity named Sarkata. Vicky and his friends must unite with Stree to defeat it.",
            "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=1200&auto=format&fit=crop&q=80",
            "Shraddha Kapoor, Rajkummar Rao, Pankaj Tripathi, Abhishek Banerjee",
            "https://www.youtube.com/watch?v=kC7fS3_vQ3A"
        );

        Movie m5 = new Movie(
            "mov-5",
            "Spider-Man: Across the Spider-Verse",
            "English, Hindi",
            "Animation, Action, Sci-Fi",
            9.0,
            "510K votes",
            "2h 20m",
            "U",
            "02 Jun, 2023",
            "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.",
            "https://images.unsplash.com/photo-1635805737707-575885ab0820?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1200&auto=format&fit=crop&q=80",
            "Shameik Moore, Hailee Steinfeld, Oscar Isaac, Daniel Kaluuya",
            "https://www.youtube.com/watch?v=cqGjhVJWtEg"
        );

        Movie m6 = new Movie(
            "mov-6",
            "Interstellar",
            "English",
            "Sci-Fi, Adventure, Drama",
            9.3,
            "980K votes",
            "2h 49m",
            "UA",
            "07 Nov, 2014",
            "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft along with a team of researchers.",
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&auto=format&fit=crop&q=80",
            "Matthew McConaughey, Anne Hathaway, Jessica Chastain, Michael Caine",
            "https://www.youtube.com/watch?v=zSWdZVtXT7E"
        );

        movies.put(m1.getId(), m1);
        movies.put(m2.getId(), m2);
        movies.put(m3.getId(), m3);
        movies.put(m4.getId(), m4);
        movies.put(m5.getId(), m5);
        movies.put(m6.getId(), m6);

        // 3. Showtimes generator (for today, tomorrow, day 3)
        LocalDate today = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String[] dates = new String[] {
            today.format(df),
            today.plusDays(1).format(df),
            today.plusDays(2).format(df)
        };

        String[][] theaters = new String[][] {
            {"PVR: Phoenix Marketcity", "Kurla West, Mumbai", "Audi 01 (Dolby 7.1)"},
            {"INOX: R-City Mall", "LBS Marg, Ghatkopar", "Insignia Screen 03"},
            {"Cinepolis: Viviana Mall", "Eastern Express Highway, Thane", "Screen 05 (IMAX 3D)"},
            {"PVR ICON: Infinity Mall", "Link Road, Andheri West", "Gold Class Audi 02"}
        };

        String[] times = new String[] {"10:15 AM", "01:45 PM", "05:30 PM", "09:15 PM"};

        int showCount = 1;
        for (Movie movie : movies.values()) {
            for (int d = 0; d < dates.length; d++) {
                String date = dates[d];
                for (int t = 0; t < theaters.length; t++) {
                    String[] th = theaters[t];
                    // Create 2 showtimes per theater per date
                    for (int s = 0; s < 2; s++) {
                        String time = times[(t + s) % times.length];
                        String showId = "show-" + (showCount++);

                        // Pre-populate some realistic booked seats
                        Set<String> preBooked = new LinkedHashSet<>();
                        if ((showCount % 2) == 0) {
                            preBooked.addAll(Arrays.asList("C4", "C5", "D5", "D6"));
                        }
                        if ((showCount % 3) == 0) {
                            preBooked.addAll(Arrays.asList("E6", "E7", "F7", "F8"));
                        }

                        Showtime st = new Showtime(
                            showId,
                            movie.getId(),
                            th[0],
                            th[1],
                            date,
                            time,
                            th[2],
                            160, // Classic
                            240, // Prime
                            380, // Recliner
                            preBooked
                        );
                        showtimes.put(st.getId(), st);
                    }
                }
            }
        }

        // 4. Create one initial sample booking for demo user so "My Bookings" has immediate content to show!
        Showtime sampleShow = showtimes.get("show-1");
        if (sampleShow != null) {
            sampleShow.getBookedSeats().addAll(Arrays.asList("D7", "D8"));
            Booking sampleBooking = new Booking();
            sampleBooking.setId("BMS-2026-78421");
            sampleBooking.setUserId(demoUser.getId());
            sampleBooking.setUserName(demoUser.getName());
            sampleBooking.setUserEmail(demoUser.getEmail());
            sampleBooking.setUserPhone(demoUser.getPhone());
            sampleBooking.setShowtimeId(sampleShow.getId());
            sampleBooking.setMovieId(m1.getId());
            sampleBooking.setMovieTitle(m1.getTitle());
            sampleBooking.setMoviePoster(m1.getPosterUrl());
            sampleBooking.setMovieLanguage(m1.getLanguage());
            sampleBooking.setTheaterName(sampleShow.getTheaterName());
            sampleBooking.setScreen(sampleShow.getScreen());
            sampleBooking.setShowDate(sampleShow.getDate());
            sampleBooking.setShowTime(sampleShow.getTime());
            sampleBooking.setSeats(Arrays.asList("D7", "D8"));
            sampleBooking.setSeatCategory("Prime");
            sampleBooking.setBaseAmount(480.0);
            sampleBooking.setConvenienceFee(43.20);
            sampleBooking.setTotalAmount(523.20);
            sampleBooking.setPaymentMethod("UPI (Google Pay)");
            sampleBooking.setPaymentTransactionId("TXN_UPI_984712");
            sampleBooking.setStatus("CONFIRMED");
            sampleBooking.setBookingTimestamp(today.toString() + " 11:20 AM");
            bookings.put(sampleBooking.getId(), sampleBooking);
        }
    }

    // ----------------------------------------------------
    // User Operations
    // ----------------------------------------------------
    public User findUserByEmail(String email) {
        if (email == null) return null;
        for (User u : users.values()) {
            if (email.trim().equalsIgnoreCase(u.getEmail().trim())) {
                return u;
            }
        }
        return null;
    }

    public User findUserById(String id) {
        return id != null ? users.get(id) : null;
    }

    public synchronized User registerUser(String name, String email, String password, String phone) {
        if (findUserByEmail(email) != null) {
            return null; // Already exists
        }
        User user = new User(UUID.randomUUID().toString(), name, email, password, phone);
        users.put(user.getId(), user);
        save();
        return user;
    }

    public User authenticate(String email, String password) {
        User u = findUserByEmail(email);
        if (u != null && u.getPassword() != null && u.getPassword().equals(password)) {
            return u;
        }
        return null;
    }

    // ----------------------------------------------------
    // Movie Operations
    // ----------------------------------------------------
    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies.values());
    }

    public Movie getMovieById(String id) {
        return movies.get(id);
    }

    // ----------------------------------------------------
    // Showtime Operations
    // ----------------------------------------------------
    public List<Showtime> getShowtimesByMovieAndDate(String movieId, String date) {
        List<Showtime> list = new ArrayList<>();
        for (Showtime s : showtimes.values()) {
            if (s.getMovieId().equals(movieId)) {
                if (date == null || date.isEmpty() || s.getDate().equals(date)) {
                    list.add(s);
                }
            }
        }
        list.sort(Comparator.comparing(Showtime::getTime));
        return list;
    }

    public Showtime getShowtimeById(String id) {
        return showtimes.get(id);
    }

    // ----------------------------------------------------
    // Booking Operations
    // ----------------------------------------------------
    public synchronized Booking createBooking(
        String userId,
        String showtimeId,
        List<String> seats,
        String seatCategory,
        double baseAmount,
        double convenienceFee,
        double totalAmount,
        String paymentMethod
    ) {
        User user = findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Showtime showtime = getShowtimeById(showtimeId);
        if (showtime == null) {
            throw new IllegalArgumentException("Showtime not found");
        }

        Movie movie = getMovieById(showtime.getMovieId());
        if (movie == null) {
            throw new IllegalArgumentException("Movie not found");
        }

        // Thread-safe check: Are any seats already taken?
        if (showtime.isAnySeatBooked(seats)) {
            throw new IllegalStateException("One or more selected seats have already been booked!");
        }

        // Reserve seats
        showtime.bookSeats(seats);

        // Generate Booking
        String bookingId = "BMS-" + LocalDate.now().getYear() + "-" + (10000 + new Random().nextInt(90000));
        String txnId = "TXN_" + System.currentTimeMillis();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setUserId(user.getId());
        booking.setUserName(user.getName());
        booking.setUserEmail(user.getEmail());
        booking.setUserPhone(user.getPhone());
        booking.setShowtimeId(showtime.getId());
        booking.setMovieId(movie.getId());
        booking.setMovieTitle(movie.getTitle());
        booking.setMoviePoster(movie.getPosterUrl());
        booking.setMovieLanguage(movie.getLanguage());
        booking.setTheaterName(showtime.getTheaterName());
        booking.setScreen(showtime.getScreen());
        booking.setShowDate(showtime.getDate());
        booking.setShowTime(showtime.getTime());
        booking.setSeats(new ArrayList<>(seats));
        booking.setSeatCategory(seatCategory);
        booking.setBaseAmount(baseAmount);
        booking.setConvenienceFee(convenienceFee);
        booking.setTotalAmount(totalAmount);
        booking.setPaymentMethod(paymentMethod != null ? paymentMethod : "UPI (Simulated)");
        booking.setPaymentTransactionId(txnId);
        booking.setStatus("CONFIRMED");
        booking.setBookingTimestamp(LocalDate.now().toString() + " " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));

        bookings.put(booking.getId(), booking);

        save();
        return booking;
    }

    public List<Booking> getBookingsByUserId(String userId) {
        List<Booking> list = new ArrayList<>();
        if (userId == null) return list;
        for (Booking b : bookings.values()) {
            if (userId.equals(b.getUserId())) {
                list.add(b);
            }
        }
        // Newest bookings first
        list.sort((a, b) -> b.getId().compareTo(a.getId()));
        return list;
    }

    public Booking getBookingById(String id) {
        return bookings.get(id);
    }

    public synchronized boolean cancelBooking(String bookingId, String userId) {
        Booking b = bookings.get(bookingId);
        if (b == null || !b.getUserId().equals(userId)) {
            return false;
        }
        if ("CANCELLED".equals(b.getStatus())) {
            return false;
        }

        b.setStatus("CANCELLED");

        // Release seats back to the showtime!
        Showtime st = getShowtimeById(b.getShowtimeId());
        if (st != null) {
            st.releaseSeats(b.getSeats());
        }

        save();
        return true;
    }
}

