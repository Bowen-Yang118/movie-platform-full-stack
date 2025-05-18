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

// Declaring a WebServlet called movies.SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "movies.SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");


        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            //name;
            //year of birth (N/A if not available);


            // Construct a query with parameter represented by "?"
            String starquery = "SELECT m.name, m.birthYear " +
                    "FROM stars m " +
                    "WHERE m.id = ?";

            // Declare our statement
            PreparedStatement star_statement = conn.prepareStatement(starquery);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            star_statement.setString(1, id);

            // Perform the query
            ResultSet starRs = star_statement.executeQuery();



            JsonObject starJson = new JsonObject();
            if (starRs.next()) {

                starJson.addProperty("name", starRs.getString("name"));
                String birthYear = starRs.getString("birthYear");
                starJson.addProperty("birthYear", birthYear != null ? birthYear : "N/A");

            }



            //all movies (hyperlinked) in which the star acted.

//            String star_in_movieQuery = "SELECT g.movieId, m.title FROM stars_in_movies g " +
//                    "JOIN movies m on m.id=g.movieId " +
//                    "WHERE  g.starId = ?";
            String star_in_movieQuery = "SELECT m.id AS movieId, m.title, m.year " +
                    "FROM stars_in_movies g " +
                    "JOIN movies m ON m.id = g.movieId " +
                    "WHERE g.starId = ? " +
                    "ORDER BY m.year DESC, m.title ASC";

            PreparedStatement star_in_movieStmt = conn.prepareStatement(star_in_movieQuery);
            star_in_movieStmt.setString(1, id);
            ResultSet star_in_movieRs = star_in_movieStmt.executeQuery();


            JsonArray star_in_movieArray = new JsonArray();
            while (star_in_movieRs.next()) {
                JsonObject star_in_movie = new JsonObject();
                star_in_movie.addProperty("movieId",star_in_movieRs.getString("movieId"));
                star_in_movie.addProperty("title",star_in_movieRs.getString("title"));
                star_in_movie.addProperty("year",star_in_movieRs.getInt("year"));
                star_in_movieArray.add(star_in_movie);
            }
            starJson.add("movies", star_in_movieArray);


            out.write(starJson.toString());
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
            //for test use
            // System.out.println(id);
            out.close();
        }


    }

}
