package com.example.bankingapp.domain.model

sealed class AppError : Exception() {
    data class NetworkError(override val message: String) : AppError()
    data class AuthenticationError(override val message: String) : AppError()
    data class ValidationError(override val message: String) : AppError()
    data class DataNotFoundError(override val message: String) : AppError()
    data class SecurityError(override val message: String) : AppError()
    data class UnknownError(override val message: String) : AppError()
}