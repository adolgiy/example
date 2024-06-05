package io.snoozer.example

import android.app.PendingIntent
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : ComponentActivity() {

    private val notification = OngoingOrderNotification(
        status = "В пути",
        address = "Россия, Москва, Большая Марфинская улица, 1к4",
        comment = "Курьер будет с 12:05 до 12:25",
        points = List(4) { OngoingOrderNotification.Point(it <= 0) }
    )
    private val notificationChannelId = "ongoing_notification_id"
    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
        updateOngoingNotification(notification)
    }

    private fun updateOngoingNotification(notification: OngoingOrderNotification) {
        val existingNotificationChannel = notificationManagerCompat.getNotificationChannel(notificationChannelId)
        if (existingNotificationChannel == null) {
            val importance = NotificationManagerCompat.IMPORTANCE_HIGH
            val channel = NotificationChannelCompat.Builder(notificationChannelId, importance)
                .setName("Ongoing Notification")
                .setDescription("We'll show important information about your active order")
                .setShowBadge(true)
                .build()

            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notificationCompatBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(Color.MAGENTA)
            .setColorized(true)
            .setContentTitle("Ongoing Title")
            .setContentText("Ongoing Content Text")
            .setContentInfo("Ongoing Content Info")
            .setContentIntent(PendingIntent.getActivity(this, 1234, intent, PendingIntent.FLAG_IMMUTABLE))
            .setCustomBigContentView(createRemoteViews(notification))
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.baseline_back_hand_24)
            .setSubText("Sub Text")

        try {
            notificationManagerCompat.notify(1234, notificationCompatBuilder.build())
        } catch (e: SecurityException) {
            // nothing
        }
    }

    private fun createRemoteViews(notification: OngoingOrderNotification): RemoteViews {
        val views = RemoteViews(packageName, R.layout.notification)
        views.setTextViewText(R.id.status, notification.status)
        views.setTextViewText(R.id.address, "Россия, Москва, Большая Марфинская улица, 1к4")
        views.setTextViewText(R.id.comment, "Курьер будет с 12:05 до 12:25")
        views.removeAllViews(R.id.progress)

        notification.points.forEachIndexed { index, point ->
            val dotRemoteViews = RemoteViews(packageName, R.layout.notification_dot)
            val dotDrawableResourceId = when {
                point.finished -> R.drawable.bg_dot_finished
                notification.points.getOrNull(index.dec())?.finished == true -> R.drawable.bg_dot_active
                else -> R.drawable.bg_dot_pending
            }

            dotRemoteViews.setImageViewResource(R.id.dot, dotDrawableResourceId)
            views.addView(R.id.progress, dotRemoteViews)

            if (index != notification.points.lastIndex) {
                val layoutResId = when {
                    point.finished -> R.layout.notification_divider_active
                    else -> R.layout.notification_divider_pending
                }

                views.addView(R.id.progress, RemoteViews(packageName, layoutResId))
            }
        }

        return views
    }

    data class OngoingOrderNotification(
        val status: String,
        val address: String,
        val comment: String,
        val points: List<Point>
    ) {

        data class Point(
            val finished: Boolean
        )
    }
}
