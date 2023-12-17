package com.onirutla.flexchat.ui.screens.login_screen

import androidx.lifecycle.ViewModel
import com.onirutla.flexchat.core.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

}
