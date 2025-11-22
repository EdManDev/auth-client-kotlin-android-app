package com.example.auth_client_kotlin_android_app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.auth_client_kotlin_android_app.databinding.FragmentRegisterBinding
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthViewModel
import com.example.auth_client_kotlin_android_app.ui.viewmodel.AuthState

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                authViewModel.register(email, password, name)
            }
        }

        binding.loginTextView.setOnClickListener {
            // Navigate to login fragment
            (activity as? AuthActivity)?.showLoginFragment()
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameEditText.error = "Name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            isValid = false
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (password != confirmPassword) {
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
                    binding.registerButton.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                    // Navigate to main activity or email verification
                    (activity as? AuthActivity)?.onRegistrationSuccess()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}