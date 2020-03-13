package com.lucas.gatewayserviceexample.repository;

import com.lucas.gatewayserviceexample.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
}
