package movies;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;



/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb, we use ajax to talk
 * generates json
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "movies.MovieSearchServlet", urlPatterns = "/api/searchResults")
public class MovieSearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            // mysql for this servlet should be ReadOnly
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        //json output
        response.setContentType("application/json");
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();




        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();
            if (dbCon == null) throw new Exception("Database connection failed.");


            //get parameter
            String title = request.getParameter("title");
            String director = request.getParameter("director");
            String star_name = request.getParameter("star_name");
            //genre debug
            String genre = request.getParameter("genre");
            String year_Str = request.getParameter("year");
            Integer year = null;
            if (year_Str != null && !year_Str.trim().isEmpty()) {
                try {
                    year = Integer.parseInt(year_Str.trim());
                } catch (NumberFormatException e) {
                    return;
                }
            }


            //movielist?sortBy=title&titleOrder=desc&ratingOrder=desc
            //ORDER BY rating ASC, title DESC
            String sortBy = (request.getParameter("sortBy") != null) ? request.getParameter("sortBy") : "title";
            String titleOrder = (request.getParameter("titleOrder") != null) ? request.getParameter("titleOrder").trim() : "asc";
            String ratingOrder = (request.getParameter("ratingOrder") != null) ? request.getParameter("ratingOrder").trim() : "asc";

            String sortLogic;
            if (sortBy.equals("title")){
                sortLogic = " ORDER BY " + sortBy + " " + titleOrder + ", rating " + ratingOrder;
            } else{
                sortLogic = " ORDER BY " + sortBy + " " + ratingOrder + ", rating " + titleOrder;
            }


            //Limit and offset
            String pageStr = request.getParameter("page");
            String limitStr = request.getParameter("limit");
            int currentPage = 1;
            int limit = 10;
            try {
                if (pageStr != null) {
                    currentPage = Integer.parseInt(pageStr);
                }
                if (limitStr != null) {
                    limit = Integer.parseInt(limitStr);
                }
            } catch (NumberFormatException e) {
                out.write("{\"error\": \"Invalid currentPage or limit\"}");
                return;
            }
            int offset = (currentPage - 1) * limit;
            String pageLogic = " LIMIT "+limit+" OFFSET "+offset;


            // Generate a SQL query
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT m.id AS movieId, m.title, m.price, m.year, m.director, r.rating, " +
                            "GROUP_CONCAT(DISTINCT CONCAT(g.id, ':', g.name) SEPARATOR ',') AS genres, " +
                            "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name, ':', COALESCE(sc.movie_count, 0)) SEPARATOR ',') AS stars " +
                            "FROM movies m " +
                            "LEFT JOIN genres_in_movies gim ON gim.movieId = m.id " +
                            "LEFT JOIN genres g ON gim.genreId = g.id " +
                            "LEFT JOIN stars_in_movies sim ON sim.movieId = m.id " +
                            "LEFT JOIN stars s ON sim.starId = s.id " +
                            "LEFT JOIN (SELECT starId AS id, COUNT(movieId) AS movie_count FROM stars_in_movies GROUP BY starId) sc " +
                            "ON s.id = sc.id " +
                            "LEFT JOIN ratings r ON m.id = r.movieId " +
                            "WHERE 1=1"
            );
//



            List<Object> params = new ArrayList<>();

//            //Similar Search
//            if (title != null && !title.trim().isEmpty()) {
//                if (title.equals("*%")) {
//                    queryBuilder.append(" AND m.title REGEXP '^[^A-Za-z0-9]'");}
//                else if (title.contains("%")) {
//                    System.out.println("similar search as title"+title);
//                    queryBuilder.append(" AND m.title LIKE ?");
//                    params.add(title);
//                } else if(title.contains("_")) {
//                    System.out.println("similar search as title"+title);
//                    queryBuilder.append(" AND m.title LIKE ?");
//                    params.add(title);
//                } else {
//                    queryBuilder.append(" AND m.title = ?");
//                    params.add(title);
//                }
//
//            }

            if (title != null && !title.trim().isEmpty()) {
                if (title.contains("%")) {
                    System.out.println("similar search as title: " + title);
                    queryBuilder.append(" AND m.title LIKE ?");
                    params.add(title);
                } else {
                    // full text search
                    String[] words = title.trim().split("\\s+");
                    StringBuilder booleanQuery = new StringBuilder();
                    for (String word : words) {
                        if (!word.isEmpty()) {
                            booleanQuery.append("+").append(word).append("* ");
                        }
                    }


                    queryBuilder.append(" AND MATCH(m.title) AGAINST (? IN BOOLEAN MODE)");
                    params.add(booleanQuery.toString().trim());

//                    // threshold
//                    int threshold = Math.max(1, title.trim().length() / 5);
//                    // fuzzy search
//                    queryBuilder.append(" OR e    dth(lower(m.title), lower(?), ")
//                            .append(threshold)
//                            .append(") = true )");
//                    params.add(title);
                }
            }




            if (year != null) {
                queryBuilder.append(" AND m.year = ?");
                params.add(year);
            }


            if (director != null && !director.trim().isEmpty()) {
                if (director.contains("%")) {
                    System.out.println("similar search as director"+director);
                    queryBuilder.append(" AND m.director LIKE ?");
                    params.add(director);
                } else if(director.contains("_")) {
                    System.out.println("similar search as director"+director);
                    queryBuilder.append(" AND m.director LIKE ?");
                    params.add(director);
                } else {
                    queryBuilder.append(" AND m.director = ?");
                    params.add(director);
                }
            }


            if (star_name != null && !star_name.trim().isEmpty()) {
                if (star_name.contains("%")) {
                    System.out.println("similar search as star_name"+star_name);
                    queryBuilder.append(" AND s.name LIKE ?");
                    params.add(star_name);
                } else if(star_name.contains("_")) {
                    System.out.println("similar search as star_name"+star_name);
                    queryBuilder.append(" AND s.name LIKE ?");
                    params.add(star_name);
                } else {
                    queryBuilder.append(" AND s.name = ?");
                    params.add(star_name);
                }
            }


            if (genre != null && !genre.trim().isEmpty()) {
                queryBuilder.append(" AND g.name = ?");
                params.add(genre);
            }


            queryBuilder.append(" GROUP BY m.id");
            queryBuilder.append(sortLogic);
            queryBuilder.append(pageLogic);



            // Set parameters
            PreparedStatement preSt = dbCon.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    preSt.setInt(i + 1, (Integer) params.get(i));
                } else {
                    preSt.setString(i + 1, (String) params.get(i));
                }
            }

            // Log final query
            request.getServletContext().log("Executing SQL: " + preSt.toString());
            // Execute the query
            ResultSet rs = preSt.executeQuery();




            /*movies.name as m_name, movies.id as m_id, movies.year, movies.director," +
            "genres.name as g_name, genres.id as g_id" +
                    "stars.name as s_name, stars.id as s_id" +
                    "ratings.rating" */
            JsonArray moviesArray = new JsonArray(); // To hold all movie JSON objects

            // Iterate through the result set
            while (rs.next()) {
                // Extract movie details
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                double moviePrice = rs.getDouble("price");
                int movieYear = rs.getInt("year");
                String movieDirector = rs.getString("director");
                double movieRating = rs.getDouble("rating");

                // Parse genres (stored as "id:name,id:name,...")
                String genresConcat = rs.getString("genres");
                JsonArray genresArray = new JsonArray();
                if (genresConcat != null && !genresConcat.isEmpty()) {
                    String[] genres = genresConcat.split(",");
                    for (String genre1 : genres) {
                        String[] idName = genre1.split(":");
                        JsonObject genreObj = new JsonObject();
                        genreObj.addProperty("id", idName[0]);   // Genre ID
                        genreObj.addProperty("name", idName[1]); // Genre Name
                        genresArray.add(genreObj);
                    }
                }

                // Parse stars (stored as "id:name,id:name,...")
                String starsConcat = rs.getString("stars");
                JsonArray starsArray = new JsonArray();
                if (starsConcat != null && !starsConcat.isEmpty()) {
                    String[] stars = starsConcat.split(",");
                    for (String star : stars) {
                        String[] idName = star.split(":");
                        JsonObject starObj = new JsonObject();
                        starObj.addProperty("id", idName[0]);   // Star ID
                        starObj.addProperty("name", idName[1]); // Star Name
                        starObj.addProperty("movie_count", Integer.parseInt(idName[2])); // Number of movies played for each Star
                        starsArray.add(starObj);
                    }
                }

                // Create a JSON object for the current movie
                JsonObject movieJson = new JsonObject();
                movieJson.addProperty("id", movieId);
                movieJson.addProperty("price", moviePrice);
                movieJson.addProperty("title", movieTitle);
                movieJson.addProperty("year", movieYear);
                movieJson.addProperty("director", movieDirector);
                movieJson.addProperty("rating", movieRating);
                movieJson.add("genres", genresArray);
                movieJson.add("stars", starsArray);

                // Add the movie JSON object to the movies array
                moviesArray.add(movieJson);
            }

            // Write the JSON array to the response
            out.write(moviesArray.toString());
            response.setStatus(200);

            // Close all structures
            rs.close();
            dbCon.close();



        } catch (Exception e) {
            /*
             * After you deploy the WAR file through tomcat manager webpage,
             *   there's no console to see the print messages.
             * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
             *
             * To view the last n lines (for example, 100 lines) of messages you can use:
             *   tail -100 catalina.out
             * This can help you debug your program after deploying it on AWS.
             */
            request.getServletContext().log("Error: ", e);
            return;
        }
        out.close();
    }
}



