package com.frei.app.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        contact: String,
        gender: String,
        address: String
    ): Result<FirebaseUser> {

        return try {
            val authResult = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user!!
            val userId = firebaseUser.uid

            val userDetails = hashMapOf(
                "userId" to userId,
                "firstName" to firstName,
                "lastName" to lastName,
                "contact" to contact,
                "gender" to gender,
                "address" to address,
                "email" to email,
                "createdAt" to FieldValue.serverTimestamp()
            )


            firestore.collection("users")
                .document(userId)
                .set(userDetails)
                .await()



            Result.success(firebaseUser)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {

        return try {

            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success(result.user!!)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        verificationId: String,
        otp: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(
                verificationId,
                otp
            )

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}