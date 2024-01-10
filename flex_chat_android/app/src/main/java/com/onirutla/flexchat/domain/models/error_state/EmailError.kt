package com.onirutla.flexchat.domain.models.error_state

sealed class EmailError(val message: String) {
    data object EmptyOrBlank : EmailError(message = "Email should not be empty or blank")
    data object NotValidEmail: EmailError(message = "Email should be in valid format")
}
