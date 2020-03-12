package com.lucas.gatewayserviceexample.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AuthenticationRequest {

    private final String username;
    private final String password;

}
