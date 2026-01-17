package com.et.SudburryApiGateway.repository;


import com.et.SudburryApiGateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findByUsername(String username);
}
