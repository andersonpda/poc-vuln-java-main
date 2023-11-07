package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {
  
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "password123";
  private static final String TOKEN = "token123";

  @CrossOrigin(origins = "https://example.com") // Make CORS safe
  @PostMapping("/login")
  LoginResponse login(@RequestBody LoginRequest input) {
    if (input.username.equals(USERNAME) && input.password.equals(PASSWORD)) {
      return new LoginResponse(TOKEN); 
    } else {
      throw new Unauthorized("Access Denied");
    }
  }

  // Other methods

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  class Unauthorized extends RuntimeException {
    public Unauthorized(String exception) {
      super(exception);
    }
  }

}

class LoginRequest {
  public String username;
  public String password;
}

class LoginResponse {
  public String token;
  
  public LoginResponse(String token) {
    this.token = token;
  }
}