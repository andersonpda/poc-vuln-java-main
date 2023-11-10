package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class Comment {
  private static final Logger log = Logger.getLogger(Comment.class.getName());
  
  private String commentId;
  private String user;
  private String commentBody;
  private Timestamp commentCreatedOn;

  public Comment(String commentId, String user, String commentBody, Timestamp commentCreatedOn) {
    this.commentId = commentId;
    this.user = user;
    this.commentBody = commentBody;
    this.commentCreatedOn = commentCreatedOn;
  }

  public static Comment create(String user, String commentBody){
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), user, commentBody, timestamp);
    try {
      if (comment.save()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (Exception e) {
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetchAll() {
    List<Comment> comments = new ArrayList<>();
    try (Connection cxn = Postgres.connection();
        Statement stmt = cxn.createStatement()) {
      
      String query = "select * from comments;";
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String commentId = rs.getString("id");
        String user = rs.getString("username");
        String commentBody = rs.getString("body");
        Timestamp commentCreatedOn = rs.getTimestamp("created_on");
        Comment c = new Comment(commentId, user, commentBody, commentCreatedOn);
        comments.add(c);
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.severe(e.getClass().getName()+": "+e.getMessage());
    }
    return comments;
  }

  public static boolean delete(String commentId) {
    try (Connection con = Postgres.connection();
        PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments where id = ?")) {
      pStatement.setString(1, commentId);
      return 1 == pStatement.executeUpdate();
    } catch(Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean save() throws SQLException {
    try (Connection con = Postgres.connection();
        PreparedStatement pStatement = con.prepareStatement("INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)")) {
      pStatement.setString(1, this.commentId);
      pStatement.setString(2, this.user);
      pStatement.setString(3, this.commentBody);
      pStatement.setTimestamp(4, this.commentCreatedOn);
      return 1 == pStatement.executeUpdate();
    }
  }
}