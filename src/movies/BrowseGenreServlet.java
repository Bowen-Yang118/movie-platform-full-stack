package movies;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "movies.BrowseGenreServlet", urlPatterns = "/_browse_genre")
public class BrowseGenreServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonArray genreArray = new JsonArray();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT name FROM genres ORDER BY name ASC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JsonObject genreObject = new JsonObject();
                genreObject.addProperty("name", rs.getString("name"));
                genreArray.add(genreObject);
            }
            response.getWriter().write(genreArray.toString());
        } catch (SQLException e) {
//          e.printStackTrace();
            response.setStatus(500);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", e.getMessage());
            response.getWriter().write(errorResponse.toString());
        }
    }
}
