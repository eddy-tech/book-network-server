package com.registration.book.book_network.auth.resource;

import com.registration.book.book_network.auth.dto.AuthenticationRequest;
import com.registration.book.book_network.auth.dto.AuthenticationResponse;
import com.registration.book.book_network.auth.dto.RegistrationRequest;
import com.registration.book.book_network.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationResource {
    private final AuthenticationService authService;

    @PostMapping("/register")
    @ResponseStatus(ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request) throws MessagingException {
        authService.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    @ResponseStatus(OK)
    public ResponseEntity<AuthenticationResponse> authenticate(
          @Valid @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/activate")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        authService.activateAccount(token);
    }
}

