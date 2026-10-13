package com.movieticket.model;

import java.util.*;

public class Booking {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String showtimeId;
    private String movieId;
    private String movieTitle;
    private String moviePoster;
    private String movieLanguage;
    private String theaterName;
    private String screen;
    private String showDate;
    private String showTime;
    private List<String> seats;
    private String seatCategory;
    private double baseAmount;
    private double convenienceFee;
    private double totalAmount;
    private String paymentMethod;
    private String paymentTransactionId;
    private String status; // "CONFIRMED" or "CANCELLED"
    private String bookingTimestamp;

    public Booking() {
        this.seats = new ArrayList<>();
        this.status = "CONFIRMED";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getMoviePoster() { return moviePoster; }
    public void setMoviePoster(String moviePoster) { this.moviePoster = moviePoster; }

    public String getMovieLanguage() { return movieLanguage; }
    public void setMovieLanguage(String movieLanguage) { this.movieLanguage = movieLanguage; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }

    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }

    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }

    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }

    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }

    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }

    public double getConvenienceFee() { return convenienceFee; }
    public void setConvenienceFee(double convenienceFee) { this.convenienceFee = convenienceFee; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingTimestamp() { return bookingTimestamp; }
    public void setBookingTimestamp(String bookingTimestamp) { this.bookingTimestamp = bookingTimestamp; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("userEmail", userEmail);
        map.put("userPhone", userPhone);
        map.put("showtimeId", showtimeId);
        map.put("movieId", movieId);
        map.put("movieTitle", movieTitle);
        map.put("moviePoster", moviePoster);
        map.put("movieLanguage", movieLanguage);
        map.put("theaterName", theaterName);
        map.put("screen", screen);
        map.put("showDate", showDate);
        map.put("showTime", showTime);
        map.put("seats", seats);
        map.put("seatCategory", seatCategory);
        map.put("baseAmount", baseAmount);
        map.put("convenienceFee", convenienceFee);
        map.put("totalAmount", totalAmount);
        map.put("paymentMethod", paymentMethod);
        map.put("paymentTransactionId", paymentTransactionId);
        map.put("status", status);
        map.put("bookingTimestamp", bookingTimestamp);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Booking fromMap(Map<String, Object> map) {
        if (map == null) return null;
        Booking b = new Booking();
        b.setId((String) map.get("id"));
        b.setUserId((String) map.get("userId"));
        b.setUserName((String) map.get("userName"));
        b.setUserEmail((String) map.get("userEmail"));
        b.setUserPhone((String) map.get("userPhone"));
        b.setShowtimeId((String) map.get("showtimeId"));
        b.setMovieId((String) map.get("movieId"));
        b.setMovieTitle((String) map.get("movieTitle"));
        b.setMoviePoster((String) map.get("moviePoster"));
        b.setMovieLanguage((String) map.get("movieLanguage"));
        b.setTheaterName((String) map.get("theaterName"));
        b.setScreen((String) map.get("screen"));
        b.setShowDate((String) map.get("showDate"));
        b.setShowTime((String) map.get("showTime"));
        
        Object s = map.get("seats");
        if (s instanceof List) {
            List<String> seatList = new ArrayList<>();
            for (Object obj : (List<?>) s) {
                if (obj != null) seatList.add(obj.toString());
            }
            b.setSeats(seatList);
        }

        b.setSeatCategory((String) map.get("seatCategory"));
        if (map.get("baseAmount") instanceof Number) {
            b.setBaseAmount(((Number) map.get("baseAmount")).doubleValue());
        }
        if (map.get("convenienceFee") instanceof Number) {
            b.setConvenienceFee(((Number) map.get("convenienceFee")).doubleValue());
        }
        if (map.get("totalAmount") instanceof Number) {
            b.setTotalAmount(((Number) map.get("totalAmount")).doubleValue());
        }
        b.setPaymentMethod((String) map.get("paymentMethod"));
        b.setPaymentTransactionId((String) map.get("paymentTransactionId"));
        b.setStatus((String) map.get("status"));
        b.setBookingTimestamp((String) map.get("bookingTimestamp"));
        return b;
    }
}

