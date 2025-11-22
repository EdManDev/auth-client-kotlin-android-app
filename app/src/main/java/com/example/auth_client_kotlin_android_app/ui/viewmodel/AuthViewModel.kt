package com.example.auth_client_kotlin_android_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth_client_kotlin_android_app.data.models.AuthResponse
import com.example.auth_client_kotlin_android_app.data.models.SessionResponse
import com.example.auth_client_kotlin_android_app.data.models.TokenVerifyResponse
import com.example.auth_client_kotlin_android_app.data.models.User
import com.example.auth_client_kotlin_android_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _sessionState = MutableLiveData<SessionState>()
    val sessionState: LiveData<SessionState> = _sessionState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun register(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(email, password, name)
            result.fold(
                onSuccess = { authResponse ->
                    _currentUser.value = authResponse.user
                    _authState.value = AuthState.Success(authResponse)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { authResponse ->
                    _currentUser.value = authResponse.user
                    _authState.value = AuthState.Success(authResponse)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }

    fun logout() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.logout()
            result.fold(
                onSuccess = {
                    _currentUser.value = null
                    _authState.value = AuthState.LoggedOut
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Logout failed")
                }
            )
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            val result = authRepository.refreshToken()
            result.fold(
                onSuccess = { refreshResponse ->
                    _authState.value = AuthState.TokenRefreshed
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Token refresh failed")
                }
            )
        }
    }

    fun forgotPassword(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.PasswordResetRequested
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Forgot password failed")
                }
            )
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.resetPassword(token, newPassword)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.PasswordReset
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Reset password failed")
                }
            )
        }
    }

    fun verifyEmail(token: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.verifyEmail(token)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.EmailVerified
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Email verification failed")
                }
            )
        }
    }

    fun resendVerification(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.resendVerification(email)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.VerificationResent
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Resend verification failed")
                }
            )
        }
    }

    fun getSession() {
        _sessionState.value = SessionState.Loading
        viewModelScope.launch {
            val result = authRepository.getSession()
            result.fold(
                onSuccess = { sessionResponse ->
                    _sessionState.value = SessionState.Success(sessionResponse)
                },
                onFailure = { exception ->
                    _sessionState.value = SessionState.Error(exception.message ?: "Get session failed")
                }
            )
        }
    }

    fun verifyToken() {
        viewModelScope.launch {
            val result = authRepository.verifyToken()
            result.fold(
                onSuccess = { tokenResponse ->
                    _sessionState.value = SessionState.TokenVerified(tokenResponse.valid)
                },
                onFailure = { exception ->
                    _sessionState.value = SessionState.Error(exception.message ?: "Token verification failed")
                }
            )
        }
    }

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    fun getStoredUser(): User? {
        return authRepository.getStoredUser()
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                },
                onFailure = { exception ->
                    android.util.Log.e("AuthViewModel", "Failed to get current user", exception)
                    // Keep stored user data as fallback
                }
            )
        }
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val authResponse: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
    object TokenRefreshed : AuthState()
    object PasswordResetRequested : AuthState()
    object PasswordReset : AuthState()
    object EmailVerified : AuthState()
    object VerificationResent : AuthState()
}

sealed class SessionState {
    object Loading : SessionState()
    data class Success(val sessionResponse: SessionResponse) : SessionState()
    data class TokenVerified(val isValid: Boolean) : SessionState()
    data class Error(val message: String) : SessionState()
}