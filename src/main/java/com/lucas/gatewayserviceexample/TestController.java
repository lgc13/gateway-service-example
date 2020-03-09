package com.lucas.gatewayserviceexample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/lucas")
    public String getLucas() {
        return "Hi lucas";
    }
}
