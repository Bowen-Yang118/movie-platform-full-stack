package movies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called movies.SingleMovieServlet, which maps to url "/api/single-star"
@WebServlet(name = "movies.SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("single movie");
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");
        System.out.println(id);

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String moviequery = "SELECT m.id, m.title, m.year, m.director, r.rating, m.price " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE m.id = ?";

            // Declare our statement
            PreparedStatement movie_statement = conn.prepareStatement(moviequery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            movie_statement.setString(1, id);

            // Perform the query
            ResultSet movieRs = movie_statement.executeQuery();

            /*Single common.parser.Movie Page
            From common.parser.Movie List Page or Single Star Page, if the user clicks on a movie (hyperlinked),
            the corresponding Single common.parser.Movie page displays all the information about the movie, including:
            title;1
            year;1
            director;1
            all of the genres;
            all of the stars (hyperlinked);
            rating.1
            */

            JsonObject movieJson = new JsonObject();
            if (movieRs.next()) {

                movieJson.addProperty("id", movieRs.getString("id"));
                movieJson.addProperty("title", movieRs.getString("title"));
                movieJson.addProperty("year", movieRs.getInt("year"));
                movieJson.addProperty("director", movieRs.getString("director"));
                movieJson.addProperty("rating", movieRs.getFloat("rating"));
                movieJson.addProperty("price", movieRs.getString("price"));
            }


            // find genres
//            String genreQuery = "SELECT g.name FROM genres_in_movies gim " +
//                    "JOIN genres g ON gim.genreId = g.id WHERE gim.movieId = ?";
            String genreQuery = "SELECT g.id, g.name FROM genres_in_movies gim " +
                    "JOIN genres g ON gim.genreId = g.id " +
                    "WHERE gim.movieId = ? ORDER BY g.name ASC";


            PreparedStatement genreStmt = conn.prepareStatement(genreQuery);
            genreStmt.setString(1, id);
            ResultSet genreRs = genreStmt.executeQuery();

            JsonArray genresArray = new JsonArray();
            while (genreRs.next()) {
//                genresArray.add(genreRs.getString("name"));
                JsonObject genreJson = new JsonObject();
                genreJson.addProperty("id", genreRs.getString("id"));
                genreJson.addProperty("name", genreRs.getString("name"));
                genresArray.add(genreJson);
            }
            movieJson.add("genres", genresArray);


            // find stars
//            String starQuery = "SELECT s.id, s.name FROM stars_in_movies sim " +
//                    "JOIN stars s ON sim.starId = s.id WHERE sim.movieId = ?";
            String starQuery = "SELECT s.id, s.name, " +
                    "(SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) AS movie_count " +
                    "FROM stars_in_movies sim " +
                    "JOIN stars s ON sim.starId = s.id " +
                    "WHERE sim.movieId = ? " +
                    "GROUP BY s.id, s.name " +
                    "ORDER BY movie_count DESC, s.name ASC";


            PreparedStatement starStmt = conn.prepareStatement(starQuery);
            starStmt.setString(1, id);
            ResultSet starRs = starStmt.executeQuery();

            JsonArray starsArray = new JsonArray();
            while (starRs.next()) {
                JsonObject starJson = new JsonObject();
                starJson.addProperty("id", starRs.getString("id"));
                starJson.addProperty("name", starRs.getString("name"));
                starJson.addProperty("movie_count", starRs.getInt("movie_count"));
                starsArray.add(starJson);
            }
            movieJson.add("stars", starsArray);

            out.write(movieJson.toString());

            response.setStatus(200);



        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {

            out.close();
        }


    }

}
