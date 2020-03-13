package com.lucas.gatewayserviceexample.services;

import com.lucas.gatewayserviceexample.entity.User;
import com.lucas.gatewayserviceexample.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(">>> username: " + username);
        User user = usersRepository.findUserByUsername(username);
        System.out.println("Loading user by username: " + user.getUsername());
        return user;
    }
}
