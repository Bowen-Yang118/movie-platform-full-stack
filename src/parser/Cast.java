package parser;

public class Cast {
    private String starId;
    private String movieId;

    public Cast(String starId, String movieId) {
        this.starId = starId;
        this.movieId = movieId;
    }

    public String getStarId() { return starId; }
    public String getMovieId() { return movieId; }
}

