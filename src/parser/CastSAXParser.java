package parser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



public class CastSAXParser extends DefaultHandler {
    private List<Cast> castList;
    private String tempValue, movieId, starName;
    private Connection conn;
    private PreparedStatement insertCastStmt;
    private Map<String, String> existingStars;
    private Set<String> existingStarMoviePairs;
    private boolean inFilm;

    private static final Logger logger = Logger.getLogger("GlobalLogger");

    static {
        try {
            Locale.setDefault(Locale.ENGLISH);

            FileHandler fileHandler = new FileHandler("parse_errors.log", true); // Append to the log file
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevent logging to console twice
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public CastSAXParser(Connection conn) throws SQLException {
        this.conn = conn;
        this.castList = new ArrayList<>();

        insertCastStmt = conn.prepareStatement(
                "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)"
        );

        loadExistingStars();
        loadExistingStarMoviePairs();
    }
    private Map<String, String> existingMovies;

    private void loadExistingMovies() throws SQLException {
        existingMovies = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM movies");
        while (rs.next()) {
            existingMovies.put(rs.getString("id"), rs.getString("id"));
        }
        rs.close();
        stmt.close();
    }


    private void loadExistingStars() throws SQLException {
        existingStars = new HashMap<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, name FROM stars");
        while (rs.next()) {
            existingStars.put(rs.getString("name").toLowerCase(), rs.getString("id"));
        }
        rs.close();
        stmt.close();
    }

    private void loadExistingStarMoviePairs() throws SQLException {
        existingStarMoviePairs = new HashSet<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT starId, movieId FROM stars_in_movies");
        while (rs.next()) {
            existingStarMoviePairs.add(rs.getString("starId") + "-" + rs.getString("movieId"));
        }
        rs.close();
        stmt.close();
    }

    public void parseXML(String filename) {
        try {
            loadExistingMovies();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            // Explicitly set ISO-8859-1 encoding
            FileInputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            InputSource inputSource = new InputSource(isr);

            saxParser.parse(inputSource, this);

            batchInsertCasts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void batchInsertCasts() {
        try {
            for (Cast cast : castList) {
                if (!existingStarMoviePairs.contains(cast.getStarId() + "-" + cast.getMovieId())) {
                    insertCastStmt.setString(1, cast.getStarId());
                    insertCastStmt.setString(2, cast.getMovieId());
                    insertCastStmt.addBatch();
                    existingStarMoviePairs.add(cast.getStarId() + "-" + cast.getMovieId());
                }
            }
            insertCastStmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println(e);
                System.err.println("Error in cast -> rolling back");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempValue = "";
        if (qName.equalsIgnoreCase("filmc")) {
            inFilm = true; // new movie
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempValue = new String(ch, start, length).trim();
    }

    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("f")) {
            movieId = tempValue.trim();
        } else if (qName.equalsIgnoreCase("a")) {
            starName = tempValue.trim().replaceAll("\\s+", " ").toLowerCase();
        } else if (qName.equalsIgnoreCase("m")) {
            if (movieId == null || movieId.isEmpty() || !existingMovies.containsKey(movieId)) {
                String logMessage = "movie " + movieId + " not exist on movies, skip " + starName;
                System.err.println(logMessage);
                logger.warning(logMessage);
                return;
            }

            String starId = existingStars.get(starName);
            if (starId != null) {
                castList.add(new Cast(starId, movieId));
                System.out.println("Add pair " + starName + " -> " + movieId);
            } else {
                String logMessage ="Unmatched pair " + starName + " <-> "+movieId;
                System.out.println(logMessage);
                logger.warning(logMessage);
            }
        }
        else if (qName.equalsIgnoreCase("filmc")) {
            inFilm = false;
        }
    }
}



