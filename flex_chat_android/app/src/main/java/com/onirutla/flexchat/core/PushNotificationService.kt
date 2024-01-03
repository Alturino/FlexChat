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
import arrow.fx.coroutines.autoCloseable
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onirutla.flexchat.R
import com.onirutla.flexchat.domain.repository.ConversationRepository
import com.onirutla.flexchat.domain.repository.UserRepository
import com.onirutla.flexchat.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var conversationRepository: ConversationRepository

    @Inject
    lateinit var userRepository: UserRepository

    private val coroutineScope = CoroutineScope(SupervisorJob())

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "Firebase Channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Firebase Notification"
    }

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
        coroutineScope.launch {
            userRepository.currentUser.collect {
                if (it.id != message.data["userId"]) {
                    sendNotification(title, body)
                }
            }
        }
    }

    private fun sendNotification(title: String, notificationBody: String) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(notificationBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannelCompat
                .Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(NOTIFICATION_CHANNEL_NAME)
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
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
