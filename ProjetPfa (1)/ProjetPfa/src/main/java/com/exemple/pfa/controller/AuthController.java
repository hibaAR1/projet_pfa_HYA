package com.exemple.pfa.controller;

import com.exemple.pfa.model.*;
import com.exemple.pfa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq registerReq) {
        try {
            // Check if the email already exists
            if (userRepository.existsByEmail(registerReq.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorRes(HttpStatus.BAD_REQUEST, "Email déjà utilisé"));
            }

            // Create a new user and save to the database
            User newUser = new User(registerReq.getEmail(), registerReq.getPassword());
            newUser.setFirstName(registerReq.getFirstName());
            newUser.setLastName(registerReq.getLastName());
            userRepository.save(newUser);

            // Generate JWT token for the new user
            String token = jwtUtil.createToken(newUser, List.of("ROLE_USER"));
            return ResponseEntity.ok(new RegisterRes(newUser.getEmail(), token));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorRes(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq loginReq) {
        try {
            // Authenticate the user with the provided credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword())
            );

            // Find the user by email
            Optional<User> userOpt = userRepository.findByEmail(loginReq.getEmail());
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorRes(HttpStatus.BAD_REQUEST, "Invalid username or password"));
            }

            User user = userOpt.get();

            // Generate JWT token for the authenticated user
            String token = jwtUtil.createToken(user, List.of("ROLE_USER"));
            return ResponseEntity.ok(new LoginRes(user.getEmail(), token));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorRes(HttpStatus.BAD_REQUEST, "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorRes(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}