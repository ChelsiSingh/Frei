package com.frei.app.presentation.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val otpSent: Boolean = false,
    val verificationId: String? = null,
    val isPhoneVerified: Boolean = false,

    val otp: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun otpSent(verificationId: String) {
        _uiState.value = _uiState.value.copy(
            otpSent = true,
            verificationId = verificationId
        )
    }

    fun phoneVerified() {
        _uiState.value = _uiState.value.copy(
            isPhoneVerified = true,
            otpSent = false
        )
    }

    fun setLoading(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = value
        )
    }

    fun setError(message: String?) {
        _uiState.value = _uiState.value.copy(
            error = message
        )
    }

    fun sendOtp(
        activity: Activity,
        phoneNumber: String
    ) {

        setLoading(true)

        repository.sendOtp(
            activity = activity,
            phoneNumber = phoneNumber,
            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {

                    setLoading(false)

                    phoneVerified()
                }

                override fun onVerificationFailed(
                    e: FirebaseException
                ) {

                    setLoading(false)

                    setError(e.message)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {

                    setLoading(false)

                    otpSent(verificationId)
                }
            }
        )
    }

    fun verifyOtp(
        otp: String
    ) {

        val verificationId =
            uiState.value.verificationId ?: return

        setLoading(true)

        repository.verifyOtp(

            verificationId = verificationId,

            otp = otp,

            onSuccess = {

                setLoading(false)

                phoneVerified()

            },

            onFailure = {

                setLoading(false)

                setError(it.message)

            }
        )
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {

            setLoading(true)

            val result = repository.login(
                email = email,
                password = password
            )

            setLoading(false)

            result
                .onSuccess {

                    onSuccess()

                }
                .onFailure {

                    setError(it.message)

                }
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        contact: String,
        gender: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            setLoading(true)

            val result = repository.register(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                contact = contact,
                gender = gender,
                address = address
            )

            setLoading(false)

            result
                .onSuccess {
                    onSuccess()
                }
                .onFailure {
                    setError(it.message)
                }
        }
    }

    val currentUser
        get() = repository.currentUser
}