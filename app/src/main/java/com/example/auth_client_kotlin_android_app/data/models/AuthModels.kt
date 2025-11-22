package com.example.auth_client_kotlin_android_app.data.models

// Request models
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class VerifyEmailRequest(
    val token: String
)

data class ResendVerificationRequest(
    val email: String
)

// Response models
data class AuthResponse(
    val token: TokenData,
    val user: User
)

data class TokenData(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

data class RefreshTokenResponse(
    val accessToken: String?,
    val refreshToken: String?
)

data class SessionResponse(
    val user: User
)

data class TokenVerifyResponse(
    val valid: Boolean
)

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val first_name: String,
    val last_name: String,
    val role: String,
    val is_active: Boolean,
    val is_verified: Boolean,
    val created_at: String,
    val updated_at: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)