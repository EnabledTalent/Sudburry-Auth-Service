package com.et.SudburryApiGateway.repository;

import com.et.SudburryApiGateway.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

  Optional<PasswordResetOtp> findByUsername(String username);

  void deleteByUsername(String username);
}
