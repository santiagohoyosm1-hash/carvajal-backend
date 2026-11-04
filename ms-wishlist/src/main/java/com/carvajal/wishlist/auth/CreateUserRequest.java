package com.carvajal.wishlist.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9._-]{3,50}", message = "El usuario debe tener entre 3 y 50 caracteres válidos")
        String username,
        @NotBlank
        @Size(min = 12, max = 72, message = "La contraseña debe tener entre 12 y 72 caracteres")
        String password) { }
