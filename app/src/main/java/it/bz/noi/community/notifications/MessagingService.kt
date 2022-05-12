package it.bz.noi.community.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import it.bz.noi.community.R
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		// ...

		// TODO(developer): Handle FCM messages here.
		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		Log.d(TAG, "From: ${remoteMessage.from}")

		// Check if message contains a data payload.
		if (remoteMessage.data.isNotEmpty()) {
			Log.d(TAG, "Message data payload: ${remoteMessage.data}")

			/*if (*//* Check if data needs to be processed by long running job *//* true) {
				// For long-running tasks (10 seconds or more) use WorkManager.
				scheduleJob()
			} else {
				// Handle message within 10 seconds
				handleNow()
			}*/
		}

		// Check if message contains a notification payload.
		var msg = ""
		remoteMessage.notification?.let {
			Log.d(TAG, "Message Notification Body: ${it.body}")
			msg = it.body ?: "test"
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.

		val args = Bundle()
		showPendingIntent(msg, args)
	}

	private fun showPendingIntent(message: CharSequence, args: Bundle) {
		with (NotificationManagerCompat.from(this)) {
			val id = Random.nextInt()
			args.putInt("notificationId", id)
			val notification = NotificationCompat.Builder(this@MessagingService, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_noi_app_icon)
				.setContentText(message)
				.setAutoCancel(true)
				//.setContentIntent(createPendingIntent(args))
				.build()
			notify(id, notification)
		}
	}

	companion object {
		private const val TAG = "MessagingService"
		private const val CHANNEL_ID = "newsChannel"

		@JvmStatic
		fun createChannelIfNeeded(context: Context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				with (NotificationManagerCompat.from(context)) {
					val channelName = "News" // context.getString()
					val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
						description = "Apertura dettaglio newx"//context.getString(R.string.transaction_channel_desc)
					}
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

				val msg ="FCM registration token: $token"
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
