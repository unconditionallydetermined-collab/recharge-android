package com.example.recharge.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // Check existing session
        viewModelScope.launch {
            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                _state.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _state.update { it.copy(isLoading = false, isAuthenticated = true) }
            } catch (e: Exception) {
                Timber.e(e, "Sign in failed")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Sign in failed") }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                _state.update { it.copy(isLoading = false, isAuthenticated = true) }
            } catch (e: Exception) {
                Timber.e(e, "Sign up failed")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Sign up failed") }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.resetPasswordForEmail(email)
                _state.update {
                    it.copy(isLoading = false, error = "Password reset email sent — check your inbox.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Password reset failed")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Reset failed") }
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.updateUser { password = newPassword }
                _state.update {
                    it.copy(isLoading = false, error = "Password updated successfully.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Password update failed")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Update failed") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _state.update { AuthUiState() }
            } catch (e: Exception) {
                Timber.e(e, "Sign out failed")
            }
        }
    }
}
