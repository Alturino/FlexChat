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
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onirutla.flexchat.MainActivity
import com.onirutla.flexchat.R
import com.onirutla.flexchat.auth.domain.repository.AuthRepository
import com.onirutla.flexchat.conversation.domain.repository.ConversationRepository
import com.onirutla.flexchat.core.util.NotificationConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class CallNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var conversationRepository: ConversationRepository

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("From: ${message.from}")
        Timber.d("Message data payload: ${message.data}")
        Timber.d("Message notification body: ${message.notification?.body}")

        val title = message.notification?.title.orEmpty()
        val body = message.notification?.body.orEmpty()
        authRepository.currentUserFlow
            .filterNot { it.ongoingCallId.isEmpty() or it.ongoingCallId.isBlank() }
            .onEach {  }
            .launchIn(coroutineScope)
    }

    private fun sendNotification(title: String, notificationBody: String) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NotificationConstants.CALL_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationConstants.CALL_NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(notificationBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setChannelId(NotificationConstants.CALL_NOTIFICATION_CHANNEL_ID)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannelCompat
                .Builder(
                    NotificationConstants.CALL_NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                )
                .setName(NotificationConstants.CHAT_NOTIFICATION_CHANNEL_NAME)
                .build()
            notificationManager.createNotificationChannel(notificationChannel)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(NotificationConstants.CALL_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel(CancellationException("CallNotificationService is destroyed"))
    }

}
