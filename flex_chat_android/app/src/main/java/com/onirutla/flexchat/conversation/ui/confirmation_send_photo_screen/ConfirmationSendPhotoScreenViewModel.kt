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

package com.onirutla.flexchat.conversation.ui.confirmation_send_photo_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.conversation.domain.repository.AttachmentRepository
import com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen.ConfirmationSendPhotoScreenEvent
import com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen.ConfirmationSendPhotoScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmationSendPhotoScreenViewModel @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ConfirmationSendPhotoScreenState())
    val state = _state.asStateFlow()

    operator fun invoke(uri: Uri) {
        _state.update { it.copy(photoUri = uri) }
    }

    fun onEvent(event: ConfirmationSendPhotoScreenEvent) {
        when (event) {
            ConfirmationSendPhotoScreenEvent.OnSendClick -> {
                // TODO: Implement upload image to storage monitor the progress and return it's download url
                viewModelScope.launch {
//                    attachmentRepository.createAttachment()
                }
            }

            is ConfirmationSendPhotoScreenEvent.OnCaptionChange -> {
                _state.update { it.copy(caption = event.caption) }
            }

            else -> {}
        }
    }

}
