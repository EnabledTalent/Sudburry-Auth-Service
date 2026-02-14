package com.et.SudburryApiGateway.repository;

import com.et.SudburryApiGateway.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

  boolean existsByTokenHash(String tokenHash);

  long deleteByExpiresAtBefore(Date now);
}

