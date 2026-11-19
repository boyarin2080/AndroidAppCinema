package com.example.onlinecinema;

/**
 * A single movie found by the search.
 */
public class Film {
    private final String id;
    private final String title;
    private final String poster;

    public Film(String id, String title, String poster) {
        this.id = id;
        this.title = title;
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getPoster() {
        return poster;
    }
}
