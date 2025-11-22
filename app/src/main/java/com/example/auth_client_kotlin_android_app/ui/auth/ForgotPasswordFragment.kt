package com.example.auth_client_kotlin_android_app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.auth_client_kotlin_android_app.databinding.FragmentForgotPasswordBinding
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthViewModel
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthState

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()

            if (validateInput(email)) {
                authViewModel.forgotPassword(email)
            }
        }

        binding.backToLoginTextView.setOnClickListener {
            // Navigate back to login fragment
            (activity as? AuthActivity)?.showLoginFragment()
        }
    }

    private fun validateInput(email: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
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
                is AuthState.PasswordResetRequested -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resetPasswordButton.isEnabled = true
                    Toast.makeText(context, "Password reset email sent. Check your inbox and follow the instructions.", Toast.LENGTH_LONG).show()
                    // Stay on this fragment or navigate back to login
                    // User will typically receive email and come back to reset password
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