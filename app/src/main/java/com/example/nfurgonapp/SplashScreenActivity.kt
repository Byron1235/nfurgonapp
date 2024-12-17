package com.example.nfurgonapp

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.nfurgonapp.Model.DriverInfoModel
import com.example.nfurgonapp.Utils.UserUtils
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit


class SplashScreenActivity : ComponentActivity() {

    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private lateinit var database:FirebaseDatabase
    private lateinit var driverInfoRef:DatabaseReference
    override fun onStart() {
        super.onStart()
        delaySplashScreen()
    }

    override fun onStop() {
        if (::firebaseAuth.isInitialized && ::listener.isInitialized) {
            firebaseAuth.removeAuthStateListener(listener)
        }
        super.onStop()
    }

    private fun delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe({
                firebaseAuth.addAuthStateListener(listener)
            }, {
                // Manejar error
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa el launcher de actividad
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == RESULT_OK && FirebaseAuth.getInstance().currentUser != null) {
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, "Bienvenido: ${user?.uid}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: ${response?.error?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        setContentView(R.layout.activity_splash_screen)
        init()
    }

    private fun init() {

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)

        providers = listOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null) {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                            return@addOnCompleteListener
                        }

                        // Obtiene el token de registro de FCM
                        val token = task.result
                        Log.d(TAG, "Got new FCM registration token: $token")
                        UserUtils.updateToken(this@SplashScreenActivity, token)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@SplashScreenActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                checkUserFromFirebase()
            } else {
                showLoginLayout()
            }
        }
    }

    private fun checkUserFromFirebase() {
        driverInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        //Toast.makeText(this@SplashScreenActivity, "Usuario ya registrado!", Toast.LENGTH_SHORT).show()
                        val model = snapshot.getValue(DriverInfoModel::class.java)
                        goToHomeActivity(model)
                    }
                    else{
                        showRegisterLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashScreenActivity, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun goToHomeActivity(model: DriverInfoModel?) {
        Common.currentUser = model
        startActivity(Intent(this, DriverHomeActivity::class.java))
        finish()
    }

    private fun showRegisterLayout() {
        val builder = AlertDialog.Builder(this, R.style.DialogTheme)
        val  itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null)

        val edt_primer_nombre = itemView.findViewById<View>(R.id.edt_primer_nombre) as TextInputEditText
        val edt_apellido = itemView.findViewById<View>(R.id.edt_apellido) as TextInputEditText
        val edt_phone_number= itemView.findViewById<View>(R.id.phone_number) as TextInputEditText

        val btn_continue = itemView.findViewById<View>(R.id.btn_register) as Button

        //Set data
        if (FirebaseAuth.getInstance().currentUser!!.phoneNumber != null && !TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber))
            edt_phone_number.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)

        //View
        builder.setView(itemView)
        val dialog = builder.create()
        dialog.show()

        //event
        btn_continue.setOnClickListener{
            if (TextUtils.isDigitsOnly(edt_primer_nombre.text.toString()))
            {
                Toast.makeText(this@SplashScreenActivity,"Por favor ingrese su nombre.",  Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (TextUtils.isDigitsOnly(edt_apellido.text.toString()))
            {
                Toast.makeText(this@SplashScreenActivity,"Por favor ingrese su apellido.",  Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (TextUtils.isDigitsOnly(edt_phone_number.text.toString()))
            {
                Toast.makeText(this@SplashScreenActivity,"Por favor ingrese su número.",  Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                val Model = DriverInfoModel()
                Model.primer_nombre = edt_primer_nombre.text.toString()
                Model.apellido = edt_apellido.text.toString()
                Model.numerocelular = edt_phone_number.text.toString()
                Model.rating = 0.0

                driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(Model)
                    .addOnFailureListener{ e ->
                        Toast.makeText(this@SplashScreenActivity,""+e.message,  Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
                        progressBar.visibility = View.GONE

                    }
                    .addOnSuccessListener {
                        Toast.makeText(this@SplashScreenActivity,"Registro completado.",  Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        goToHomeActivity(Model)

                        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
                        progressBar.visibility = View.GONE
                    }
            }
        }
    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.layout_singin)
            .setPhoneButtonId(R.id.btn_phone_singin)
            .setGoogleButtonId(R.id.btn_google_singin) // Asegúrate de que ID de botón es correcto
            .build()

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setTheme(R.style.Theme_LoginTheme)
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        // Lanza el Intent con ActivityResultLauncher
        signInLauncher.launch(signInIntent)
    }
}
