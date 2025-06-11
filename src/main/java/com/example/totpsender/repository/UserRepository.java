package com.example.totpsender.repository;

import com.example.totpsender.model.User;
import com.example.totpsender.model.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Repository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByRole(UserRole role);

    long countByRole(UserRole role);

    List<User> findAllExceptAdmins();
}
