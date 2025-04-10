package edu.psu.com.example.aileaguehackathon;

import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String language;
    private String currentLocation;
    private List<String> favoriteTeams;
    private List<String> interests;
    private List<String> dietaryPreferences;
    private boolean needsAccessibility;

    public User() {
        // Default constructor required for JSON parsing
    }

    public User(String userId, String name, String email, String language, String currentLocation,
                List<String> favoriteTeams, List<String> interests, List<String> dietaryPreferences,
                boolean needsAccessibility) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.language = language;
        this.currentLocation = currentLocation;
        this.favoriteTeams = favoriteTeams;
        this.interests = interests;
        this.dietaryPreferences = dietaryPreferences;
        this.needsAccessibility = needsAccessibility;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<String> getFavoriteTeams() {
        return favoriteTeams;
    }

    public void setFavoriteTeams(List<String> favoriteTeams) {
        this.favoriteTeams = favoriteTeams;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(List<String> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public boolean isNeedsAccessibility() {
        return needsAccessibility;
    }

    public void setNeedsAccessibility(boolean needsAccessibility) {
        this.needsAccessibility = needsAccessibility;
    }
}