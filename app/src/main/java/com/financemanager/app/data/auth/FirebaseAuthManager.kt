package com.financemanager.app.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.financemanager.app.data.model.UserAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class FirebaseAuthManager(private val context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<UserAccount?>(mapFirebaseUser(firebaseAuth.currentUser))
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = mapFirebaseUser(auth.currentUser)
        }
    }

    private fun mapFirebaseUser(user: FirebaseUser?): UserAccount? {
        if (user == null) return null
        val email = user.email ?: ""
        val displayName = user.displayName?.ifBlank { null }
            ?: if (email.isNotEmpty()) email.substringBefore("@") else "User"
        return UserAccount(
            id = user.uid,
            email = email,
            displayName = displayName,
            photoUrl = user.photoUrl?.toString()
        )
    }

    suspend fun signInWithGoogle(webClientId: String): Result<UserAccount> {
        return try {
            val cleanClientId = webClientId.trim()
            if (cleanClientId.isBlank() || cleanClientId.contains("dummy", ignoreCase = true)) {
                return Result.failure(
                    IllegalArgumentException("Please provide your Firebase Web Client ID (from Firebase Console > Authentication > Google).")
                )
            }

            // Generate cryptographic nonce for Credential Manager
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(cleanClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign into real Firebase Auth using the verified Google ID token
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val fUser = authResult.user
                    ?: return Result.failure(IllegalStateException("Firebase User was null after Google authentication."))

                val userAccount = mapFirebaseUser(fUser)!!
                _currentUser.value = userAccount
                saveStoredWebClientId(cleanClientId)
                Result.success(userAccount)
            } else {
                Result.failure(IllegalStateException("Unexpected credential format: ${credential.type}"))
            }
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(Exception("Failed to decode Google credential: ${e.localizedMessage}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserAccount> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val fUser = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase User was null after email sign in."))
            val userAccount = mapFirebaseUser(fUser)!!
            _currentUser.value = userAccount
            Result.success(userAccount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserAccount> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val fUser = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase User was null after sign up."))

            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                fUser.updateProfile(profileUpdates).await()
            }

            val userAccount = mapFirebaseUser(fUser)!!
            _currentUser.value = userAccount
            Result.success(userAccount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    fun getStoredWebClientId(): String {
        val prefs = context.getSharedPreferences("finance_auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("custom_web_client_id", "") ?: ""
    }

    fun saveStoredWebClientId(clientId: String) {
        context.getSharedPreferences("finance_auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("custom_web_client_id", clientId.trim())
            .apply()
    }
}
