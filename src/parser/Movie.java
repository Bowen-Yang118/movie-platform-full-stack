package parser;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;
    private List<String> genres;

    public Movie() {
        genres = new ArrayList<>();
    }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setYear(int year) { this.year = year; }
    public void setDirector(String director) { this.director = director; }
    public void addGenre(String genre) { this.genres.add(genre); }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getDirector() { return director; }
    public List<String> getGenres() { return genres; }
}

