package com.onirutla.flexchat.ui.screens.confirmation_send_photo_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onirutla.flexchat.domain.repository.AttachmentRepository
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
        }
    }

}
