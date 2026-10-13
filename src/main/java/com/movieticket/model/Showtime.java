package com.movieticket.model;

import java.util.*;

public class Showtime {
    private String id;
    private String movieId;
    private String theaterName;
    private String theaterLocation;
    private String date;
    private String time;
    private String screen;
    private int priceClassic;
    private int pricePrime;
    private int priceRecliner;
    private Set<String> bookedSeats;

    public Showtime() {
        this.bookedSeats = new LinkedHashSet<>();
    }

    public Showtime(String id, String movieId, String theaterName, String theaterLocation,
                    String date, String time, String screen, 
                    int priceClassic, int pricePrime, int priceRecliner, Set<String> bookedSeats) {
        this.id = id;
        this.movieId = movieId;
        this.theaterName = theaterName;
        this.theaterLocation = theaterLocation;
        this.date = date;
        this.time = time;
        this.screen = screen;
        this.priceClassic = priceClassic;
        this.pricePrime = pricePrime;
        this.priceRecliner = priceRecliner;
        this.bookedSeats = bookedSeats != null ? bookedSeats : new LinkedHashSet<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public String getTheaterLocation() { return theaterLocation; }
    public void setTheaterLocation(String theaterLocation) { this.theaterLocation = theaterLocation; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }

    public int getPriceClassic() { return priceClassic; }
    public void setPriceClassic(int priceClassic) { this.priceClassic = priceClassic; }

    public int getPricePrime() { return pricePrime; }
    public void setPricePrime(int pricePrime) { this.pricePrime = pricePrime; }

    public int getPriceRecliner() { return priceRecliner; }
    public void setPriceRecliner(int priceRecliner) { this.priceRecliner = priceRecliner; }

    public Set<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(Set<String> bookedSeats) { this.bookedSeats = bookedSeats; }

    public synchronized boolean isAnySeatBooked(List<String> seats) {
        for (String seat : seats) {
            if (bookedSeats.contains(seat.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public synchronized void bookSeats(List<String> seats) {
        for (String seat : seats) {
            bookedSeats.add(seat.toUpperCase());
        }
    }

    public synchronized void releaseSeats(List<String> seats) {
        for (String seat : seats) {
            bookedSeats.remove(seat.toUpperCase());
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("movieId", movieId);
        map.put("theaterName", theaterName);
        map.put("theaterLocation", theaterLocation);
        map.put("date", date);
        map.put("time", time);
        map.put("screen", screen);
        map.put("priceClassic", priceClassic);
        map.put("pricePrime", pricePrime);
        map.put("priceRecliner", priceRecliner);
        map.put("bookedSeats", new ArrayList<>(bookedSeats));
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Showtime fromMap(Map<String, Object> map) {
        if (map == null) return null;
        Showtime s = new Showtime();
        s.setId((String) map.get("id"));
        s.setMovieId((String) map.get("movieId"));
        s.setTheaterName((String) map.get("theaterName"));
        s.setTheaterLocation((String) map.get("theaterLocation"));
        s.setDate((String) map.get("date"));
        s.setTime((String) map.get("time"));
        s.setScreen((String) map.get("screen"));
        if (map.get("priceClassic") instanceof Number) {
            s.setPriceClassic(((Number) map.get("priceClassic")).intValue());
        }
        if (map.get("pricePrime") instanceof Number) {
            s.setPricePrime(((Number) map.get("pricePrime")).intValue());
        }
        if (map.get("priceRecliner") instanceof Number) {
            s.setPriceRecliner(((Number) map.get("priceRecliner")).intValue());
        }
        Object bs = map.get("bookedSeats");
        if (bs instanceof List) {
            List<Object> list = (List<Object>) bs;
            for (Object item : list) {
                if (item != null) {
                    s.getBookedSeats().add(item.toString().toUpperCase());
                }
            }
        }
        return s;
    }
}

