package com.example.auth_client_kotlin_android_app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.auth_client_kotlin_android_app.databinding.FragmentResetPasswordBinding
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthViewModel
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthState

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = arguments?.getString("token")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token?.let { binding.tokenEditText.setText(it) }

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.resetPasswordButton.setOnClickListener {
            val token = binding.tokenEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (validateInput(token, newPassword, confirmPassword)) {
                authViewModel.resetPassword(token, newPassword)
            }
        }

        binding.backToLoginTextView.setOnClickListener {
            // Navigate back to login fragment
            (activity as? AuthActivity)?.showLoginFragment()
        }
    }

    private fun validateInput(token: String, newPassword: String, confirmPassword: String): Boolean {
        var isValid = true

        if (token.isEmpty()) {
            binding.tokenEditText.error = "Reset token is required"
            isValid = false
        }

        if (newPassword.isEmpty()) {
            binding.newPasswordEditText.error = "New password is required"
            isValid = false
        }

        if (newPassword.length < 6) {
            binding.newPasswordEditText.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (newPassword != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.resetPasswordButton.isEnabled = false
                }
                is AuthState.PasswordReset -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resetPasswordButton.isEnabled = true
                    Toast.makeText(context, "Password reset successful. Please login with your new password.", Toast.LENGTH_LONG).show()
                    // Navigate back to login
                    (activity as? AuthActivity)?.showLoginFragment()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resetPasswordButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resetPasswordButton.isEnabled = true
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}