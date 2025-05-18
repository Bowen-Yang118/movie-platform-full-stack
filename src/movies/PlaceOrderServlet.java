package movies;

import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.io.*;
import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet("/api/placeOrder")
public class PlaceOrderServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    //use POST instead of GET
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();
            if (dbCon == null) throw new Exception("Database connection failed.");


            //parameter obtained
            BufferedReader reader = request.getReader();
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            String firstName = json.get("firstName").getAsString();
            String lastName = json.get("lastName").getAsString();
            String creditCard = json.get("creditCard").getAsString();
            String expiry = json.get("expiry").getAsString();
            JsonObject cart = json.getAsJsonObject("cart"); // 购物车数据




            // verify
            String checkCardQuery = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            PreparedStatement checkCardStmt = dbCon.prepareStatement(checkCardQuery);
            checkCardStmt.setString(1, creditCard);
            checkCardStmt.setString(2, firstName);
            checkCardStmt.setString(3, lastName);
            checkCardStmt.setString(4, expiry);

            ResultSet rs = checkCardStmt.executeQuery();
            if (!rs.next()) {
                response.setStatus(400);
                out.write("{\"success\": false, \"message\": \"Invalid payment details.\"}");
                return;
            }

            // use pre-set id rn
            int customerId = 961;
            String insertSalesQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, NOW(), ?)";

            PreparedStatement insertSalesStmt = dbCon.prepareStatement(insertSalesQuery);
            for (String movieId : cart.keySet()) {
                JsonObject movie = cart.getAsJsonObject(movieId);
                int quantity = movie.get("quantity").getAsInt();

                insertSalesStmt.setInt(1, customerId);
                insertSalesStmt.setString(2, movieId);
                insertSalesStmt.setInt(3, quantity);
                insertSalesStmt.executeUpdate();
            }

            out.write("{\"success\": true}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            out.write("{\"success\": false, \"message\": \"Server error. Please try again later.\"}");
        }
    }
}



