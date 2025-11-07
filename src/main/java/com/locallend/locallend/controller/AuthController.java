package com.locallend.locallend.controller;

import com.locallend.locallend.dto.request.UserLoginDto;
import com.locallend.locallend.dto.request.UserRegistrationDto;
import com.locallend.locallend.dto.response.JwtAuthResponseDto;
import com.locallend.locallend.dto.response.UserResponseDto;
import com.locallend.locallend.model.User;
import com.locallend.locallend.security.JwtTokenProvider;
import com.locallend.locallend.service.UserService;
import com.locallend.locallend.util.UserMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles user registration and login with JWT token generation.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user.
     * @param dto User registration data
     * @return Created user response DTO
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        User user = userService.registerUser(dto);
        UserResponseDto response = userMapper.toUserResponseDto(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and login with JWT token.
     * @param dto Login credentials (username/email and password)
     * @return JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@Valid @RequestBody UserLoginDto dto) {
        User user = userService.verifyCredentials(dto.getUsernameOrEmail(), dto.getPassword());
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername());
        
        // Prepare response with token and user data
        UserResponseDto userResponse = userMapper.toUserResponseDto(user);
        JwtAuthResponseDto response = new JwtAuthResponseDto(token, userResponse);
        
        return ResponseEntity.ok(response);
    }
}
