package com.example.bankingapp.domain.model

import java.time.LocalDateTime

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val profileImageUrl: String?,
    val dateOfBirth: LocalDateTime?,
    val address: Address?,
    val lastLoginDate: LocalDateTime?,
    val isVerified: Boolean
)