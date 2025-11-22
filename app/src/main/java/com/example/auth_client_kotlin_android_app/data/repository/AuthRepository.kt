package com.example.auth_client_kotlin_android_app.data.repository

import com.example.auth_client_kotlin_android_app.data.models.*
import com.example.auth_client_kotlin_android_app.data.network.AuthApiService
import com.example.auth_client_kotlin_android_app.data.network.RetrofitClient
import com.example.auth_client_kotlin_android_app.data.network.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val apiService: AuthApiService = RetrofitClient.authApiService) {

    suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(email, password, name))
                android.util.Log.d("AuthRepository", "Register response code: ${response.code()}")
                android.util.Log.d("AuthRepository", "Register response body: ${response.body()}")
                android.util.Log.d("AuthRepository", "Register response error body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    android.util.Log.d("AuthRepository", "Response body success: ${responseBody?.success}")
                    android.util.Log.d("AuthRepository", "Response body data: ${responseBody?.data}")
                    android.util.Log.d("AuthRepository", "Response body message: ${responseBody?.message}")

                    // Check API-level success
                    if (responseBody?.success == true) {
                        responseBody.data?.let { authResponse ->
                            android.util.Log.d("AuthRepository", "Registration response: accessToken=${authResponse.token.access_token.take(10)}...")
                            if (authResponse.token.access_token.isNotEmpty()) {
                                // Since API doesn't provide refresh token, we'll use access token as refresh token for now
                                TokenManager.saveTokens(authResponse.token.access_token, authResponse.token.access_token)
                                // Save user data for offline access
                                val gson = Gson()
                                val userJson = gson.toJson(authResponse.user)
                                TokenManager.saveUserData(userJson)
                                Result.success(authResponse)
                            } else {
                                Result.failure(Exception("Registration failed: Missing access token in response"))
                            }
                        } ?: Result.failure(Exception("Registration failed: No data in response.data"))
                    } else {
                        // API returned success: false with error message
                        val errorMessage = responseBody?.message ?: "Registration failed: Unknown error"
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    Result.failure(Exception("Registration failed: ${response.message()} (${response.code()})"))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Register exception", e)
                Result.failure(e)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                android.util.Log.d("AuthRepository", "Login response code: ${response.code()}")
                android.util.Log.d("AuthRepository", "Login response body: ${response.body()}")
                android.util.Log.d("AuthRepository", "Login response error body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    android.util.Log.d("AuthRepository", "Response body success: ${responseBody?.success}")
                    android.util.Log.d("AuthRepository", "Response body data: ${responseBody?.data}")
                    android.util.Log.d("AuthRepository", "Response body message: ${responseBody?.message}")

                    // Check API-level success
                    if (responseBody?.success == true) {
                        responseBody.data?.let { authResponse ->
                            android.util.Log.d("AuthRepository", "Login response: accessToken=${authResponse.token.access_token.take(10)}...")
                            if (authResponse.token.access_token.isNotEmpty()) {
                                // Since API doesn't provide refresh token, we'll use access token as refresh token for now
                                TokenManager.saveTokens(authResponse.token.access_token, authResponse.token.access_token)
                                // Save user data for offline access
                                val gson = Gson()
                                val userJson = gson.toJson(authResponse.user)
                                TokenManager.saveUserData(userJson)
                                Result.success(authResponse)
                            } else {
                                Result.failure(Exception("Login failed: Missing access token in response"))
                            }
                        } ?: Result.failure(Exception("Login failed: No data in response.data"))
                    } else {
                        // API returned success: false with error message
                        val errorMessage = responseBody?.message ?: "Login failed: Unknown error"
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    Result.failure(Exception("Login failed: ${response.message()} (${response.code()})"))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Login exception", e)
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.logout()
                TokenManager.clearTokens()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Logout failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                TokenManager.clearTokens()
                Result.failure(e)
            }
        }
    }

    suspend fun refreshToken(): Result<RefreshTokenResponse> {
        return withContext(Dispatchers.IO) {
            val currentToken = TokenManager.getAccessToken()
            if (currentToken == null) {
                return@withContext Result.failure(Exception("No access token available"))
            }

            // Since the API doesn't provide refresh tokens, we'll simulate token refresh
            // by validating the current token. In a real implementation, you might want to
            // implement a different refresh mechanism or contact your backend team.
            try {
                val response = apiService.verifyToken()
                if (response.isSuccessful) {
                    response.body()?.data?.let { tokenResponse ->
                        if (tokenResponse.valid) {
                            // Token is still valid, return a mock refresh response
                            val mockResponse = RefreshTokenResponse(
                                accessToken = currentToken,
                                refreshToken = currentToken
                            )
                            Result.success(mockResponse)
                        } else {
                            TokenManager.clearTokens()
                            Result.failure(Exception("Token is no longer valid"))
                        }
                    } ?: Result.failure(Exception("Token refresh failed: No data"))
                } else {
                    TokenManager.clearTokens()
                    Result.failure(Exception("Token refresh failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                TokenManager.clearTokens()
                Result.failure(e)
            }
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Forgot password failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.resetPassword(ResetPasswordRequest(token, newPassword))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Reset password failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun verifyEmail(token: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.verifyEmail(VerifyEmailRequest(token))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Email verification failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun resendVerification(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.resendVerification(ResendVerificationRequest(email))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Resend verification failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSession(): Result<SessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSession()
                android.util.Log.d("AuthRepository", "Get session response code: ${response.code()}")
                android.util.Log.d("AuthRepository", "Get session response body: ${response.body()}")
                android.util.Log.d("AuthRepository", "Get session response error body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    android.util.Log.d("AuthRepository", "Session response body success: ${responseBody?.success}")
                    android.util.Log.d("AuthRepository", "Session response body data: ${responseBody?.data}")
                    android.util.Log.d("AuthRepository", "Session response body message: ${responseBody?.message}")

                    if (responseBody?.success == true) {
                        responseBody.data?.let { sessionResponse ->
                            android.util.Log.d("AuthRepository", "Session user: ${sessionResponse.user}")
                            Result.success(sessionResponse)
                        } ?: Result.failure(Exception("Get session failed: No data in response.data"))
                    } else {
                        val errorMessage = responseBody?.message ?: "Get session failed: Unknown error"
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    Result.failure(Exception("Get session failed: ${response.message()} (${response.code()})"))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Get session exception", e)
                Result.failure(e)
            }
        }
    }

    suspend fun verifyToken(): Result<TokenVerifyResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.verifyToken()
                if (response.isSuccessful) {
                    response.body()?.data?.let { tokenResponse ->
                        Result.success(tokenResponse)
                    } ?: Result.failure(Exception("Token verification failed: No data"))
                } else {
                    Result.failure(Exception("Token verification failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()
                android.util.Log.d("AuthRepository", "Get current user response code: ${response.code()}")
                android.util.Log.d("AuthRepository", "Get current user response body: ${response.body()}")
                android.util.Log.d("AuthRepository", "Get current user response error body: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    android.util.Log.d("AuthRepository", "User response body success: ${responseBody?.success}")
                    android.util.Log.d("AuthRepository", "User response body data: ${responseBody?.data}")
                    android.util.Log.d("AuthRepository", "User response body message: ${responseBody?.message}")

                    if (responseBody?.success == true) {
                        responseBody.data?.let { user ->
                            android.util.Log.d("AuthRepository", "Current user: $user")
                            // Save updated user data
                            val gson = Gson()
                            val userJson = gson.toJson(user)
                            TokenManager.saveUserData(userJson)
                            Result.success(user)
                        } ?: Result.failure(Exception("Get current user failed: No data in response.data"))
                    } else {
                        val errorMessage = responseBody?.message ?: "Get current user failed: Unknown error"
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    Result.failure(Exception("Get current user failed: ${response.message()} (${response.code()})"))
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Get current user exception", e)
                Result.failure(e)
            }
        }
    }

    fun getStoredUser(): User? {
        val userJson = TokenManager.getUserData()
        return if (userJson != null) {
            try {
                val gson = Gson()
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Failed to parse stored user data", e)
                null
            }
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return TokenManager.hasValidTokens()
    }
}