package parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class InsertEmployee {
    public static void main(String[] args) throws Exception{
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        String employeeEmail = "classta@email.edu";
        String employeePasswd = "classta";
        String employeeFullName = "TA CS122B";
        try {
            // Encrypt the password
            PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
            String encryptedPassword = passwordEncryptor.encryptPassword(employeePasswd);

            // Establish DB connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            // Insert employee with encrypted password
            String query = "INSERT INTO employees (email, password, fullname) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, employeeEmail);
            statement.setString(2, encryptedPassword);
            statement.setString(3, employeeFullName);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Successfully inserted employee with encrypted password.");
            }

            statement.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
