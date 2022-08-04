package com.gigaxysafe.remindmethere

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.BuildConfig
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

const val MAPS_API_KEY = "AIzaSyD1Ll3jtGaIIo8EVRieuqBjog-z9E983yg"
val isVersionQOrLater: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

private const val NOTIFICATION_CHANNEL_ID = "GEOFENCING CHANNEL"

fun sendNotification(
    context: Context,
    message: String,
    latLng: LatLng
) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
            NotificationManager
    if (isVersionQOrLater &&
        notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    val intent = MainActivity.newIntent(context.applicationContext, latLng)

    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(MainActivity::class.java)
        .addNextIntent(intent)

    val notificationPendingIntent = stackBuilder
        .getPendingIntent(
            getUniqueId(),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    val notification = NotificationCompat.Builder(
        context,
        NOTIFICATION_CHANNEL_ID
    )
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(message)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

fun showReminderInMap(
    context: Context,
    map: GoogleMap,
    reminder: Reminder,
) {
    if (reminder.latLng != null) {
        val latLng = reminder.latLng as LatLng
        val vectorToBitmap =
            vectorToBitmap(context.resources, R.drawable.ic_twotone_location_on_48px)
        val marker = map.addMarker(MarkerOptions().position(latLng).icon(vectorToBitmap))
        marker.tag = reminder.id
        if (reminder.radius != null) {
            val radius = reminder.radius as Double
            map.addCircle(
                CircleOptions()
                    .center(reminder.latLng)
                    .radius(radius)
                    .strokeColor(ContextCompat.getColor(context, R.color.blue))
                    .fillColor(ContextCompat.getColor(context, R.color.blue_light))
            )
        }
    }
}

fun vectorToBitmap(
    resources: Resources,
    @DrawableRes id: Int,
): BitmapDescriptor {
    val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
    val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun hideKeyboard(context: Context, view: View) {
    val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    keyboard.hideSoftInputFromWindow(view.windowToken, 0)
}

fun EditText.requestFocusWithKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (!hasFocus()) {
        requestFocus()
    }

    post { imm.showSoftInput(this, InputMethodManager.SHOW_FORCED) }
}