package com.example.test.ui.utils

import android.content.Context
import com.example.test.R
import com.example.test.base.model.ErrorType

/**
 * Extension function to get the localized error message for an ErrorType
 */
fun ErrorType.getErrorMessage(context: Context): String {
    return when (this) {
        is ErrorType.NetworkError -> context.getString(R.string.error_no_internet)
        is ErrorType.GenericError -> context.getString(R.string.error_generic)
    }
} 