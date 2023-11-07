package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comment {

  private static final Logger logger = LoggerFactory.getLogger(Comment.class);
  
  private static final String ID = "id";
  private static final String USERNAME = "username";
  private static final String BODY = "body";
  private static final String CREATED_ON = "createdOn";

  private String id;
  private String username;
  private String body;
  private Timestamp createdOn;

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  public static Comment create(String username, String body){
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement("INSERT INTO comments (" + ID + ", " + USERNAME + ", " + BODY + ", " + CREATED_ON + ") VALUES (?,?,?,?)")) {
      pStatement.setString(1, comment.getId());
      pStatement.setString(2, comment.getUsername());
      pStatement.setString(3, comment.getBody());
      pStatement.setTimestamp(4, comment.getCreatedOn());
      return pStatement.executeUpdate() == 1 ? comment : null;
    } catch (Exception e) {
      logger.error("Unable to save comment", e);
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetchAll() {
    try (Connection con = Postgres.connection();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM comments")) {
      List<Comment> comments = new ArrayList<>();
      while (rs.next()) {
        String id = rs.getString(ID);
        String username = rs.getString(USERNAME);
        String body = rs.getString(BODY);
        Timestamp createdOn = rs.getTimestamp(CREATED_ON);
        comments.add(new Comment(id, username, body, createdOn));
      }
      return comments;
    } catch (SQLException e) {
      logger.error("Error fetching comments", e);
    }
    return new ArrayList<>();
  }

  public static boolean delete(String id) {
    try (Connection con = Postgres.connection();
         PreparedStatement stmt = con.prepareStatement("DELETE FROM comments WHERE id = ?")) {
      stmt.setString(1, id);
      return stmt.executeUpdate() == 1;
    } catch (SQLException e) {
      logger.error("Error deleting comment", e);
    }
    return false;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getBody() {
    return body;
  }

  public Timestamp getCreatedOn() {
    return createdOn;
  }

  private boolean save() throws SQLException {
    //TDB
    return true; 
  }

}