package com.lucas.gatewayserviceexample.controllers;

import com.lucas.gatewayserviceexample.entity.User;
import com.lucas.gatewayserviceexample.models.AuthenticationRequest;
import com.lucas.gatewayserviceexample.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UsersRepository usersRepository;

    @PostMapping("/users")
    public void createUser(@RequestBody AuthenticationRequest authenticationRequest) {
        User user = new User(authenticationRequest.getUsername(),  authenticationRequest.getPassword(), List.of("COOl"));
        User newUser = usersRepository.save(user);
        log.info("Created user with username: {}", newUser.getUsername());
    }
}
