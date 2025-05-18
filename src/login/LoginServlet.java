package login;

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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.util.Map;
import java.util.HashMap;
import common.JwtUtil;


@WebServlet(name = "login.LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);


        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();

        // Verify the reCAPTCHA response
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.getMessage());
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (verifyCredentials(username, password)) {
            // Login success:
            // set this user into the session
//            request.getSession().setAttribute("user", new User(username));
            // Prepare claims (session data)
            Map<String, Object> claims = new HashMap<>();

            // Generate JWT
            String token = JwtUtil.generateToken(username, claims);
            JwtUtil.updateJwtCookie(request, response, token);

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");

        } else {
            // Login fail
            responseJsonObject.addProperty("status", "fail");
            // Log to localhost log
            request.getServletContext().log("Login failed");
            responseJsonObject.addProperty("message", "Invalid username or password");
        }
        response.getWriter().write(responseJsonObject.toString());
    }


    // verifies the user's credentials against the database.
    private boolean verifyCredentials(String email, String password) {
        boolean isValidUser = false;


        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT password FROM customers WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Get the stored plaintext password from the database
                String storedPassword = rs.getString("password");
                // Directly compare plaintext passwords
                isValidUser = password.equals(storedPassword);
//                // get the encrypted password from the database
//                String encryptedPassword = rs.getString("password");
//                // use the same encryptor to compare the user input password with encrypted password stored in DB
//                isValidUser = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isValidUser;
    }
}

