package com.et.SudburryApiGateway.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import com.et.SudburryApiGateway.entity.LoginResponse;
import com.et.SudburryApiGateway.entity.PasswordResetOtp;
import com.et.SudburryApiGateway.entity.User;
import com.et.SudburryApiGateway.entity.UserDTO;
import com.et.SudburryApiGateway.entity.VerificationToken;
import com.et.SudburryApiGateway.repository.PasswordResetOtpRepository;
import com.et.SudburryApiGateway.repository.UserRepository;
import com.et.SudburryApiGateway.repository.VerificationTokenRepository;
import com.et.SudburryApiGateway.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService implements UserDetailsService {


  @Autowired
  private UserRepository _userRepository;

  @Autowired
  private VerificationTokenRepository _verificationTokenRepository;

  @Autowired
  private PasswordResetOtpRepository _passwordResetOtpRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private PasswordEncoder _passwordEncoder;

  private static final SecureRandom OTP_RANDOM = new SecureRandom();
  private static final long PASSWORD_RESET_OTP_TTL_MS = 15 * 60 * 1000L;

  public User registerUser(UserDTO userDTO) {
    if (userDTO == null || userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
      throw new IllegalArgumentException("Username (email) is required");
    }

    String normalizedUsername = userDTO.getUsername().trim().toLowerCase(Locale.ROOT);

    if (_userRepository.existsById(normalizedUsername)) {
      throw new IllegalStateException("Username already exists");
    }

    User user = new User();
    user.setEnabled(false);
    user.setUsername(normalizedUsername);
    user.setPassword(_passwordEncoder.encode(userDTO.getPassword()));
    user.setName(userDTO.getName());
    user.setRole(normalizeRole(userDTO.getRole()));
    user.setFirstTimeLogin(true);
    return _userRepository.save(user);

  }

  private static String normalizeRole(String rawRole) {
    if (rawRole == null) return "USER";
    String r = rawRole.trim();
    if (r.isBlank()) return "USER";
    r = r.toUpperCase(Locale.ROOT);
    if (r.startsWith("ROLE_")) {
      r = r.substring("ROLE_".length());
    }
    return r;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String normalizedUsername = username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    User registeredUser = _userRepository.findByUsername(normalizedUsername);
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

  public LoginResponse loginUser(String username, String password) {
    String normalizedUsername = username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    User user = _userRepository.findByUsername(normalizedUsername);
    if (user == null) {
      throw new IllegalArgumentException("User not found");
    }

    if (!user.isEnabled()) {
      throw new IllegalStateException("User not enabled. Please verify your email.");
    }

    String passwordStored = user.getPassword();
    Boolean isPasswordMatch = _passwordEncoder.matches(password, passwordStored);
    if (!isPasswordMatch) {
      throw new IllegalArgumentException("Invalid password");
    }

    String jwt = TokenUtil.generateJwtToken(user);

    boolean firstTimeLogin = user.isFirstTimeLogin();
    if (firstTimeLogin) {
      user.setFirstTimeLogin(false);
      _userRepository.save(user);
    }

    return new LoginResponse(
            new LoginResponse.TokenEnvelope(
                    jwt,
                    new LoginResponse.RoleEnvelope(user.getRole())
            ),
            firstTimeLogin
    );

  }

  /**
   * If the user exists, generates a 6-digit OTP, stores its hash, and emails the code.
   * Does nothing for unknown emails (caller should still return a generic success message).
   */
  @Transactional
  public void requestPasswordResetOtp(String rawEmail) {
    if (rawEmail == null || rawEmail.trim().isEmpty()) {
      throw new IllegalArgumentException("Email is required");
    }
    String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
    User user = _userRepository.findByUsername(normalized);
    if (user == null) {
      return;
    }

    String otp = String.format("%06d", 100_000 + OTP_RANDOM.nextInt(900_000));
    String otpHash = _passwordEncoder.encode(otp);
    Date expiry = new Date(System.currentTimeMillis() + PASSWORD_RESET_OTP_TTL_MS);

    _passwordResetOtpRepository.deleteByUsername(normalized);
    PasswordResetOtp row = new PasswordResetOtp(normalized, otpHash, expiry);
    _passwordResetOtpRepository.save(row);

    emailService.sendPasswordResetOtp(user.getUsername(), user.getName(), otp);
  }

  @Transactional
  public void resetPasswordWithOtp(String rawEmail, String otp, String newPassword) {
    if (rawEmail == null || rawEmail.trim().isEmpty()) {
      throw new IllegalArgumentException("Email is required");
    }
    if (otp == null || otp.trim().isEmpty()) {
      throw new IllegalArgumentException("OTP is required");
    }
    if (newPassword == null || newPassword.length() < 8) {
      throw new IllegalArgumentException("New password must be at least 8 characters");
    }

    String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
    String otpDigits = otp.trim().replaceAll("\\s+", "");

    Optional<PasswordResetOtp> rowOpt = _passwordResetOtpRepository.findByUsername(normalized);
    if (rowOpt.isEmpty()) {
      throw new IllegalArgumentException("Invalid or expired code");
    }

    PasswordResetOtp row = rowOpt.get();
    if (System.currentTimeMillis() > row.getExpiryDate().getTime()) {
      _passwordResetOtpRepository.delete(row);
      throw new IllegalArgumentException("Invalid or expired code");
    }

    if (!_passwordEncoder.matches(otpDigits, row.getOtpHash())) {
      throw new IllegalArgumentException("Invalid or expired code");
    }

    User user = _userRepository.findByUsername(normalized);
    if (user == null) {
      _passwordResetOtpRepository.delete(row);
      throw new IllegalArgumentException("Invalid or expired code");
    }

    user.setPassword(_passwordEncoder.encode(newPassword));
    _userRepository.save(user);
    _passwordResetOtpRepository.delete(row);
  }
}