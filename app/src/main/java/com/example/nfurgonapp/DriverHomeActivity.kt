package com.example.nfurgonapp

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.nfurgonapp.Utils.UserUtils
import com.example.nfurgonapp.databinding.ActivityDriverHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDriverHomeBinding
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var img_avatar: ImageView
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent> // Nuevo launcher
    private var imageUri: Uri? = null
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDriverHome.toolbar)

        drawerLayout = binding.drawerLayout as DrawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_driver_home)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        init()

        // Inicialización del launcher para la selección de imagen
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data // Guardar el URI de la imagen seleccionada
                showDialogUpload() // Mostrar el diálogo de confirmación
            }
        }
    }

    // Método para mostrar el diálogo de confirmación antes de subir la imagen
    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this@DriverHomeActivity)
        builder.setTitle("Cambiar avatar")
            .setMessage("¿Quieres cambiar tu avatar?")
            .setNegativeButton("CANCELAR") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setPositiveButton("CAMBIAR") { dialogInterface, _ ->
                if (imageUri != null) {
                    img_avatar.setImageURI(imageUri) // Asignar la imagen al ImageView solo si el usuario confirma

                    // Iniciar el proceso de subida
                    waitingDialog.show()
                    val avatarFolder = storageReference.child("avatars/" + FirebaseAuth.getInstance().currentUser!!.uid)
                    avatarFolder.putFile(imageUri!!)
                        .addOnFailureListener { e ->
                            Snackbar.make(drawerLayout, e.message!!, Snackbar.LENGTH_LONG).show()
                            waitingDialog.dismiss()
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                    val update_data = HashMap<String, Any>()
                                    update_data["avatar"] = uri.toString()
                                    UserUtils.updateUser(drawerLayout, update_data)
                                }
                            }
                            waitingDialog.dismiss()
                        }
                        .addOnProgressListener { taskSnapshot ->
                            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                            waitingDialog.setMessage(StringBuilder("Subiendo: ").append(progress).append("%"))
                        }
                }
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_green_light))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.colorAccent))
        }
        dialog.show()
    }

    private fun init() {
        storageReference = FirebaseStorage.getInstance().reference
        waitingDialog = AlertDialog.Builder(this)
            .setMessage("Esperando...")
            .setCancelable(false).create()

        navView.setNavigationItemSelectedListener { it ->
            if (it.itemId == R.id.nav_sing_out) {
                val builder = AlertDialog.Builder(this@DriverHomeActivity)
                builder.setTitle("Cerrar sesión?")
                    .setMessage("¿Quieres cerrar sesión?")
                    .setNegativeButton("CANCELAR") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("CERRAR SESIÓN") { dialogInterface, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@DriverHomeActivity, SplashScreenActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)
                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.colorAccent))
                }
                dialog.show()
            }
            true
        }

        val headerView = navView.getHeaderView(0)
        val txt_name = headerView.findViewById<View>(R.id.txt_name) as TextView
        val txt_phone = headerView.findViewById<View>(R.id.txt_phone) as TextView
        val txt_star = headerView.findViewById<View>(R.id.txt_star) as TextView
        img_avatar = headerView.findViewById<View>(R.id.img_avatar) as ImageView

        txt_name.text = (Common.buildWelcomeMessage())
        txt_phone.text = Common.currentUser!!.numerocelular
        txt_star.text = StringBuilder().append(Common.currentUser!!.rating)

        if (Common.currentUser != null && Common.currentUser!!.avatar != null && !TextUtils.isEmpty(Common.currentUser!!.avatar)) {
            Glide.with(this)
                .load(Common.currentUser!!.avatar)
                .into(img_avatar)
        }

        img_avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(Intent.createChooser(intent, "Selecciona la foto"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_driver_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 7272
    }
}
