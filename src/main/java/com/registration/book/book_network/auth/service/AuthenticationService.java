package com.registration.book.book_network.auth.service;

import com.registration.book.book_network.auth.dto.AuthenticationRequest;
import com.registration.book.book_network.auth.dto.AuthenticationResponse;
import com.registration.book.book_network.auth.dto.RegistrationRequest;
import com.registration.book.book_network.core.models.Token;
import com.registration.book.book_network.core.models.User;
import com.registration.book.book_network.role.repository.RoleRepository;
import com.registration.book.book_network.user.repository.TokenRepository;
import com.registration.book.book_network.user.repository.UserRepository;
import com.registration.book.core.security.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static com.registration.book.book_network.core.enums.EmailTemplateName.ACTIVATE_ACCOUNT;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalArgumentException("ROLE USER was not initialized"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }

    private String generateAndSaveActivationToken(User user) {
        //Generate a token
        var generateToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generateToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generateToken;
    }

    private String generateActivationCode(int length) {
        var characters = "0123456789";
        var codeBuilder = new StringBuilder();
        var secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            var randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(
                    characters.charAt(randomIndex)
            );
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate (AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = ((User)auth.getPrincipal());
        claims.put("fullName", user.getFullName());
        var jwtToken = jwtService.generateToken(claims, user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void activateAccount(String token) throws MessagingException {
        var saveToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException(("Invalid token")));

        if(LocalDateTime.now().isAfter(saveToken.getExpiresAt())){
            sendValidationEmail(saveToken.getUser());
            throw new RuntimeException(
                    "Activation token has expired. A new token has been sent to the same email address."
            );
        }

        var user = userRepository.findById(saveToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException(("User not found")));

        user.setEnabled(true);
        userRepository.save(user);
        saveToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(saveToken);
    }
}
