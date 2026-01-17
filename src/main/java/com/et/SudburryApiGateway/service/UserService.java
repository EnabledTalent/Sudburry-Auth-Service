package com.et.SudburryApiGateway.service;

import java.util.Date;

import com.et.SudburryApiGateway.entity.User;
import com.et.SudburryApiGateway.entity.UserDTO;
import com.et.SudburryApiGateway.entity.VerificationToken;
import com.et.SudburryApiGateway.repository.UserRepository;
import com.et.SudburryApiGateway.repository.VerificationTokenRepository;
import com.et.SudburryApiGateway.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService implements UserDetailsService {


  @Autowired
  private UserRepository _userRepository;

  @Autowired
  private VerificationTokenRepository _verificationTokenRepository;

  @Autowired
  private PasswordEncoder _passwordEncoder;

  public User registerUser(UserDTO userDTO) {
    User user = new User();
    user.setEnabled(false);
    user.setUsername(userDTO.getUsername());
    user.setPassword(_passwordEncoder.encode(userDTO.getPassword()));
    user.setRole("ADMIN");
    return _userRepository.save(user);

  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User registeredUser = _userRepository.findByUsername(username);
    if (registeredUser == null) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    return org.springframework.security.core.userdetails.User
        .withUsername(registeredUser.getUsername())
        .password(registeredUser.getPassword())
        .roles(registeredUser.getRole())
        .disabled(!registeredUser.isEnabled())
        .build();


  }

  public void saveVerificationToken(User user, String verificationToken) {
    VerificationToken token = new VerificationToken();
    token.setToken(verificationToken);
    token.setUser(user);
    token.setExpiryDate(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
    _verificationTokenRepository.save(token);
  }

  public VerificationToken verifyRegistrationToken(String verificationToken) {
    VerificationToken fetchedToken = (VerificationToken) _verificationTokenRepository.findByToken(verificationToken);
    if (fetchedToken == null) {
      return null;
    }

    long registeredTime = fetchedToken.getExpiryDate().getTime();
    if (System.currentTimeMillis() > registeredTime) {
      _verificationTokenRepository.delete(fetchedToken);
      return null;
    }

    return fetchedToken;


  }

  public void enableUser(VerificationToken token) {
    User fetchedUser = token.getUser();
    fetchedUser.setEnabled(true);
    _userRepository.save(fetchedUser);
    _verificationTokenRepository.delete(token);
  }

  public String loginUser(String username, String password) {
    User user = _userRepository.findByUsername(username);
    if (user == null) {
      return "User not found";
    }

    if (!user.isEnabled()) {
      return "User not enabled. Please verify your email.";
    }

    String passwordStored = user.getPassword();
    Boolean isPasswordMatch = _passwordEncoder.matches(password, passwordStored);
    if (!isPasswordMatch) {
      return "Invalid password";
    }
    return TokenUtil.generateJwtToken(user);

  }
}
