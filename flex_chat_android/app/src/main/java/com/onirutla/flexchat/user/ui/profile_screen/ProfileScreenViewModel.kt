/*
 * Copyright 2024 Ricky Alturino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onirutla.flexchat.user.ui.profile_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class ProfileScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileScreenState())
    val state = _state.asStateFlow()

    init {
        authRepository.currentUserFlow
            .onEach { user -> _state.update { it.copy(user = user) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ProfileScreenEvent) {
        when (event) {
            ProfileScreenEvent.OnLogOutClick -> {
                authRepository.signOut()
            }
        }
    }
}
