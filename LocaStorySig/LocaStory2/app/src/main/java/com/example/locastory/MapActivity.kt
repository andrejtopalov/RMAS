package com.example.locastory

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback

{
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val CAMERA_POSITION_KEY = "CameraPositionKey"
    //private var mapViewBundle: Bundle? = null
    private var savedCameraPosition: CameraPosition? = null
    private val FINE_LOCATION_PERMISSION_REQUEST_CODE = 100
    private val COARSE_LOCATION_PERMISSION_REQUEST_CODE = 101
    //Trebaju mi 2 promenljive za fine i coarse location
    //private var FINE_LOCATION: String = Manifest.permission.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY))
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val yourLocation = lastLocation?.let { LatLng(it.latitude, it.longitude) }
                //googleMap.addMarker(MarkerOptions().position(yourLocation).title("Your Location"))
                if (savedCameraPosition == null && googleMap.cameraPosition == null) {
                    val cameraPosition = CameraPosition.Builder()
                        .target(yourLocation!!)
                        .zoom(15f)
                        .build()

                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
            }
        }
        requestLocationUpdates()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        outState.putParcelable(CAMERA_POSITION_KEY, savedCameraPosition)
    }

    /*override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (checkLocationPermission()) {
            // Permission is already granted, enable the map features
            initializeMap()
        } else {
            // Permission is not granted, request it
            requestLocationPermission()
        }
        // Set up the map to allow adding markers on click
        googleMap.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }
    }*/

    private fun initializeMap() {

        // Pomeramo kameru na Nis
        val location = LatLng(43.3209, 21.8958) //Nis
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun checkLocationPermission(): Boolean {
        val permission1 = Manifest.permission.ACCESS_FINE_LOCATION
        val permission2 = Manifest.permission.ACCESS_COARSE_LOCATION
        val check1 = ContextCompat.checkSelfPermission(this, permission1) == PackageManager.PERMISSION_GRANTED
        val check2 = ContextCompat.checkSelfPermission(this, permission2) == PackageManager.PERMISSION_GRANTED
        return check1 && check2
    }

    private fun requestLocationPermission() {
        val permission1 = Manifest.permission.ACCESS_FINE_LOCATION
        val permission2 = Manifest.permission.ACCESS_COARSE_LOCATION
        if(ContextCompat.checkSelfPermission(this, permission1) == PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(permission2), COARSE_LOCATION_PERMISSION_REQUEST_CODE)
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(permission1), FINE_LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE || requestCode == COARSE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the map
                initializeMap()
            } else {
                // Permission denied, show a toast
                Toast.makeText(this, "Location permission denied. Cannot access the map.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*private void getDeviceLocation(){

    }*/

    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Marker Title")
            .snippet("Marker Snippet")

        googleMap.addMarker(markerOptions)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

   /* override fun onMapReady(googleMap: GoogleMap) {
        val sydney = LatLng(-33.852, 151.211)
        googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
    }*/

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1500
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }



    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            showLocationSettingsDialog()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (checkLocationPermission()) {
            getDeviceLocation()
            googleMap.isMyLocationEnabled = true
            // Permission is already granted, enable the map features
            //initializeMap()
        } else {
            // Permission is not granted, request it
            requestLocationPermission()
        }

        // Set up the map to allow adding markers on click
        googleMap.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }
    }

    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("Please grant location permission to use this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getDeviceLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (checkLocationPermission()) {
                val location = fusedLocationClient.lastLocation
                location.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentLocation = task.result
                        moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), 15f)
                    } else {
                        Toast.makeText(this,"Current location is not available!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            e.message
        }
    }



    private fun moveCamera(latLng: LatLng, zoom: Float) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}