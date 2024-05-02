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

package com.onirutla.flexchat.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import arrow.core.getOrElse
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onirutla.flexchat.MainActivity
import com.onirutla.flexchat.R
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.data.model.Conversation
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.conversation.ui.call.CallActivity
import com.onirutla.flexchat.core.util.NotificationConstants.CALL_NOTIFICATION_CHANNEL_ID
import com.onirutla.flexchat.core.util.NotificationConstants.CALL_NOTIFICATION_CHANNEL_NAME
import com.onirutla.flexchat.core.util.NotificationConstants.CALL_NOTIFICATION_ID
import com.onirutla.flexchat.core.util.NotificationConstants.CHAT_NOTIFICATION_CHANNEL_ID
import com.onirutla.flexchat.core.util.NotificationConstants.CHAT_NOTIFICATION_CHANNEL_NAME
import com.onirutla.flexchat.core.util.NotificationConstants.CHAT_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
internal class ChatNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var conversationRepository: ConversationRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private val coroutineScope = CoroutineScope(SupervisorJob())

    private lateinit var notificationManager: NotificationManagerCompat

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Refreshed token: $token")
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(applicationContext)
        createNotificationChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("From: ${message.from}")
        Timber.d("Message data payload: ${message.data}")
        Timber.d("Message notification body: ${message.notification?.body}")

        coroutineScope.launch {
            val currentUser = authRepository.getCurrentUser()
            Timber.d("currentUser: $currentUser")
            if (message.data.containsKey("notificationType") && message.data["notificationType"] == NotificationType.Message.name) {
                if (currentUser.id != message.data["userId"]) {
                    Timber.d("creating chat notification")
                    createChatNotification(message, notificationManager)
                    Timber.d("complete chat notification creation")
                }
            }
            if (message.data.containsKey("notificationType") && message.data["notificationType"] == NotificationType.Call.name) {
                val conversationId = message.data["conversationId"].orEmpty()
                val callInitiatorId = message.data["callInitiatorId"].orEmpty()
                val conversation = conversationRepository.getConversationById(conversationId)
                    .onLeft { Timber.e(it) }
                    .getOrElse { throw it }
                Timber.d("currentUserId: ${currentUser.id}, callInitiatorId: $callInitiatorId, conversationId: $conversationId, conversation: $conversation")
                if (currentUser.id != message.data["callInitiatorId"]) {
                    Timber.d("creating incoming call notification")
                    incomingCallNotification(
                        message,
                        notificationManager,
                        conversation,
                        callInitiatorId
                    )
                    Timber.d("finished incoming call notification creation")
                }
            }
        }
    }

    private fun createPendingIntent(notificationType: NotificationType): PendingIntent =
        when (notificationType) {
            NotificationType.Message -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                PendingIntent.getActivity(
                    applicationContext,
                    CHAT_NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            NotificationType.Call -> {
                val intent = Intent(applicationContext, CallActivity::class.java)
                PendingIntent.getActivity(
                    applicationContext,
                    CALL_NOTIFICATION_ID,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        }

    private fun createNotificationBuilder(
        title: String,
        body: String,
        notificationType: NotificationType,
    ): NotificationCompat.Builder {
        val pendingIntent = createPendingIntent(notificationType)
        val notificationChannelId = when (notificationType) {
            NotificationType.Message -> CHAT_NOTIFICATION_CHANNEL_ID
            NotificationType.Call -> CALL_NOTIFICATION_CHANNEL_ID
        }
        val notificationBuilder = NotificationCompat
            .Builder(applicationContext, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setChannelId(notificationChannelId)
        return notificationBuilder
    }

    private fun isPermissionNotGranted(): Boolean {
        val result = ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        )
        return result != PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        val chatNotificationChannel = NotificationChannelCompat
            .Builder(CHAT_NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(CHAT_NOTIFICATION_CHANNEL_NAME)
            .build()

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setLegacyStreamType(AudioManager.STREAM_RING)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        val callNotificationChannel = NotificationChannelCompat
            .Builder(CALL_NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(CALL_NOTIFICATION_CHANNEL_NAME)
            .setVibrationEnabled(true)
            .setSound(ringtoneUri, audioAttributes)
            .build()

        notificationManager.apply {
            createNotificationChannel(chatNotificationChannel)
            createNotificationChannel(callNotificationChannel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun createChatNotification(
        message: RemoteMessage,
        notificationManager: NotificationManagerCompat,
    ) {
        val notificationType: NotificationType = NotificationType.Message
        val notificationBuilder = createNotificationBuilder(
            message.notification?.title.orEmpty(),
            message.notification?.body.orEmpty(),
            notificationType
        )
        notificationManager.notify(CHAT_NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    private fun incomingCallNotification(
        message: RemoteMessage,
        notificationManager: NotificationManagerCompat,
        conversation: Conversation,
        callInitiatorId: String,
    ) {
        val notificationType = NotificationType.Call
        val notificationBuilder = createNotificationBuilder(
            message.notification?.title.orEmpty(),
            message.notification?.body.orEmpty(),
            notificationType
        )
        if (conversation.messageIds.size == 2) {
            // TODO: implement incoming call notification notify all users except the one who started the call
//            Timber.d("callInitiatorId: $callInitiatorId, callInitiator: $callInitiator")
//
//            Timber.d("creating person for notification")
//            val person = Person.Builder()
//                .setName(callInitiator?.username.orEmpty())
//                .setImportant(true)
//                .build()
//            Timber.d("person for notification creation finished")
//
//            val answerPendingIntent = createPendingIntent(notificationType)
//            val incomingCallStyle = NotificationCompat.CallStyle.forIncomingCall(
//                person,
//                answerPendingIntent,
//                answerPendingIntent
//            )
//            notificationBuilder.setStyle(incomingCallStyle)
        }
        if (isPermissionNotGranted()) {
            return
        }
        notificationManager.notify(CALL_NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
