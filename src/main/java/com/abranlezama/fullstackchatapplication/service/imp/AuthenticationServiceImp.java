package com.abranlezama.fullstackchatapplication.service.imp;

import com.abranlezama.fullstackchatapplication.dto.authentication.AuthRequest;
import com.abranlezama.fullstackchatapplication.dto.authentication.TokenResponse;
import com.abranlezama.fullstackchatapplication.model.User;
import com.abranlezama.fullstackchatapplication.repository.UserRepository;
import com.abranlezama.fullstackchatapplication.service.AuthenticationService;
import com.abranlezama.fullstackchatapplication.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImp implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenResponse registerUser(AuthRequest authRequest) {

        Optional<User> userOptional = userRepository.findByUsername(authRequest.username());

        if (userOptional.isPresent()) throw new RuntimeException("Username is taken");

        User user = User.builder()
                .username(authRequest.username())
                .password(passwordEncoder.encode(authRequest.password()))
                .lastUpdated(LocalDateTime.now())
                .build();

        user = userRepository.save(user);


        String jwtToken = jwtService.generateToken(user);

        return new TokenResponse(jwtToken);
    }

    @Override
    public TokenResponse login(AuthRequest authRequest) {
        User user = userRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> new RuntimeException("Wrong credentials"));

        if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            throw new RuntimeException("Wrong credentials");
        }

        return new TokenResponse(jwtService.generateToken(user));
    }
}
