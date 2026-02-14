package com.et.SudburryApiGateway.controller;


import com.et.SudburryApiGateway.entity.User;
import com.et.SudburryApiGateway.entity.UserDTO;
import com.et.SudburryApiGateway.entity.UserInfoResponse;
import com.et.SudburryApiGateway.entity.LoginResponse;
import com.et.SudburryApiGateway.entity.VerificationToken;
import com.et.SudburryApiGateway.repository.UserRepository;
import com.et.SudburryApiGateway.service.EmailService;
import com.et.SudburryApiGateway.service.UserService;
import com.et.SudburryApiGateway.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(
        origins = "http://localhost:3000",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE }
)
public class UserController {

  @Autowired
  private UserService _userService;

  @Autowired
  private UserRepository _userRepository;

  @Autowired
  private EmailService emailService;

  @Value("${app.verification.base-url:http://localhost:8081}")
  private String verificationBaseUrl;

  @PostMapping("/register")
  public User registerUser(@RequestBody UserDTO userDTO) {

    User user =  _userService.registerUser(userDTO);
    String verificationToken = java.util.UUID.randomUUID().toString();
    String verificationTokenUrl = verificationBaseUrl + "/verifyRegistrationToken?token=" + verificationToken;
    _userService.saveVerificationToken(user, verificationToken);

    // Send mail with the verification URL (username is treated as the email)
    emailService.sendVerificationEmail(user.getUsername(), userDTO.getName(), verificationTokenUrl);

    return user;
  }

  @GetMapping("/verifyRegistrationToken")
  public String verifyRegistration(@RequestParam("token") String verificationToken) {
    VerificationToken token = _userService.verifyRegistrationToken(verificationToken);
    if (token != null) {
      _userService.enableUser(token);
      return "Token verification successful, user enabled. Please login to proceed.";
    } else {
      return "Token verification failed. Please try again.";
    }
  }

  @PostMapping("/signin")
  public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
    try {
      LoginResponse response = _userService.loginUser(username, password);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
    }
  }

  @GetMapping("/test")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public String test() {
//    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    String username = authentication.getName();
//    authentication.getAuthorities().forEach(authority -> {
//      System.out.println("User Authority: " + authority.getAuthority());
//    });
//    if (authentication.getAuthorities().isEmpty()) {
//      return "No authorities found for user: " + username;
//    }
//    if (authentication.getAuthorities().toString() == "[ROLE_ADMIN]") {
//      return "Test endpoint accessed successfully by ADMIN user: " + username;
//    }
//
//    return "Test endpoint access denied for user: " + username;
    return "Test endpoint accessed successfully by ADMIN user.";
  }

  @GetMapping("/hello")
  public String hello() {
    return "Hello, World!";
  }

  @GetMapping("/userinfo")
  public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
    try {
      // Extract token from "Bearer <token>" format
      if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Missing or invalid Authorization header. Expected format: Bearer <token>");
      }

      String token = authorizationHeader.substring(7); // Remove "Bearer " prefix

      // Validate the token
      Claims claims = TokenUtil.validateSignedToken(token);
      if (claims == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid or expired JWT token");
      }

      // Extract username and role from token
      String username = claims.getSubject();
      String role = claims.get("role", String.class);

      // Fetch user from database to get the name
      User user = _userRepository.findByUsername(username);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found");
      }

      // Create and return response
      UserInfoResponse response = new UserInfoResponse(
              username,
              user.getName(),
              role
      );

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Error processing request: " + e.getMessage());
    }
  }

}


// SERVLET -> MIDDLE LAYER -> CONTROLLER _> SERVICE _> DATABASE