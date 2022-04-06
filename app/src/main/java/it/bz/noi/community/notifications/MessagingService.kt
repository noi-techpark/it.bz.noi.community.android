package it.bz.noi.community.notifications

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging

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
		remoteMessage.notification?.let {
			Log.d(TAG, "Message Notification Body: ${it.body}")
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}

	companion object {
		private const val TAG = "MessagingService"

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
