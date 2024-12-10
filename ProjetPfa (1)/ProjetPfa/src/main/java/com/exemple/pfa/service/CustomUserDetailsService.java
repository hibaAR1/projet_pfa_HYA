package com.exemple.pfa.service;

import com.exemple.pfa.repository.UserRepository;
import com.exemple.pfa.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the user using Optional and handle it
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));

        // Instead of adding "ROLE_USER", add "USER" only
        List<String> roles = new ArrayList<>();
        roles.add("USER"); // Spring Security will automatically prefix it with "ROLE_"

        // Return the user with roles and password
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(roles.toArray(new String[0])) // Convert List to Array
                .build();
    }
}