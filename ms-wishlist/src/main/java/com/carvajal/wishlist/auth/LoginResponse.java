package com.carvajal.wishlist.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, Long usuarioId, String role) { }
