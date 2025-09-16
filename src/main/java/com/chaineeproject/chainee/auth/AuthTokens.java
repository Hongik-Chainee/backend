package com.chaineeproject.chainee.auth;

public record AuthTokens(String accessToken, long accessExpEpochSec,
                         String refreshToken, long refreshExpEpochSec) {}
