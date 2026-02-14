package com.et.SudburryApiGateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

  @Value("${app.email.brevo.api-key:}")
  private String brevoApiKey;

  @Value("${app.email.brevo.from-email:}")
  private String brevoFromEmail;

  @Value("${app.email.brevo.from-name:Sudbury City}")
  private String brevoFromName;

  public void sendVerificationEmail(String toEmail, String name, String verificationUrl) {
    if (toEmail == null || toEmail.isBlank()) {
      throw new IllegalArgumentException("User email is missing (using username as email)");
    }
    String displayName = (name == null || name.isBlank()) ? "there" : name;
    String subject = "Verify your email";
    String text =
            "Hi " + displayName + ",\n\n"
                    + "Please verify your email by clicking this link:\n"
                    + verificationUrl + "\n\n"
                    + "If you did not create an account, you can ignore this email.\n";

    // Brevo (HTTP over 443) - works on Render.
    if (brevoApiKey == null || brevoApiKey.isBlank() || brevoFromEmail == null || brevoFromEmail.isBlank()) {
      System.out.println("Brevo email not sent (BREVO_API_KEY/BREVO_FROM_EMAIL missing). Verification URL: " + verificationUrl);
      return;
    }

    // Common misconfig: SMTP key (xsmtpsib-...) is NOT the same as API key (xkeysib-...)
    String key = brevoApiKey.trim();
    if (key.startsWith("xsmtpsib-")) {
      throw new IllegalStateException(
              "Brevo key looks like an SMTP key (xsmtpsib-...). " +
                      "For HTTP API you must use an API v3 key (xkeysib-...)."
      );
    }

    sendViaBrevo(toEmail, subject, text);
  }

  private void sendViaBrevo(String toEmail, String subject, String text) {
    try {
      HttpClient client = HttpClient.newHttpClient();

      String body = "{"
              + "\"sender\":{\"name\":\"" + jsonEscape(brevoFromName) + "\",\"email\":\"" + jsonEscape(brevoFromEmail) + "\"},"
              + "\"to\":[{\"email\":\"" + jsonEscape(toEmail) + "\"}],"
              + "\"subject\":\"" + jsonEscape(subject) + "\","
              + "\"textContent\":\"" + jsonEscape(text) + "\""
              + "}";

      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
              .header("api-key", brevoApiKey)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        throw new IllegalStateException("Brevo API failed with status " + status + ": " + response.body());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to send email via Brevo: " + e.getMessage(), e);
    }
  }

  private static String jsonEscape(String s) {
    if (s == null) return "";
    return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
  }
}

