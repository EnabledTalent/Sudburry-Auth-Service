
package com.et.SudburryApiGateway.config;

import com.et.SudburryApiGateway.service.TokenRevocationService;
import com.et.SudburryApiGateway.util.TokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;



@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private TokenRevocationService tokenRevocationService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    String path = request.getRequestURI();

    if (path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui.html")
            || path.startsWith("/webjars")
            || path.startsWith("/register")
            || path.startsWith("/signin")
            || path.startsWith("/verifyRegistrationToken")) {
      filterChain.doFilter(request, response);
      return;
    }

    if (authHeader == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Missing Authorization header");
      return;
    }

    // Extract token from "Bearer <token>" format
    String jwtToken = authHeader;
    if (authHeader.startsWith("Bearer ")) {
      jwtToken = authHeader.substring(7);
    }

    // VALIDATE THE TOKEN
    final Claims claims;
    try {
      claims = TokenUtil.validateSignedToken(jwtToken);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Invalid or expired JWT token");
      return;
    }

    if (tokenRevocationService != null && tokenRevocationService.isTokenRevoked(jwtToken)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("JWT token has been revoked");
      return;
    }

    String username = claims.getSubject();
    String role = claims.get("role", String.class);
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);

  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.contains("register") || path.contains("signin") || path.contains("verifyRegistrationToken");
  }
}
