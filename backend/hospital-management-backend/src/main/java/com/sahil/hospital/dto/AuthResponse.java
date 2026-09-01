package com.sahil.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private UserResponse user;

    public long getExpiresInMs() {
        return this.expiresIn;
    }

    public String getUsername() {
        return this.user != null ? this.user.getEmail() : null;
    }

    public String getEmail() {
        return this.user != null ? this.user.getEmail() : null;
    }

    public String getRole() {
        return (this.user != null && this.user.getRole() != null) ? this.user.getRole().name() : null;
    }
}
