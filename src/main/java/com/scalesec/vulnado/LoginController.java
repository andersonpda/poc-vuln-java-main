package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class LoginController {
  @Value("${app.secret}")
  private String secret;

  @CrossOrigin(origins = "http://trustedwebsite.com") // Adjusted for CORS
  @PostMapping(value = "/login", produces = "application/json", consumes = "application/json") // Replaced @RequestMapping with @PostMapping
  LoginResponse login(@RequestBody LoginRequest input) {
    User user = User.fetch(input.getUsername()); // Used accessor method
    if (Postgres.md5(input.getPassword()).equals(user.hashedPassword)) { // Used accessor method
      return new LoginResponse(user.token(secret));
    } else {
      throw new Unauthorized("Access Denied");
    }
  }
}

class LoginRequest implements Serializable {
  private String username; // Made private
  private String password; // Made private

  // Accessor methods
  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }
}

class LoginResponse implements Serializable {
  private static final String token; // Made private and final

  public LoginResponse(String msg) { this.token = msg; }

  // Accessor method
  public static String getToken() {
    return token;
  }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);
  }
}