package com.example.auth_client_kotlin_android_app.data.network

import com.example.auth_client_kotlin_android_app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @DELETE("api/v1/auth/logout")
    suspend fun logoutDelete(): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<RefreshTokenResponse>>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<ApiResponse<Unit>>

    @GET("api/v1/auth/session")
    suspend fun getSession(): Response<ApiResponse<SessionResponse>>

    @GET("api/v1/auth/verify-token")
    suspend fun verifyToken(): Response<ApiResponse<TokenVerifyResponse>>

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
}