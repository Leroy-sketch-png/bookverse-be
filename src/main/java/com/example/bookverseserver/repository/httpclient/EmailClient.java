package com.example.bookverseserver.repository.httpclient;

import com.example.bookverseserver.dto.request.Authentication.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "email-client",
        url = "https://api.brevo.com",
        configuration = com.example.bookverseserver.configuration.FeignConfig.class
)
public interface EmailClient {
    @PostMapping(
            value = "/v3/smtp/email",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendEmail(@RequestHeader("api-key") String apiKey, @RequestBody EmailRequest body);
}

