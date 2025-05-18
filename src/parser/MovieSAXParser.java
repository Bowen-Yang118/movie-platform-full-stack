package parser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;


/*For mains243.xml

info: movie_name/movie_id/director/genres
update table: movies, genres, genres_in_movies

*/


public class MovieSAXParser extends DefaultHandler {
    private List<Movie> movies;
    private Movie tempMovie;
    private String tempValue;
    private String currentDirector;

    private Map<String, String> genreMapping;
    private PreparedStatement insertMovieStmt, insertGenreStmt, insertGenreMovieStmt;
    private Map<String, String> existingMovies;
    private Map<String, Integer> existingGenres;
    private Set<String> existingGenreMoviePairs;
    private Connection conn;
    private int count = 1;

    public MovieSAXParser(Connection conn) throws SQLException {
        this.conn = conn;
        this.genreMapping = GenreMapping.getGenreMapping();
        movies = new ArrayList<>();

        insertMovieStmt = conn.prepareStatement(
                "INSERT INTO movies (id, title, year, director,price) VALUES (?, ?, ?, ?,?)");
        insertGenreStmt = conn.prepareStatement(
                "INSERT INTO genres (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        insertGenreMovieStmt = conn.prepareStatement(
                "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)");

        loadExistingMovies();
        loadExistingGenres();
        loadExistingGenreMoviePairs();
    }

    private void loadExistingMovies() throws SQLException {
        existingMovies = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, title FROM movies");
        while (rs.next()) {
            existingMovies.put(rs.getString("id"), rs.getString("title"));
        }
        rs.close();
        stmt.close();
    }

    private void loadExistingGenres() throws SQLException {
        existingGenres = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, name FROM genres");
        while (rs.next()) {
            existingGenres.put(rs.getString("name"), rs.getInt("id"));
        }
        rs.close();
        stmt.close();
    }

    private void loadExistingGenreMoviePairs() throws SQLException {
        existingGenreMoviePairs = new HashSet<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT genreId, movieId FROM genres_in_movies");
        while (rs.next()) {
            existingGenreMoviePairs.add(rs.getInt("genreId") + "-" + rs.getString("movieId"));
        }
        rs.close();
        stmt.close();
    }

    public void parseXML(String filename) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            // Explicitly set ISO-8859-1 encoding
            FileInputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            InputSource inputSource = new InputSource(isr);

            saxParser.parse(inputSource, this);
            batchInsertMovies();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void batchInsertMovies() {
        try {
            for (Movie movie : movies) {
                String movieId = movie.getId();
                //assign a new id if not exits
                if (movieId == null || movieId.isEmpty()) {
                    movieId = "tt" + (1000000 + count);
                    count += 1;
                }
                System.out.println("ADD movie: " + movieId);
                if (!existingMovies.containsKey(movieId)) {
                    insertMovieStmt.setString(1, movieId);
                    insertMovieStmt.setString(2, movie.getTitle());
                    insertMovieStmt.setInt(3, movie.getYear());
                    insertMovieStmt.setString(4, movie.getDirector());



                    float randomPrice = 5 + (float) (Math.random() * 45);
                    BigDecimal roundedPrice = new BigDecimal(randomPrice).setScale(2, RoundingMode.HALF_UP);
                    insertMovieStmt.setBigDecimal(5, roundedPrice);

                    insertMovieStmt.addBatch();
                    existingMovies.put(movieId, movie.getTitle());
                }

                for (String cat : movie.getGenres()) {
                    String genre = genreMapping.getOrDefault(cat, cat);
                    int genreId = existingGenres.getOrDefault(genre, -1);

                    if (genreId == -1) {
                        insertGenreStmt.setString(1, genre);
                        insertGenreStmt.executeUpdate();
                        ResultSet rs = insertGenreStmt.getGeneratedKeys();
                        if (rs.next()) {
                            genreId = rs.getInt(1);
                            existingGenres.put(genre, genreId);
                        }
                        rs.close();
                    }

                    String genreMovieKey = genreId + "-" + movieId;
                    if (!existingGenreMoviePairs.contains(genreMovieKey)) {
                        insertGenreMovieStmt.setInt(1, genreId);
                        insertGenreMovieStmt.setString(2, movieId);
                        insertGenreMovieStmt.addBatch();
                        existingGenreMoviePairs.add(genreMovieKey);
                    }
                }
            }
            insertMovieStmt.executeBatch();
            insertGenreMovieStmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println(e);
                System.err.println("Error in movie -> rolling back");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }


    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempValue = "";
        if (qName.equalsIgnoreCase("directorfilms")) {
            currentDirector = "";
        } else if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dirname")) {
            currentDirector = tempValue;
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempValue.trim());
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempValue);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                tempMovie.setYear(Integer.parseInt(tempValue));
            } catch (NumberFormatException e) {
                tempMovie.setYear(0);
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            tempMovie.addGenre(tempValue);
        } else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(currentDirector);
            movies.add(tempMovie);
        }
    }
}
