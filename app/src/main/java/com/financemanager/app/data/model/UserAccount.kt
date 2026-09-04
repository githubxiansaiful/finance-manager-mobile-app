package com.financemanager.app.data.model

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)
