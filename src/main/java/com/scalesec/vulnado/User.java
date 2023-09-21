
<Only the complete Code with the correction>

Fix Make sure using a dynamically formatted SQL query is safe here.
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    try {
      // Check if the user is logged in
      if (isLoggedIn()) {
        String jws = Jwts.builder()
         .setSubject(this.username)
         .signWith(key)
         .compact();
        return jws;
      } else {
        // User is not logged in - return null
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      // Handle the exception (e.g., log the error, throw a custom exception, etc.)
      return null;
    }
  }

  private boolean isLoggedIn() {
    // Implement your logic to check if the user is logged in (e.g., by verifying a session, checking a database, etc.)
    return true; // For demonstration purposes, assume the user is logged in
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parser()
       .setSigningKey(key)
       .parseClaimsJws(token);
    } catch(Exception e) {
      e.printStackTrace();
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    Statement stmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      stmt = cxn.createStatement();
      System.out.println("Opened database successfully");

      String query = "select * from users where username = '" + un + "' limit 1";
      System.out.println(query);
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
    } finally {
      return user;
    }
  }
}