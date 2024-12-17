// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import it.bz.noi.community.MainActivity
import it.bz.noi.community.R
import it.bz.noi.community.ui.NewsTickerFlow
import kotlin.random.Random


class MessagingService : FirebaseMessagingService() {

	override fun onNewToken(token: String) {
		super.onNewToken(token)
	}

	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		remoteMessage.notification?.let {
			val title = it.title ?: ""
			val message = it.body ?: ""
			val link = remoteMessage.data["deep_link"]

			showNotificationBanner(message, title, Uri.parse(link ?: ""))
			NewsTickerFlow.tick()
		}
	}

	companion object {
		private const val TAG = "MessagingService"
		private const val CHANNEL_ID = "newsChannel"

		fun Context.showNotification() {
			showNotificationBanner(
				"Test",
				"Test",
				Uri.parse("noi-community://it.bz.noi.community/eventDetails/2343242")
			)
		}

		private fun Context.showNotificationBanner(
			message: CharSequence,
			title: CharSequence,
			link: Uri
		) {
			with(NotificationManagerCompat.from(this)) {
				val id = Random.nextInt()

				val newsOrEventDetailsPendingIntent = PendingIntent.getActivity(
					applicationContext,
					0,
					Intent(this@showNotificationBanner, MainActivity::class.java).apply {
						data = link
						flags = Intent.FLAG_ACTIVITY_NEW_TASK
					},
					PendingIntent.FLAG_IMMUTABLE
				)

				val notification =
					NotificationCompat.Builder(this@showNotificationBanner, CHANNEL_ID)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle(title)
						.setContentText(message)
						.setAutoCancel(true)
						.setContentIntent(newsOrEventDetailsPendingIntent)
						.build()

				try {
					notify(id, notification)
				} catch (e: SecurityException) {
					Log.e(TAG, "No permission granted", e)
				}
			}
		}

		@JvmStatic
		fun createChannelIfNeeded(context: Context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				with(NotificationManagerCompat.from(context)) {
					val channelName = context.getString(R.string.news_notification_channel_name)
					val channel = NotificationChannel(
						CHANNEL_ID,
						channelName,
						NotificationManager.IMPORTANCE_HIGH
					)
					createNotificationChannel(channel)
				}
			}
		}

		fun registrationToken() {
			FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
				if (!task.isSuccessful) {
					Log.w(TAG, "Fetching FCM registration token failed", task.exception)
					return@OnCompleteListener
				}

				// Get new FCM registration token
				val token = task.result

				val msg = "FCM registration token: $token"
				Log.d(TAG, msg)
			})
		}

		fun subscribeToTopic(topic: String) {
			Firebase.messaging.subscribeToTopic(topic).addOnCompleteListener { task ->
				var msg = "Topic $topic subscribed successfully"
				if (!task.isSuccessful) {
					msg = "Error subscribing topic $topic"
				}
				Log.d(TAG, msg)
			}
		}

		fun unsubscribeFromTopic(topic: String) {
			Firebase.messaging.unsubscribeFromTopic(topic).addOnCompleteListener { task ->
				var msg = "Topic $topic unsubscribed successfully"
				if (!task.isSuccessful) {
					msg = "Error unsubscribing topic $topic"
				}
				Log.d(TAG, msg)
			}
		}
	}

}
