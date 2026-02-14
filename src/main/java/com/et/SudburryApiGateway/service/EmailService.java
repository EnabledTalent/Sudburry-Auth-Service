package com.et.SudburryApiGateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.properties.mail.smtp.from:${spring.mail.username:}}")
  private String from;

  public void sendVerificationEmail(String toEmail, String name, String verificationUrl) {
    if (toEmail == null || toEmail.isBlank()) {
      throw new IllegalArgumentException("User email is missing (using username as email)");
    }
    SimpleMailMessage message = new SimpleMailMessage();
    if (from != null && !from.isBlank()) {
      message.setFrom(from);
    }
    message.setTo(toEmail);
    message.setSubject("Verify your email");
    String displayName = (name == null || name.isBlank()) ? "there" : name;
    message.setText(
            "Hi " + displayName + ",\n\n"
                    + "Please verify your email by clicking this link:\n"
                    + verificationUrl + "\n\n"
                    + "If you did not create an account, you can ignore this email.\n"
    );
    mailSender.send(message);
  }
}

