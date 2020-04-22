package com.thetrainingplan

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat


class ActivityAlarmBroadcastReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context?, p1: Intent?) {

        val NOTIFICATION_ID = 1983429
        // Use NotificationCompat.Builder to set up our notification.
        val builder = NotificationCompat.Builder(context)

        builder.setAutoCancel(true)

        builder.setSmallIcon(R.drawable.notification_template_icon_bg)

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle("New Daily Tasks")

        // Content text, which appears in smaller text below the title
        builder.setContentText("Let's tick off your goals")

        // The subtext, which appears under the text on newer devices.
        builder.setSubText("")

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setOngoing(true)

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Will display the notification in the notification bar
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        val mIntent = Intent(context, ActivityRefreshTasks::class.java)
        mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context!!.startActivity(mIntent)

    }
}