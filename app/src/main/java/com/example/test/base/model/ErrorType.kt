package com.example.test.base.model

/**
 * Sealed class representing different types of errors that can occur in the app.
 * This is a common error type that can be used across different features.
 */
sealed class ErrorType {
    /**
     * Represents network-related errors like no internet connection,
     * connection timeout, etc.
     */
    object NetworkError : ErrorType()

    /**
     * Represents all other types of errors that are not specifically
     * handled by other error types
     */
    object GenericError : ErrorType()

} 