package movies;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "movies.DashboardLoginServlet", urlPatterns = "/_dashboard/login")
public class DashboardLoginServlet extends HttpServlet{
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("doPost");
        String email = request.getParameter("email");
        System.out.println(email);
        String password = request.getParameter("password");
        System.out.println(password);
        JsonObject responseJsonObject = new JsonObject();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT password FROM employees WHERE email=?")) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                // email not exist
                response.setStatus(401);  // Unauthorized
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email does not exist");
                request.getServletContext().log("Dashboard Login failed: Email not found");
            } else {
                // Get the stored plaintext password from the database
                String storedPassword = rs.getString("password");
                boolean isPasswordCorrect = password.equals(storedPassword);
//                String encryptedPassword = rs.getString("password");
//                boolean isPasswordCorrect = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (isPasswordCorrect) {
                    // login success
                    HttpSession session = request.getSession();
                    session.setAttribute("employee", email);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Login successful");
                } else {
                    // wrong password
                    response.setStatus(401);
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect password");
                    request.getServletContext().log("Dashboard Login failed: Incorrect password for email " + email);
                }
            }

            rs.close();
            response.getWriter().write(responseJsonObject.toString());
        } catch (SQLException e) {
            response.setStatus(500);
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Internal server error: " + e.getMessage());
            request.getServletContext().log("Dashboard Login failed: " + e.getMessage());
            response.getWriter().write(responseJsonObject.toString());
        }
    }
}
