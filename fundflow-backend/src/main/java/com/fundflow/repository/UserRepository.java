package com.fundflow.repository;

import com.fundflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Used to fan out "new campaign pending review" notifications to all admins.
    List<User> findByRole_Name(String roleName);
}
