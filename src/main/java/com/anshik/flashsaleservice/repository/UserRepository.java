package com.anshik.flashsaleservice.repository;

import com.anshik.flashsaleservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Used for login and validation
    Optional<User> findByUsername(String username);
}
