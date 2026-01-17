package com.et.SudburryApiGateway.repository;


import com.et.SudburryApiGateway.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VerificationTokenRepository<VerificationToken> extends JpaRepository<com.et.SudburryApiGateway.entity.VerificationToken, Long> {
  VerificationToken findByToken(String verificationToken);
}
