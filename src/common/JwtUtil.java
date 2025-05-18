package common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET_KEY = "MY+SECRETKEY+er9dfa8erfjf34dwe5fd5wqqa3adfdf";
    private static final SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));
    private static final long EXPIRATION_TIME = 86400000; // 1 day

    // Generate a JWT token
    // claims can be used as session to store anything related to the user
    public static String generateToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims) // Add custom claims here
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate a JWT token
    // If the token is not valid, a null pointer will be returned
    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // Set the JWT token in cookie
    // If there already exists a cookie with the name "jwtToken", update that cookie
    // If no such cookie found, create a new cookie
    public static void updateJwtCookie(HttpServletRequest request, HttpServletResponse response, String newJwtToken) {
        // Retrieve existing cookies from the request
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    // Update the cookie value
                    cookie.setValue(newJwtToken);
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setMaxAge(24 * 60 * 60);
                    // Uncomment next line if HTTPS is enabled.
                    //cookie.setSecure(true);

                    // Add the updated cookie to the response
                    response.addCookie(cookie);
                    return;
                }
            }
        }

        // If the cookie wasn't found, create it
        Cookie newCookie = new Cookie("jwtToken", newJwtToken);
        newCookie.setHttpOnly(true);
        newCookie.setPath("/");
        newCookie.setMaxAge(24 * 60 * 60);
        //newCookie.setSecure(true);
        response.addCookie(newCookie);
    }

    // Get the cookie from request
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null; // Cookie not found
    }
}
