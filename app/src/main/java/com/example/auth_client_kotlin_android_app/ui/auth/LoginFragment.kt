package com.example.auth_client_kotlin_android_app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.auth_client_kotlin_android_app.databinding.FragmentLoginBinding
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthViewModel
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthState

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        binding.registerTextView.setOnClickListener {
            // Navigate to register fragment
            (activity as? AuthActivity)?.showRegisterFragment()
        }

        binding.forgotPasswordTextView.setOnClickListener {
            // Navigate to forgot password fragment
            (activity as? AuthActivity)?.showForgotPasswordFragment()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    // Navigate to main activity
                    (activity as? AuthActivity)?.onLoginSuccess()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is AuthState.PasswordResetRequested -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}