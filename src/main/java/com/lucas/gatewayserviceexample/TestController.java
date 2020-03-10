package com.lucas.gatewayserviceexample;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;

    @GetMapping("/lucas")
    public String getLucas() {
        return "Hi lucas";
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()
            ));
            log.info("User authenticated with username: {}", authenticationRequest.getUsername());
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            return ResponseEntity.ok(jwtUtil.generateToken(userDetails));
        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", authenticationRequest.getUsername());
            return ResponseEntity.status(403).body("Invalid username or password");
        }
    }
}
