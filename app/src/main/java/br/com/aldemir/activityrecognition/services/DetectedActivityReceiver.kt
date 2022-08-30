package br.com.aldemir.activityrecognition.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.com.aldemir.activityrecognition.MainActivity
import br.com.aldemir.activityrecognition.R
import br.com.aldemir.activityrecognition.helper.SUPPORTED_ACTIVITY_KEY
import br.com.aldemir.activityrecognition.helper.SupportedActivity
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

private const val DETECTED_PENDING_INTENT_REQUEST_CODE = 100
private const val RELIABLE_CONFIDENCE = 50

private const val DETECTED_ACTIVITY_CHANNEL_ID = "detected_activity_channel_id"
const val DETECTED_ACTIVITY_NOTIFICATION_ID = 10
private const val TAG = "ActivityUpdate"

class DetectedActivityReceiver : BroadcastReceiver() {

    companion object {

        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, DetectedActivityReceiver::class.java)
            val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(context, DETECTED_PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(context, DETECTED_PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            return pendingIntent
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.let {
                handleDetectedActivities(it.probableActivities, context)
            }
        }
    }

    private fun handleDetectedActivities(
        detectedActivities: List<DetectedActivity>,
        context: Context
    ) {
        Log.d(TAG, "onReceive: $detectedActivities")
        detectedActivities
            .filter {
                it.type == DetectedActivity.STILL ||
                        it.type == DetectedActivity.WALKING ||
                        it.type == DetectedActivity.RUNNING ||
                        it.type == DetectedActivity.ON_FOOT ||
                        it.type == DetectedActivity.TILTING ||
                        it.type == DetectedActivity.IN_VEHICLE ||
                        it.type == DetectedActivity.ON_BICYCLE
            }
            .filter { it.confidence > RELIABLE_CONFIDENCE }
            .run {
                if (isNotEmpty()) {
                    showNotification(this[0], context)
                    sendBroadcast(this[0], context)
                }
            }
    }

    private fun sendBroadcast(detectedActivity: DetectedActivity, context: Context) {
        Log.w(TAG, "__________GCM Broadcast ${detectedActivity.confidence}")
        val intentActivity = Intent(context.getString(R.string.broadcast_detected_activity))
        intentActivity.apply {
            putExtra(
                SUPPORTED_ACTIVITY_KEY,
                SupportedActivity.fromActivityType(detectedActivity.type)
            )
            putExtra(
                context.getString(R.string.activity_confidence),
                detectedActivity.confidence
            )
        }
        context.sendBroadcast(intentActivity)
    }

    private fun showNotification(detectedActivity: DetectedActivity, context: Context) {

        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(
                SUPPORTED_ACTIVITY_KEY,
                SupportedActivity.fromActivityType(detectedActivity.type)
            )
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val activity = SupportedActivity.fromActivityType(detectedActivity.type)

        val builder = NotificationCompat.Builder(context, DETECTED_ACTIVITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(activity.activityText))
            .setContentText("Você estar ${detectedActivity.confidence}% certo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(DETECTED_ACTIVITY_NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "detected_activity_channel_name"
            val descriptionText = "detected_activity_channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(DETECTED_ACTIVITY_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}