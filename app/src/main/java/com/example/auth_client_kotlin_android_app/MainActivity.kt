package com.example.auth_client_kotlin_android_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.auth_client_kotlin_android_app.data.models.User
import com.example.auth_client_kotlin_android_app.ui.auth.AuthActivity
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthViewModel
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthState
import com.example.auth_client_kotlin_android_app.ui.viewmodel.SessionState

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userInfoTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Check if user is logged in
        if (!authViewModel.isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        userInfoTextView = findViewById(R.id.userInfoTextView)
        logoutButton = findViewById(R.id.logoutButton)

        // Setup logout button
        logoutButton.setOnClickListener {
            performLogout()
        }

        // Load stored user data immediately as fallback
        loadStoredUserData()

        // Observe current user data
        observeCurrentUser()

        // Fetch fresh user data from API
        authViewModel.getCurrentUser()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeCurrentUser() {
        authViewModel.currentUser.observe(this, Observer<User?> { user ->
            android.util.Log.d("MainActivity", "Current user observer triggered: $user")
            if (user != null) {
                val verificationStatus = if (user.is_verified) "Verified" else "Not Verified"
                val displayText = """
                    Email: ${user.email}
                    Name: ${user.first_name} ${user.last_name}
                    Role: ${user.role}
                    Status: $verificationStatus
                """.trimIndent()
                android.util.Log.d("MainActivity", "Setting user info text: $displayText")
                userInfoTextView.text = displayText
            } else {
                android.util.Log.d("MainActivity", "User is null, setting 'No user data available'")
                userInfoTextView.text = "No user data available"
            }
        })
    }

    private fun loadStoredUserData() {
        val storedUser = authViewModel.getStoredUser()
        android.util.Log.d("MainActivity", "Stored user loaded: $storedUser")
        if (storedUser != null) {
            authViewModel.setCurrentUser(storedUser)
        } else {
            android.util.Log.w("MainActivity", "No stored user data found")
            userInfoTextView.text = "No user data available"
        }
    }

    private fun performLogout() {
        logoutButton.isEnabled = false
        logoutButton.text = "Logging out..."

        authViewModel.logout()
        authViewModel.authState.observe(this, Observer { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Already handled above
                }
                is AuthState.LoggedOut -> {
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navigateToAuth()
                }
                is AuthState.Error -> {
                    Toast.makeText(this, "Logout failed: ${state.message}", Toast.LENGTH_SHORT).show()
                    logoutButton.isEnabled = true
                    logoutButton.text = "Logout"
                }
                else -> {
                    logoutButton.isEnabled = true
                    logoutButton.text = "Logout"
                }
            }
        })
    }

    private fun navigateToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Check authentication status when app comes back to foreground
        // This handles the case when app is accessed from Recent Apps
        if (!authViewModel.isLoggedIn()) {
            android.util.Log.d("MainActivity", "User not logged in, redirecting to auth")
            navigateToAuth()
            return
        }

        // Refresh user data when app resumes
        authViewModel.getCurrentUser()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle new intent when app is launched from Recent Apps
        android.util.Log.d("MainActivity", "New intent received from Recent Apps")

        // Re-check authentication and refresh data
        if (!authViewModel.isLoggedIn()) {
            navigateToAuth()
            return
        }

        // Process any data from the new intent if needed
        setIntent(intent)
    }

    override fun onRestart() {
        super.onRestart()
        // Called when app is restarted from Recent Apps
        android.util.Log.d("MainActivity", "App restarted from Recent Apps")

        // Ensure we have valid authentication
        if (!authViewModel.isLoggedIn()) {
            navigateToAuth()
            return
        }
    }
}