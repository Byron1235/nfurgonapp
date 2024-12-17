package com.example.nfurgonapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nfurgonapp.Model.DriverInfoModel
import com.google.android.gms.maps.model.LatLng
import java.util.Random


object Common {
    fun buildWelcomeMessage(): String {
        return StringBuilder("Bienvenido ")
            .append(currentUser!!.primer_nombre)
            .append(" ")
            .append(currentUser!!.apellido)
            .toString()

    }

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent: PendingIntent? = null

        // Crear el PendingIntent solo si el intent no es null
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Usar FLAG_IMMUTABLE para mayor seguridad en Android 12 y superior
            )
        }

        val NOTIFICATION_CHANNEL_ID = "nfurgon_dev_1.0"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Comprobar si el canal ya existe
            val existingChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (existingChannel == null) {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "NFurgon",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Canal de notificaciones para NFurgon"
                    enableLights(true)
                    lightColor = Color.RED
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.baseline_directions_car_24) // Asegúrate de tener este ícono en drawable
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.baseline_directions_car_24)) // Asegúrate de tener este ícono en drawable
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para que sea visible

        // Mostrar la notificación
        notificationManager.notify(id, notificationBuilder.build()) // Usa el ID proporcionado para notificaciones únicas
    }
    fun decodePoly(encoded: String): java.util.ArrayList<LatLng?>? {
        val poly = ArrayList<LatLng?>()  // Cambiar el tipo a LatLng?
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lng += dlng

            // Crear un objeto LatLng y agregarlo a la lista
            val p = LatLng(
                ((lat.toDouble() / 1E5)),
                ((lng.toDouble() / 1E5))
            )
            poly.add(p)
        }
        return poly
    }

    fun createUniqueTripIdNumber(timeOffset: Long?): String? {
        val rd = Random()
        var current = System.currentTimeMillis()+timeOffset!!
        var  unique = current+rd.nextLong()
        if (unique < 0) unique *= -1
        return unique.toString()
    }

    val TRIP_DESTINATION_LOCATION_REF: String="TripDestinationLocation"
    val TRIP: String="Trips"
    val RIDER_INFO: String="Riders"
    val DRIVER_KEY: String="DriverKey"
    val REQUEST_DRIVER_DECLINE: String="Declinar"
    val NOTI_BODY: String= "body"
    val NOTI_TITLE: String= "Token"
    val TOKEN_REFERENCE: String="Token"
    val DRIVER_LOCATION_REFERENCE: String = "DriversLOCATION"
    var currentUser: DriverInfoModel?=null
    val DRIVER_INFO_REFERENCE: String = "DriverINFO"
    val RIDER_KEY: String = "RiderKey"
    val PICKUP_LOCATION: String = "PickupLocation"
    val REQUEST_DRIVER_TITLE: String = "RequestDriver"

    val DESINATION_LOCATION: String = "DestinationLocation"
    val DESTINATION_LOCATION_STRING: String = "DestinationLocationString"
    val PICKUP_LOCATION_STRING: String ="PickupLocationString"
}


