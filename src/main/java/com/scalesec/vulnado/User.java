package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
  private static final Logger LOGGER = Logger.getLogger(User.class.getName());
  private String id;
  private String username;
  private String hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.setUsername(username);
    this.setHashedPassword(hashedPassword);
  }

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  private void setUsername(String username) {
    this.username = username;
  }

  public String getHashedPassword() {
    return this.hashedPassword;
  }

  private void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder().setSubject(this.getUsername()).signWith(key).compact();
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parser()
        .setSigningKey(key)
        .parseClaimsJws(token);
    } catch(Exception e) {
      LOGGER.severe(e.getMessage());
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    PreparedStatement stmt = null;
    User user = null;
    try (Connection cxn = Postgres.connection()) {
      LOGGER.info("Opened database successfully");

      String query = "select * from users where username = ? limit 1";
      stmt = cxn.prepareStatement(query);
      stmt.setString(1, un);
      LOGGER.info(query);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(userId, username, password);
      }
    } catch (Exception e) {
      LOGGER.severe(e.getClass().getName()+": "+e.getMessage());
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (Exception e) {
          LOGGER.severe(e.getMessage());
        }
      }
    }
    return user;
  }
}