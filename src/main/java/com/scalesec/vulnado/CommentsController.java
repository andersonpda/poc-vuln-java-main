
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
    Comment c1 = new Comment("John", "Hello, this is a comment");
    Comment c2 = new Comment("Mary", "This is another comment");
    return List.of(c1, c2);
  }

  static Comment create(String username, String body) {
    return new Comment(username, body);
  }

  static Boolean delete(String id) {
    if (id.equals("1")) {
      return true;
    } else {
      return false;
    }
  }
}
