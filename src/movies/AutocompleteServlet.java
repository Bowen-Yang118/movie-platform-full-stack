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

@WebServlet(name = "movies.AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            // mysql for this servlet should be ReadOnly
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
            //dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        //json output
//        response.setContentType("application/json");
        // Output stream to STDOUT
        // Create a new connection to database
        try (PrintWriter out = response.getWriter(); Connection dbCon = dataSource.getConnection()) {
            if (dbCon == null) throw new Exception("Database connection failed.");

            // setup the response json array
            JsonArray jsonArray = new JsonArray();
            // get the query string from parameter
            String query = request.getParameter("query");
            // return the empty json array if query is null, empty or has less than 3 characters
            if (query == null || query.trim().length() < 3) {
                out.write(jsonArray.toString());
                return;
            }
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
            StringBuilder booleanQuery = new StringBuilder();
            for (String word : query.trim().split("\\s+")) {
                if (!word.isEmpty()) {  // Ensure we don't append empty tokens
                    booleanQuery.append("+").append(word).append("* ");
                }
            }
            String finalBooleanQuery = booleanQuery.toString().trim(); // Ensure no trailing spaces
            // SQL Query: Full-Text Search with a LIMIT of 10 results
            int threshold = Math.max(1, query.trim().length() / 5);
            // SQL Query: Full-Text Search with a LIMIT of 10 results
            String sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            // String sql = "SELECT id, title FROM movies " +
            //         "WHERE (MATCH(title) AGAINST (? IN BOOLEAN MODE)";

                    //"OR edth(lower(title), lower(?), " + threshold + ") = true) " +
                    //"LIMIT 10";


            try (PreparedStatement ps = dbCon.prepareStatement(sql)) {
                ps.setString(1, finalBooleanQuery);
                //ps.setString(2, query);
                // Log query execution (for debugging & Task Requirement)
                request.getServletContext().log("Executing SQL: " + sql + " | Parameters: [" + finalBooleanQuery + ", " + query + "]");
//                ps.setString(1, booleanQuery.toString().trim()); // Ensure no trailing spaces
                // Execute query
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        JsonObject movie = new JsonObject();
                        movie.addProperty("label", rs.getString("title"));
                        movie.addProperty("value", rs.getString("title"));  // Autocomplete suggestion text
                        JsonObject data = new JsonObject();
                        data.addProperty("movieID", rs.getString("id")); // common.parser.Movie ID as additional information
                        movie.add("data", data);
                        jsonArray.add(movie);
                    }
                }
            }

            // Send JSON response
            out.write(jsonArray.toString());
            response.setStatus(200);

        } catch (Exception e) {
            response.sendError(500, e.getMessage());
        }
    }
}
