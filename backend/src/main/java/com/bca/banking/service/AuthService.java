package com.bca.banking.service;

import com.bca.banking.dto.LoginRequest;
import com.bca.banking.dto.LoginResponse;
import com.bca.banking.model.User;
import com.bca.banking.repository.UserRepository;
import com.bca.banking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        
        return new LoginResponse(token, user.getId(), user.getUsername(), "Login successful");
    }
}

