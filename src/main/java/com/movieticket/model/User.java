package com.movieticket.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String phone;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String id, String name, String email, String password, String phone) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Map<String, Object> toMap(boolean includePassword) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        if (includePassword) {
            map.put("password", password);
        }
        map.put("phone", phone != null ? phone : "");
        return map;
    }

    public static User fromMap(Map<String, Object> map) {
        if (map == null) return null;
        User u = new User();
        u.setId((String) map.get("id"));
        u.setName((String) map.get("name"));
        u.setEmail((String) map.get("email"));
        u.setPassword((String) map.get("password"));
        u.setPhone((String) map.get("phone"));
        return u;
    }
}

