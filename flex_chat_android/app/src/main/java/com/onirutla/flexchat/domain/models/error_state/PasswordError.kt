package com.onirutla.flexchat.domain.models.error_state

sealed class PasswordError(val message: String) {
    data object TooShort : PasswordError(message = "Password should be at least more than or equal to 8 characters")
    data object NotContainsDigit : PasswordError(message = "Password should be at least have 1 digit number")
    data object NotContainsUppercase : PasswordError(message = "Password should be at least have 1 letter uppercase")
    data object NotContainsLowercase : PasswordError(message = "Password should be at least have 1 letter lowercase")
    data object NotContainNonAlphaNum: PasswordError(message = "Password should be at least have 1 character non alpha numeric")
    data object EmptyOrBlank : PasswordError(message = "Password should not be empty or blank")
    data object PasswordNotTheSameWithConfirmationPassword: PasswordError(message = "Password not the same with confirmation password")
    data object ConfirmationPasswordNotTheSameWithPassword: PasswordError(message = "Confirmation password not the same with password")
}
