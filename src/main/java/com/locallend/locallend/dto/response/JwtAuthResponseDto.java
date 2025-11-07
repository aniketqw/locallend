package com.locallend.locallend.dto.response;

/**
 * DTO for JWT authentication response.
 * Contains the JWT token and user information.
 */
public class JwtAuthResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private UserResponseDto user;

    public JwtAuthResponseDto() {}

    public JwtAuthResponseDto(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}
