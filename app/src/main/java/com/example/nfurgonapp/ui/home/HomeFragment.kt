package com.example.nfurgonapp.ui.home

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nfurgonapp.Common
import com.example.nfurgonapp.Common.decodePoly
import com.example.nfurgonapp.DriverHomeActivity
import com.example.nfurgonapp.Model.EventBus.DriverRequestReceived
import com.example.nfurgonapp.Model.RiderModel
import com.example.nfurgonapp.Model.TripPlanModel
import com.example.nfurgonapp.R
import com.example.nfurgonapp.Remote.IGoogleAPI
import com.example.nfurgonapp.Remote.RetrofitClient
import com.example.nfurgonapp.Utils.UserUtils
import com.example.nfurgonapp.Utils.UserUtils.generateGeohash
import com.example.nfurgonapp.databinding.FragmentHomeBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.SquareCap
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var chip_decline:Chip
    private lateinit var layout_accept:CardView
    private lateinit var circularProgressBar:CircularProgressBar
    private lateinit var txt_estimate_time:TextView
    private lateinit var txt_estimate_distance:TextView
    private lateinit var txt_rating:TextView
    private lateinit var root_layout:FrameLayout

    private var distance = 5.0 // Rango inicial en km
    private val MAX_DISTANCE = 50.0 // Límite máximo en km


    private lateinit var txt_type_uber:TextView
    private lateinit var img_round:ImageView
    private lateinit var layout_start_uber:CardView
    private lateinit var txt_rider_name:TextView
    private lateinit var txt_start_uber_estimate_distance:TextView
    private lateinit var txt_start_uber_estimate_time:TextView
    private lateinit var img_phone_call:ImageView
    private lateinit var btn_start_uber:MaterialButton
    private lateinit var btn_complete:MaterialButton

    private var pickupGeoFire:GeoFire?=null
    private var pickupGeoQuery:GeoQuery?=null

    private var destinationGeoFire:GeoFire?=null
    private var destionationGeoQuery:GeoQuery?=null

    private val pickupGeoQueryListener = object : GeoQueryEventListener{
        override fun onKeyEntered(key: String?, location: GeoLocation?) {
            btn_start_uber.isEnabled = true
            if (pickupGeoQuery != null){
                pickupGeoFire!!.removeLocation(key)
                pickupGeoFire = null
                pickupGeoQuery!!.removeAllListeners()
            }
        }

        override fun onKeyExited(key: String?) {
            btn_start_uber.isEnabled = false
        }

        override fun onKeyMoved(key: String?, location: GeoLocation?) {
            TODO("Not yet implemented")
        }

        override fun onGeoQueryReady() {
            TODO("Not yet implemented")
        }

        override fun onGeoQueryError(error: DatabaseError?) {
            TODO("Not yet implemented")
        }

    }


    private val destinationGeoQueryListener = object:GeoQueryEventListener{
        override fun onKeyEntered(key: String?, location: GeoLocation?) {
            Toast.makeText(requireContext(), "Destino identificado", Toast.LENGTH_SHORT).show()
            btn_complete.isEnabled = true
            if (destionationGeoQuery != null){
                destinationGeoFire!!.removeLocation(key)
                destinationGeoFire = null
                destionationGeoQuery!!.removeAllListeners()
            }
        }

        override fun onKeyExited(key: String?) {
            TODO("Not yet implemented")
        }

        override fun onKeyMoved(key: String?, location: GeoLocation?) {
            TODO("Not yet implemented")
        }

        override fun onGeoQueryReady() {
            TODO("Not yet implemented")
        }

        override fun onGeoQueryError(error: DatabaseError?) {
            TODO("Not yet implemented")
        }

    }

    private var waiting_timer:CountDownTimer?=null

    private var isTripStart=false
    private var onlineSystemAlreadyRegister=false

    private var tripNumberId:String?=""

    private val compositeDisposable = CompositeDisposable()
    private lateinit var iGoogleAPI: IGoogleAPI
    private var blackPolyline: Polyline? = null
    private var grayPolyline: Polyline? = null
    private var polylineOptions: PolylineOptions? = null
    private var blackPolylineOptions: PolylineOptions? = null
    private var polylineList: ArrayList<LatLng?>? = null

    private lateinit var mMap: GoogleMap

    private var _binding: FragmentHomeBinding? = null

    private lateinit var mapFragment: SupportMapFragment

    //Location
    private  var locationRequest: LocationRequest?=null
    private  var locationCallback: LocationCallback?=null
    private  var fusedLocationProviderClient: FusedLocationProviderClient?=null

    //Online system
    private lateinit var onlineRef:DatabaseReference
    private var currentUserRef: DatabaseReference ?= null
    private lateinit var driversLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire

    private var driverRequestReceived:DriverRequestReceived?=null
    private var countDownEvent: Disposable?=null

    private val onlineValueEventListener = object:ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists() && currentUserRef != null)
                currentUserRef!!.onDisconnect().removeValue()
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)
        onlineSystemAlreadyRegister = false
        compositeDisposable.clear()
        if (EventBus.getDefault().hasSubscriberForEvent(DriverHomeActivity::class.java))
            EventBus.getDefault().removeStickyEvent(DriverHomeActivity::class.java)
        EventBus.getDefault().unregister(this);
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }
    override fun onResume() {
        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {
        if (!onlineSystemAlreadyRegister){
            onlineRef.addValueEventListener(onlineValueEventListener)
            onlineSystemAlreadyRegister = true

        }
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initViews(root);

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }

    private fun initViews(root: View?) {
        chip_decline = root!!.findViewById(R.id.chip_decline) as Chip
        layout_accept = root!!.findViewById(R.id.layout_accept) as CardView
        circularProgressBar = root!!.findViewById(R.id.circularProgressBar) as CircularProgressBar
        txt_estimate_distance = root!!.findViewById(R.id.txt_estimate_distance) as TextView
        txt_estimate_time = root!!.findViewById(R.id.txt_estimate_time) as TextView
        txt_rating = root!!.findViewById(R.id.txt_rating) as TextView
        root_layout = root!!.findViewById(R.id.root_layout) as FrameLayout

        txt_rating = root!!.findViewById(R.id.txt_rating) as TextView
        txt_type_uber = root!!.findViewById(R.id.txt_type_uber) as TextView
        img_round = root!!.findViewById(R.id.img_round) as ImageView
        layout_start_uber = root!!.findViewById(R.id.layout_start_uber) as CardView
        txt_rider_name = root!!.findViewById(R.id.txt_rider_name) as TextView
        txt_start_uber_estimate_distance = root!!.findViewById(R.id.txt_start_uber_estimate_distance) as TextView
        txt_start_uber_estimate_time = root!!.findViewById(R.id.txt_start_uber_estimate_time) as TextView
        img_phone_call = root!!.findViewById(R.id.img_phone_call) as ImageView
        btn_start_uber = root!!.findViewById(R.id.btn_start_uber) as MaterialButton
        btn_complete = root!!.findViewById(R.id.btn_complete) as MaterialButton

        chip_decline.setOnClickListener{
            if (driverRequestReceived != null){
                if (countDownEvent != null)
                    countDownEvent!!.dispose()
                chip_decline.visibility = View.GONE
                layout_accept.visibility = View.GONE
                mMap.clear()
                circularProgressBar.progress = 0f
                UserUtils.sendDeclineRequest(root_layout,requireActivity(),
                    driverRequestReceived!!.key.toString()
                )
                driverRequestReceived = null
            }
        }

        btn_start_uber.setOnClickListener {
            if (blackPolyline != null)blackPolyline!!.remove()
            if (grayPolyline != null)grayPolyline!!.remove()

            if (waiting_timer != null) waiting_timer!!.cancel()

            if (driverRequestReceived != null){
                val destinationLatLng = LatLng(
                    driverRequestReceived!!.destinationLocation!!.split(",") [0].toDouble(),
                    driverRequestReceived!!.destinationLocation!!.split(",") [1].toDouble()
                )
                mMap.addMarker(MarkerOptions().position(destinationLatLng)
                    .title(driverRequestReceived!!.destinationLocationString)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))

                drawPathFromCurrentLocation(driverRequestReceived!!.destinationLocation)

                btn_start_uber.visibility = View.GONE
                chip_decline.visibility = View.GONE
                btn_complete.visibility = View.VISIBLE
            }
        }
        btn_complete.setOnClickListener {
            Toast.makeText(requireContext(), "Viaje completado fake#1", Toast.LENGTH_SHORT).show()
        }

    }

    private fun drawPathFromCurrentLocation(destinationLocation: String?) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(requireView(), getString(R.string.permiso_denegado), Snackbar.LENGTH_LONG).show()
            return
        }
        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e -> Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show() }
            .addOnSuccessListener { location ->

                compositeDisposable.add(iGoogleAPI.getDirections("driving", "less_driving",
                    StringBuilder()
                        .append(location.latitude)
                        .append(",")
                        .append(location.longitude)
                        .toString(),
                    destinationLocation,
                    getString(R.string.google_api_key))
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{returnResult ->
                        Log.d("APIRETURN", returnResult)
                        try {
                            val jsonObject = JSONObject(returnResult)
                            val jsonArray = jsonObject.getJSONArray("routes")
                            for (i in 0 until jsonArray.length()) {
                                val route = jsonArray.getJSONObject(i)
                                val poly = route.getJSONObject("overview_polyline")
                                val polyline = poly.getString("points")
                                polylineList = decodePoly(polyline)
                            }
                            polylineOptions = PolylineOptions()
                            polylineOptions!!.color(Color.GRAY)
                            polylineOptions!!.width(12f)
                            polylineOptions!!.startCap(SquareCap())
                            polylineOptions!!.jointType(JointType.ROUND)
                            polylineOptions!!.addAll(polylineList!!)
                            grayPolyline = mMap.addPolyline(polylineOptions!!)

                            blackPolylineOptions = PolylineOptions()
                            blackPolylineOptions!!.color(Color.BLACK)
                            blackPolylineOptions!!.width(5f)
                            blackPolylineOptions!!.startCap(SquareCap())
                            blackPolylineOptions!!.jointType(JointType.ROUND)
                            blackPolylineOptions!!.addAll(polylineList!!)
                            blackPolyline = mMap.addPolyline(blackPolylineOptions!!)

                            val origin = LatLng(location.latitude, location.longitude)
                            val destination = LatLng(destinationLocation!!.split(",")[0].toDouble(),
                                destinationLocation!!.split(",")[1].toDouble())

                            // Límites del mapa
                            val latLngBounds = LatLngBounds.Builder()
                                .include(origin)
                                .include(destination)
                                .build()

                            createGeoFireDestinationLocation(driverRequestReceived!!.key,destination)
                            mMap.addMarker(MarkerOptions().position(destination).icon(
                                BitmapDescriptorFactory.defaultMarker())
                                .title("Pickup location"))

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                        }catch (e:java.lang.Exception){
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }    }

    private fun createGeoFireDestinationLocation(key: String?, destination: LatLng) {
        val ref = FirebaseDatabase.getInstance().getReference(Common.TRIP_DESTINATION_LOCATION_REF)

        destinationGeoFire = GeoFire(ref)
        destinationGeoFire!!.setLocation(key!!,
            GeoLocation(destination.latitude,destination.longitude),{key1, error ->

            })

    }

    private fun init() {

        iGoogleAPI = RetrofitClient.instance!!.create(IGoogleAPI::class.java)

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return

        }

        locationRequest = LocationRequest()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setFastestInterval(3000)
        locationRequest!!.interval = 5000
        locationRequest!!.setSmallestDisplacement(10f)

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                val newPos = LatLng(locationResult!!.lastLocation.latitude,locationResult!!.lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))
                if (!isTripStart){

                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addressList: List<Address>? = geoCoder.getFromLocation(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude,
                            1
                        )

                        if (!addressList.isNullOrEmpty()) {  // Verifica si la lista no es nula o vacía
                            val cityName = addressList[0].locality ?: "Ciudad desconocida" // Locality puede ser null
                            driversLocationRef = FirebaseDatabase.getInstance()
                                .getReference(Common.DRIVER_LOCATION_REFERENCE)
                                .child(cityName)

                            currentUserRef = FirebaseDatabase.getInstance()
                                .getReference(Common.DRIVER_LOCATION_REFERENCE)
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)

                            val driversLocationRef = FirebaseDatabase.getInstance()
                                .getReference("DriversLOCATION")
                                .child(cityName)
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)

// En lugar de usar GeoFire
                            val locationData = hashMapOf(
                                "g" to generateGeohash(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude),
                                "l" to listOf(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                            )

                            driversLocationRef.setValue(locationData)
                                .addOnSuccessListener {
                                    Snackbar.make(mapFragment.requireView(), "Ubicación guardada exitosamente", Snackbar.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    Snackbar.make(mapFragment.requireView(), "Error al guardar ubicación: ${error.message}", Snackbar.LENGTH_SHORT).show()
                                }
                            registerOnlineSystem()
                        } else {
                            Snackbar.make(requireView(), "No se pudo encontrar la ciudad", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        Snackbar.make(requireView(), "Error al obtener la ubicación: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    }

                }else{
                    if (TextUtils.isEmpty(tripNumberId)) {
                        // Actualizar ubicación
                        val updateData = HashMap<String, Any>()
                        updateData["currentLat"] = locationResult.lastLocation.latitude
                        updateData["currentLng"] = locationResult.lastLocation.longitude

                        FirebaseDatabase.getInstance()
                            .getReference(Common.TRIP)
                            .child(tripNumberId!!)
                            .updateChildren(updateData)
                            .addOnFailureListener { e ->
                                // Manejar errores aquí
                                Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                            }
                    }
                }}

        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if ((ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap!!

        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object: PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {

                        Toast.makeText(requireContext(), "botón test", Toast.LENGTH_SHORT).show()

                        fusedLocationProviderClient!!.lastLocation.addOnFailureListener{ e ->
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()

                        }.addOnSuccessListener { location ->
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f))
                        }
                        true
                    }


                    val locationButton = (mapFragment.requireView()!!.findViewById<View>("1".toInt())!!.parent!! as View).findViewById<View>("2".toInt());
                    val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.bottomMargin = 50
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    if (p0 != null) {
                        Toast.makeText(requireContext(), "Permiso "+p0.permissionName+" ha sido denegado.", Toast.LENGTH_SHORT).show()
                    }
                }



            }).check()

        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.uber_maps_style))
            if (!success)
                Log.e("EDMT_ERROR","Style parsing error")
        }catch (e:Resources.NotFoundException){
            Log.e("EDMT ERROR", e.message ?: "Error desconocido.")
        }


    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onDriverRequestReceived(event: DriverRequestReceived){

        driverRequestReceived = event
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(requireView(), getString(R.string.permiso_denegado), Snackbar.LENGTH_LONG).show()
            return
        }
        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e -> Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show() }
            .addOnSuccessListener { location ->

                compositeDisposable.add(iGoogleAPI.getDirections("driving", "less_driving",
                    StringBuilder()
                        .append(location.latitude)
                        .append(",")
                        .append(location.longitude)
                        .toString(),
                    event.pickupLocation,
                    getString(R.string.google_api_key))
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{returnResult ->
                        Log.d("APIRETURN", returnResult)
                        try {
                            val jsonObject = JSONObject(returnResult)
                            val jsonArray = jsonObject.getJSONArray("routes")
                            for (i in 0 until jsonArray.length()) {
                                val route = jsonArray.getJSONObject(i)
                                val poly = route.getJSONObject("overview_polyline")
                                val polyline = poly.getString("points")
                                polylineList = decodePoly(polyline)
                            }
                            polylineOptions = PolylineOptions()
                            polylineOptions!!.color(Color.GRAY)
                            polylineOptions!!.width(12f)
                            polylineOptions!!.startCap(SquareCap())
                            polylineOptions!!.jointType(JointType.ROUND)
                            polylineOptions!!.addAll(polylineList!!)
                            grayPolyline = mMap.addPolyline(polylineOptions!!)

                            blackPolylineOptions = PolylineOptions()
                            blackPolylineOptions!!.color(Color.BLACK)
                            blackPolylineOptions!!.width(5f)
                            blackPolylineOptions!!.startCap(SquareCap())
                            blackPolylineOptions!!.jointType(JointType.ROUND)
                            blackPolylineOptions!!.addAll(polylineList!!)
                            blackPolyline = mMap.addPolyline(blackPolylineOptions!!)

                            // Animador
                            val valueAnimator = ValueAnimator.ofInt( 0, 100)
                            valueAnimator.duration = 1100
                            valueAnimator.repeatCount = ValueAnimator.INFINITE
                            valueAnimator.interpolator = LinearInterpolator()
                            valueAnimator.addUpdateListener { value ->
                                val points = grayPolyline!!.points
                                val percentValue = value.animatedValue.toString().toInt()
                                val size = points.size
                                val newpoints = (size * (percentValue / 100.0f)).toInt()
                                val p = points.subList(0, newpoints)
                                blackPolyline!!.setPoints(p)
                            }
                            valueAnimator.start()

                            val origin = LatLng(location.latitude, location.longitude)
                            val destination = LatLng(event.pickupLocation!!.split(",")[0].toDouble(),
                                event.pickupLocation!!.split(",")[1].toDouble())

                            // Límites del mapa
                            val latLngBounds = LatLngBounds.Builder()
                                .include(origin)
                                .include(destination)
                                .build()

                            val objects = jsonArray.getJSONObject(0)
                            val legs =objects.getJSONArray("legs")
                            val legsObjects = legs.getJSONObject(0)

                            val time =legsObjects.getJSONObject("duración")
                            val duration = time.getString("text")

                            val distanceEstimate =legsObjects.getJSONObject("distance")
                            val distance = distanceEstimate.getString("text")

                            txt_estimate_time.setText(duration)
                            txt_estimate_distance.setText(distance)

                            mMap.addMarker(MarkerOptions().position(destination).icon(
                                BitmapDescriptorFactory.defaultMarker())
                                .title("Pickup location"))

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                            chip_decline.visibility = View.VISIBLE
                            layout_accept.visibility = View.VISIBLE

                            countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext { x ->
                                    circularProgressBar.progress += 1f
                                }
                                .takeUntil{aLong -> aLong == "100".toLong()}
                                .doOnComplete {
                                    createTripPlan(event, duration, distance)
                                }.subscribe()

                        }catch (e:java.lang.Exception){
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
    }

    private fun createTripPlan(event: DriverRequestReceived, duration: String, distance: String) {
        setLayoutProcess(true)

        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(error: DatabaseError){
                    Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
                }
                override fun onDataChange(snapshot: DataSnapshot){
                    val timeOffset = snapshot.getValue(Long::class.java)

                    FirebaseDatabase.getInstance()
                        .getReference(Common.RIDER_INFO)
                        .child(event!!.key!!)
                        .addListenerForSingleValueEvent(object:ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    val riderModel = snapshot.getValue(RiderModel::class.java)

                                    if (ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {

                                        return
                                    }
                                    fusedLocationProviderClient!!.lastLocation
                                        .addOnFailureListener { e ->
                                            Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                                        }
                                        .addOnSuccessListener { location ->
                                            val tripPlanModel = TripPlanModel()
                                            tripPlanModel.driver = FirebaseAuth.getInstance().currentUser!!.uid
                                            tripPlanModel.rider = event!!.key
                                            tripPlanModel.driverInfoModel = Common.currentUser
                                            tripPlanModel.riderModel = riderModel
                                            tripPlanModel.origin = event.pickupLocation
                                            tripPlanModel.originString = event.pickupLocationString
                                            tripPlanModel.destination = event.destinationLocation
                                            tripPlanModel.destinationString = event.destinationLocationString
                                            tripPlanModel.distancePickup = distance
                                            tripPlanModel.durationPickup = duration
                                            tripPlanModel.currentLat = location.latitude
                                            tripPlanModel.currentLng = location.longitude
                                            tripNumberId = Common.createUniqueTripIdNumber(timeOffset)

                                            //Submit
                                            FirebaseDatabase.getInstance().getReference(Common.TRIP)
                                                .child(tripNumberId!!)
                                                .setValue(tripPlanModel)
                                                .addOnFailureListener { e ->
                                                    Snackbar.make(mapFragment.requireView()!!, e!!.message!!, Snackbar.LENGTH_LONG).show()
                                                }
                                                .addOnSuccessListener {
                                                    txt_rider_name.text = riderModel!!.firstName
                                                    txt_start_uber_estimate_distance.text = distance
                                                    txt_start_uber_estimate_time.text = duration
                                                    setOfflineModeForDriver(event, duration, distance)
                                                }

                                        }
                                }
                                else{
                                    Snackbar.make(mapFragment.requireView(), "Rider not found!"+" "+event!!.key!!, Snackbar.LENGTH_LONG).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
                            }

                        })
                }
            })
    }

    private fun setOfflineModeForDriver(event: DriverRequestReceived, duration: String, distance: String){
        if (currentUserRef != null){
            currentUserRef!!.removeValue()
        }

        setLayoutProcess(false)
        layout_accept.visibility = View.GONE
        layout_start_uber.visibility = View.VISIBLE

        isTripStart = true
    }

    private fun setLayoutProcess(process: Boolean) {
        var color = -1
        if (process){
            color = ContextCompat.getColor(requireContext(), R.color.dark_gray)
            circularProgressBar.indeterminateMode = true
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_check_24, 0)
        }
        else{
            color = ContextCompat.getColor(requireContext(), android.R.color.white)
            circularProgressBar.indeterminateMode = false
            circularProgressBar.progress = 0F
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_check_24, 0)
        }

        txt_estimate_time.setTextColor(color)
        txt_estimate_distance.setTextColor(color)
        txt_rating.setTextColor(color)
        txt_type_uber.setTextColor(color)
        ImageViewCompat.setImageTintList(img_round, ColorStateList.valueOf(color))

    }

}