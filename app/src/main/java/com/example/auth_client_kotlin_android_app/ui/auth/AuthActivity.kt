package com.example.auth_client_kotlin_android_app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.auth_client_kotlin_android_app.MainActivity
import com.example.auth_client_kotlin_android_app.R

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (savedInstanceState == null) {
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (Intent.ACTION_VIEW == it.action && "authclient" == it.data?.scheme && "reset-password" == it.data?.host) {
                val token = it.data?.getQueryParameter("token")
                showResetPasswordFragment(token)
            } else {
                showLoginFragment()
            }
        } ?: showLoginFragment()
    }

    fun showLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    fun showRegisterFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showForgotPasswordFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ForgotPasswordFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showResetPasswordFragment(token: String? = null) {
        val fragment = ResetPasswordFragment()
        token?.let {
            val bundle = Bundle()
            bundle.putString("token", it)
            fragment.arguments = bundle
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun onLoginSuccess() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun onRegistrationSuccess() {
        // You can either go back to login or show a verification message
        supportFragmentManager.popBackStack()
    }
}