package com.bca.banking.controller;

import com.bca.banking.dto.ChangePasswordRequest;
import com.bca.banking.service.UserService;
import com.bca.banking.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            return userService.getUserById(userId)
                    .map(user -> {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("id", user.getId());
                        profile.put("username", user.getUsername());
                        profile.put("firstName", user.getFirstName());
                        profile.put("lastName", user.getLastName());
                        profile.put("email", user.getEmail());
                        return ResponseEntity.ok(profile);
                    })
                    .orElse(ResponseEntity.status(404).body(Map.of("message", "User not found")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching profile"));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            
            boolean success = userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            } else {
                return ResponseEntity.status(400).body(Map.of("message", "Password change failed. Check current password."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error changing password: " + e.getMessage()));
        }
    }
}

