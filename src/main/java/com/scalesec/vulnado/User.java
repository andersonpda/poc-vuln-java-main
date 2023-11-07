package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
  
  private static final Logger logger = LoggerFactory.getLogger(User.class);

  private static final String USER_ID = "user_id";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  
  private String userId;
  private String username;
  private String hashedPassword;

  public User(String userId, String username, String hashedPassword) {
    this.userId = userId;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    //TDB
    return null;
  }

  public static void assertAuth(String secret, String token) {
    //TDB
  }

  public static User fetch(String username) {
    Statement stmt = null;
    User user = null;
    try (Connection cxn = Postgres.connection()) {
      stmt = cxn.createStatement();
      logger.debug("Opened database successfully");

      String query = "select * from users where username = ? limit 1";
      try (PreparedStatement pstmt = cxn.prepareStatement(query)) {
        pstmt.setString(1, username);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            String userId = rs.getString(USER_ID);
            String userUsername = rs.getString(USERNAME);
            String password = rs.getString(PASSWORD);
            user = new User(userId, userUsername, password);
          }
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } 
    return user;
  }
  
  // test methods
  
  public static void testToken() {
    //TDB
  }
  
  public static void testAssertAuth() {
    //TDB
  }
  
  public static void testFetch() {
    //TDB
  }
}