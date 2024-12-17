package com.example.nfurgonapp.Utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nfurgonapp.Common
import com.example.nfurgonapp.Model.FCMSendData
import com.example.nfurgonapp.Model.TokenModel
import com.example.nfurgonapp.R
import com.example.nfurgonapp.Remote.IFCMService
import com.example.nfurgonapp.Remote.RetrofitFCMClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

object UserUtils {
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
    private val BITS = intArrayOf(16, 8, 4, 2, 1)
    fun generateGeohash(latitude: Double, longitude: Double, precision: Int = 5): String {
        val latRange = doubleArrayOf(-90.0, 90.0)
        val lonRange = doubleArrayOf(-180.0, 180.0)
        var isEven = true
        var bit = 0
        var ch = 0
        val geohash = StringBuilder()

        while (geohash.length < precision) {
            val mid = if (isEven) {
                (lonRange[0] + lonRange[1]) / 2
            } else {
                (latRange[0] + latRange[1]) / 2
            }

            if ((if (isEven) longitude else latitude) > mid) {
                ch = ch or BITS[bit]
                if (isEven) lonRange[0] = mid else latRange[0] = mid
            } else {
                if (isEven) lonRange[1] = mid else latRange[1] = mid
            }

            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                geohash.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }
        return geohash.toString()
    }
    fun updateUser(
        view:View?,
        updateData:Map<String, Any>
    ){
        FirebaseDatabase.getInstance()
            .getReference(Common.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener { e->
                Snackbar.make(view!!, e.message!!, Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!, "Información actualizada correctamente!", Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(currentUser.uid) // Usar uid del usuario actual
                .setValue(token)
                .addOnFailureListener { e ->
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
                .addOnSuccessListener {
                    Toast.makeText(context, "Token actualizado con éxito", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendDeclineRequest(view: ViewGroup, activity: Activity, key: String) {
        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        // Obtener una instancia de la base de datos de Firebase
        FirebaseDatabase.getInstance()
            // Obtener una referencia a la ubicación de los tokens
            .getReference(Common.TOKEN_REFERENCE)
            // Especificar el token al que se quiere acceder
            .child(key)
            // Agregar un listener para un solo evento de valor
            .addListenerForSingleValueEvent(object : ValueEventListener {
                // Si ocurre un error
                override fun onCancelled(databaseError: DatabaseError) {
                    // Mostrar un mensaje de error en una Snackbar
                    Snackbar.make(view, databaseError.message, Snackbar.LENGTH_LONG).show()
                }

                // Si se obtiene el dato
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Verificar si existe el dato
                    if (dataSnapshot.exists()) {
                        // Obtener el modelo de token
                        val tokenModel = dataSnapshot.getValue(TokenModel::class.java)

                        // Crear un mapa para almacenar los datos de la notificación
                        val notificationData: MutableMap<String, String> = HashMap()

                        // Agregar los datos de la notificación al mapa
                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_DECLINE)
                        notificationData.put(Common.NOTI_BODY, "This message represents for decline Driver action")
                        notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().currentUser!!.uid)

                        val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if (fcmResponse!!.success == 0) {
                                    compositeDisposable.clear()
                                    Snackbar.make(view, activity.getString(R.string.decline_failed), Snackbar.LENGTH_LONG).show()
                                }else{
                                    Snackbar.make(view, activity.getString(R.string.decline_success), Snackbar.LENGTH_LONG).show()

                                }
                            }, { t: Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view!!, t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))
                    }else{
                        Snackbar.make(view, activity.getString(R.string.token_not_found), Snackbar.LENGTH_LONG).show()

                    }
                }
            })

    }


}