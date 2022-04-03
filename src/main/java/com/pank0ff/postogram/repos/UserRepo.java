package com.pank0ff.postogram.repos;

import com.pank0ff.postogram.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
