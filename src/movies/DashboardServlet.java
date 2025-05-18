package movies;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "movies.DashboardServlet", urlPatterns = "/_dashboard")
public class DashboardServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private void handleAddMovie(HttpServletRequest request, HttpServletResponse response, Connection connection) throws IOException {
        JsonObject responseJsonObject = new JsonObject();
        System.out.println("handleAddMovie");
        // Retrieve parameters from request
        String title = request.getParameter("title");
        String yearStr = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("star");
        String genreName = request.getParameter("genre");


        // Validate required parameters
        if (title == null || title.trim().isEmpty() ||
                yearStr == null || yearStr.trim().isEmpty() ||
                director == null || director.trim().isEmpty() ||
                starName == null || starName.trim().isEmpty() ||
                genreName == null || genreName.trim().isEmpty()) {

            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "All fields are required.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);  // Convert year to integer
        } catch (NumberFormatException e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Invalid year format.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (CallableStatement cs = connection.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?)}")) {
            cs.setString(1, title);
            cs.setInt(2, year);
            cs.setString(3, director);
            cs.setString(4, starName);
            cs.setString(5, genreName);
            cs.registerOutParameter(6, Types.VARCHAR); // Output message

            // Execute the stored procedure
            cs.execute();
            String resultMessage = cs.getString(6);

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", resultMessage);
            response.getWriter().write(responseJsonObject.toString());

        } catch (SQLException e) {
            response.setStatus(500);
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", e.getMessage());
            response.getWriter().write(responseJsonObject.toString());
        }
    }



    private void handleAddStar(HttpServletRequest request, HttpServletResponse response, Connection connection) throws IOException {
        JsonObject responseJsonObject = new JsonObject();
        String name = request.getParameter("name");
        String birthYearStr = request.getParameter("birth_year");

        if(name == null || name.trim().isEmpty()) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Star name is required.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(responseJsonObject.toString());
            return;
        }
        Integer birthYear = (birthYearStr == null || birthYearStr.isEmpty()) ? null : Integer.parseInt(birthYearStr);

        try (CallableStatement cs = connection.prepareCall("{CALL add_star(?, ?, ?)}")) {
            cs.setString(1, name);
            if (birthYear != null) {
                cs.setInt(2, birthYear);
            } else {
                cs.setNull(2, Types.INTEGER);
            }
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();
            String resultMessage = cs.getString(3);

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", resultMessage);
            response.getWriter().write(responseJsonObject.toString());

        } catch (SQLException e) {
            response.setStatus(500);
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", e.getMessage());
            response.getWriter().write(responseJsonObject.toString());
        }

    }

    private void handleGetMetadata(HttpServletResponse response, Connection connection) throws IOException, SQLException {
        JsonObject responseJsonObject = new JsonObject();
        JsonArray tablesJsonArray = new JsonArray();
        try (PreparedStatement ps = connection.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_schema = 'moviedb'");
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                String tableName = rs.getString("table_name");
                JsonObject tableJsonObject = new JsonObject();
                tableJsonObject.addProperty("table_name", tableName);

                JsonArray columnsJsonArray = new JsonArray();
                try(PreparedStatement psColumns = connection.prepareStatement("SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'moviedb' AND table_name = ?")) {
                    psColumns.setString(1, tableName);
                    try(ResultSet rsColumns = psColumns.executeQuery()) {
                        while(rsColumns.next()) {
                            JsonObject columnJsonObject = new JsonObject();
                            columnJsonObject.addProperty("column_name", rsColumns.getString("column_name"));
                            columnJsonObject.addProperty("data_type", rsColumns.getString("data_type"));
                            columnsJsonArray.add(columnJsonObject);
                        }
                    }
                }
                tableJsonObject.add("columns", columnsJsonArray);
                tablesJsonArray.add(tableJsonObject);
            }
        }
        responseJsonObject.add("metadata", tablesJsonArray);
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        System.out.println("dashboard dopost");
        HttpSession session = request.getSession(false);
        JsonObject responseJsonObject = new JsonObject();
        if(session == null || session.getAttribute("employee") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Unauthorized access");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            if("add_star".equals(action)) {
                handleAddStar(request, response, connection);
            } else if("add_movie".equals(action)) {
                handleAddMovie(request, response, connection);
            } else if("get_metadata".equals(action)) {
                handleGetMetadata(response, connection);
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid Action Parameter.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(responseJsonObject.toString());
            }
        } catch (SQLException e) {
//            e.printStackTrace();
            response.setStatus(500);
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", e.getMessage());
            response.getWriter().write(responseJsonObject.toString());
        }

    }
}
