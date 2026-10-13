package com.movieticket.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Movie {
    private String id;
    private String title;
    private String language;
    private String genre;
    private double rating;
    private String votes;
    private String duration;
    private String censorRating;
    private String releaseDate;
    private String description;
    private String posterUrl;
    private String bannerUrl;
    private String cast;
    private String trailerUrl;

    public Movie() {}

    public Movie(String id, String title, String language, String genre, double rating, 
                 String votes, String duration, String censorRating, String releaseDate, 
                 String description, String posterUrl, String bannerUrl, String cast, String trailerUrl) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.genre = genre;
        this.rating = rating;
        this.votes = votes;
        this.duration = duration;
        this.censorRating = censorRating;
        this.releaseDate = releaseDate;
        this.description = description;
        this.posterUrl = posterUrl;
        this.bannerUrl = bannerUrl;
        this.cast = cast;
        this.trailerUrl = trailerUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getVotes() { return votes; }
    public void setVotes(String votes) { this.votes = votes; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getCensorRating() { return censorRating; }
    public void setCensorRating(String censorRating) { this.censorRating = censorRating; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("language", language);
        map.put("genre", genre);
        map.put("rating", rating);
        map.put("votes", votes);
        map.put("duration", duration);
        map.put("censorRating", censorRating);
        map.put("releaseDate", releaseDate);
        map.put("description", description);
        map.put("posterUrl", posterUrl);
        map.put("bannerUrl", bannerUrl);
        map.put("cast", cast);
        map.put("trailerUrl", trailerUrl);
        return map;
    }

    public static Movie fromMap(Map<String, Object> map) {
        if (map == null) return null;
        Movie m = new Movie();
        m.setId((String) map.get("id"));
        m.setTitle((String) map.get("title"));
        m.setLanguage((String) map.get("language"));
        m.setGenre((String) map.get("genre"));
        Object r = map.get("rating");
        if (r instanceof Number) {
            m.setRating(((Number) r).doubleValue());
        }
        m.setVotes((String) map.get("votes"));
        m.setDuration((String) map.get("duration"));
        m.setCensorRating((String) map.get("censorRating"));
        m.setReleaseDate((String) map.get("releaseDate"));
        m.setDescription((String) map.get("description"));
        m.setPosterUrl((String) map.get("posterUrl"));
        m.setBannerUrl((String) map.get("bannerUrl"));
        m.setCast((String) map.get("cast"));
        m.setTrailerUrl((String) map.get("trailerUrl"));
        return m;
    }
}

