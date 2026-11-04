package com.carvajal.wishlist.auth;

public record AuthUserResponse(Long usuarioId, String username, String role) {
    static AuthUserResponse from(AuthUser user) {
        return new AuthUserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
