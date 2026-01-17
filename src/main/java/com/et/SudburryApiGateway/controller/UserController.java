package com.et.SudburryApiGateway.controller;


import com.et.SudburryApiGateway.entity.User;
import com.et.SudburryApiGateway.entity.UserDTO;
import com.et.SudburryApiGateway.entity.VerificationToken;
import com.et.SudburryApiGateway.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

  @Autowired
  private UserService _userService;

  @PostMapping("/register")
  public User registerUser(@RequestBody UserDTO userDTO) {

    User user =  _userService.registerUser(userDTO);
    String verificationToken = java.util.UUID.randomUUID().toString();
    String verificationTokenUrl = "http://localhost:8085/verifyRegistrationToken?token=" + verificationToken;
    System.out.println("Please verify your registration by clicking on the following link: " + verificationTokenUrl);
    _userService.saveVerificationToken(user, verificationToken);
    return user;
  }

  @PostMapping("/verifyRegistrationToken")
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
  public String loginUser(@RequestParam String username, @RequestParam String password) {
    return _userService.loginUser(username, password);
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

}


// SERVLET -> MIDDLE LAYER -> CONTROLLER _> SERVICE _> DATABASE