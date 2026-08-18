package com.sahil.hospital.dto;

import com.sahil.hospital.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    private String username;

    private Role role;

    public String getPhoneNumber() {
        return this.phone != null ? this.phone : "";
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phone = phoneNumber;
    }

    public String getUsername() {
        return (this.username != null && !this.username.isBlank()) ? this.username : this.email;
    }

    public static class RegisterRequestBuilder {
        public RegisterRequestBuilder phoneNumber(String phoneNumber) {
            this.phone = phoneNumber;
            return this;
        }
    }
}
