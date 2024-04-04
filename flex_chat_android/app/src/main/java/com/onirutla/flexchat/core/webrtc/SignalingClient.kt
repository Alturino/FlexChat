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

package com.onirutla.flexchat.core.webrtc

import arrow.core.Either
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.onirutla.flexchat.conversation.data.model.OnGoingCallResponse
import com.onirutla.flexchat.core.util.FirebaseCollections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SignalingClient(
    private val firestore: FirebaseFirestore,
) {
    private val signalingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ongoingCallRef = firestore.collection(FirebaseCollections.ONGOING_CALL)

    // session flow to send information about the session state to the subscribers
    private val _sessionStateFlow = MutableStateFlow(WebRTCSessionState.Offline)
    val sessionStateFlow: StateFlow<WebRTCSessionState> = _sessionStateFlow

    // signaling commands to send commands to value pairs to the subscribers
    private val _signalingCommandFlow = MutableSharedFlow<Pair<SignalingCommand, String>>()
    val signalingCommandFlow: SharedFlow<Pair<SignalingCommand, String>> = _signalingCommandFlow

    private val conversationIdStateFlow: MutableStateFlow<String> = MutableStateFlow("")

    init {
        conversationIdStateFlow.filterNot { it.isBlank() or it.isEmpty() }
            .mapNotNull {
                ongoingCallRef.document(it)
                    .get()
                    .await()
                    .toObject<OnGoingCallResponse>()
                    ?.sessionDescription
            }.filterNot { it.isBlank() or it.isEmpty() }
            .onEach {
                when {
                    it.startsWith(SignalingCommand.STATE.name, true) -> handleStateMessage(it)
                    it.startsWith(SignalingCommand.OFFER.name, true) -> handleSignalingCommand(
                        SignalingCommand.OFFER,
                        it
                    )

                    it.startsWith(SignalingCommand.ANSWER.name, true) -> handleSignalingCommand(
                        SignalingCommand.ANSWER,
                        it
                    )

                    it.startsWith(SignalingCommand.ICE.name, true) -> handleSignalingCommand(
                        SignalingCommand.ICE,
                        it
                    )
                }
            }
            .launchIn(signalingScope)
    }

    // TODO: Create signaling client to conversations and target to all conversation member
    fun sendCommand(
        signalingCommand: SignalingCommand,
        sessionDescription: String,
        conversationId: String,
        callInitiatorId: String,
    ) {
        conversationIdStateFlow.update { conversationId }
        Timber.d("[sendCommand] $signalingCommand $sessionDescription")
        signalingScope.launch {
            Either.catch {
                firestore.runTransaction {
                    val onGoingCallResponse = OnGoingCallResponse(
                        conversationId = conversationId,
                        sessionDescription = sessionDescription,
                        signalingCommand = signalingCommand.name,
                        callInitiatorId = callInitiatorId
                    )
                    it.set(ongoingCallRef.document(conversationId), onGoingCallResponse)
                }.await()
            }.onLeft { Timber.e(it) }
                .onRight { Timber.d("Successfully sending the signaling with signalingCommand: $signalingCommand, sessionDescription: $sessionDescription, conversationId: $conversationId") }
        }
    }

    private fun handleStateMessage(message: String) {
        val state = getSeparatedMessage(message)
        _sessionStateFlow.update { WebRTCSessionState.valueOf(state) }
    }

    private fun handleSignalingCommand(command: SignalingCommand, text: String) {
        val value = getSeparatedMessage(text)
        Timber.d("received signaling: $command $value")
        signalingScope.launch {
            _signalingCommandFlow.emit(command to value)
        }
    }

    private fun getSeparatedMessage(text: String) = text.substringAfter(' ')

    fun dispose() {
        _sessionStateFlow.update { WebRTCSessionState.Offline }
        firestore.runTransaction {
            it.delete(
                firestore.collection(FirebaseCollections.ONGOING_CALL)
                    .document(conversationIdStateFlow.value)
            )
        }
        signalingScope.cancel()
    }
}
