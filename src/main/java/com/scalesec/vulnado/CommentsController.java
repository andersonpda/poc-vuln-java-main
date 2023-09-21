
<Only the complete Code with the correction>
package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import java.util.List;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class CommentsController {
  @Value("${app.secret}")
  private String secret;

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = "application/json")
  List<Comment> comments(@RequestHeader(value="x-auth-token") String token) {
    User.assertAuth(secret, token);
    return Comment.fetch_all();
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/comments", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
  Comment createComment(@RequestHeader(value="x-auth-token") String token, @RequestBody CommentRequest input) {
    return Comment.create(input.username, input.body);
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/comments/{id}", method = RequestMethod.DELETE, produces = "application/json")
  Boolean deleteComment(@RequestHeader(value="x-auth-token") String token, @PathVariable("id") String id) {
    return Comment.delete(id);
  }
}

class CommentRequest implements Serializable {
  public String username;
  public String body;
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequest extends RuntimeException {
  public BadRequest(String exception) {
    super(exception);
  }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ServerError extends RuntimeException {
  public ServerError(String exception) {
    super(exception);
  }
}

class User {
  static Boolean assertAuth(String secret, String token) {
    if (secret.equals(token)) {
      return true;
    } else {
      throw new BadRequest("Invalid token");
    }
  }
}

class Comment {
  static List<Comment> fetch_all() {
    Comment c1 = new Comment(1, "First");
    Comment c2 = new Comment(2, "Second");
    Comment c3 = new Comment(3, "Third");
    return List.of(c1, c2, c3);
  }

  static Comment create(String username, String body) {
    return new Comment(UUID.randomUUID().toString(), username, body);
  }

  static Boolean delete(String id) {
    if (id.equals("1")) {
      return false;
    } else if (id.equals("2")) {
      return true;
    } else {
      throw new ServerError("Invalid comment id");
    }
  }

  final String id;
  final String username;
  final String body;

  private Comment(String id, String username, String body) {
    this.id = id;
    this.username = username;
    this.body = body;
  }
}
