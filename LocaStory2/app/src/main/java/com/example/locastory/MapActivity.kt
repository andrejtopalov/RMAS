@file:Suppress("SameParameterValue")

package com.example.locastory

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
//import com.example.locastory.StoryUtils.retrieveAllStories
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

@Suppress("DEPRECATION", "KotlinConstantConditions")
class MapActivity : AppCompatActivity(), OnMapReadyCallback

{
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var addStory: Button

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var storyList: ArrayList<Story>

    private lateinit var radius: EditText
    private lateinit var radiusBtn: ImageButton

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


        storyList = ArrayList<Story>()
       // retrieveAllStories()

        radius = findViewById(R.id.etDistance)
        radiusBtn = findViewById(R.id.btnDistance)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY))
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val yourLocation = lastLocation?.let { LatLng(it.latitude, it.longitude) }

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

        addStory = findViewById(R.id.btnAddStory)

        addStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        radiusBtn.setOnClickListener {
            filterByRadius()
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        outState.putParcelable(CAMERA_POSITION_KEY, savedCameraPosition)
    }




    private fun addMarker(story: Story) {
        val database = FirebaseDatabase.getInstance()
        val storiesRef = database.getReference("stories")

        val newStoryRef = storiesRef.push()

        newStoryRef.setValue(story)

        val storyLocation = LatLng(story.latitude, story.longitude)
        val markerOptions = MarkerOptions()
            .position(storyLocation)
            .title(story.title)
            .snippet(story.category)
        googleMap.addMarker(markerOptions)
    }





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

            retrieveAllStories()
            Log.e("test",storyList.toString())
            //addMarkerstoMap(storyList)

        } else {
            requestLocationPermission()
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

    /*
    private fun addMarkersToMap() {

        val usersRef = StoryUtils.databaseReference.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key

                    if (userId != null) {
                        val storiesRef = StoryUtils.databaseReference.child("users").child(userId).child("stories")

                        storiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(storiesSnapshot: DataSnapshot) {
                                for (storiesSnapshot in storiesSnapshot.children) {
                                    val storyMap = storiesSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                                    if (storyMap != null) {

                                        val storyObject = Story(
                                            userId,
                                            storyMap["title"] as? String ?: "",
                                            storyMap["body"] as? String ?: "",
                                            storyMap["category"] as? String ?: "",
                                            storyMap["latitude"] as? Double ?: 0.0,
                                            storyMap["longitude"] as? Double ?: 0.0,
                                            storyMap["rating"] as? String ?: "0",
                                            storyMap["numOfRatings"] as? String ?: "0"
                                        )
                                        storyList.add(storyObject)
                                    }
                                }

                                Log.d("TEST", storyList.toString())
                                for (story in storyList) {
                                    val latitude = story.latitude
                                    val longitude = story.longitude
                                    val storyLocation = LatLng(latitude, longitude)

                                    val markerOptions = MarkerOptions()
                                        .position(storyLocation)
                                        .title(story.title + " (" + story.category + ")")

                                    val marker = googleMap.addMarker(markerOptions)
                                    marker?.tag = story
                                    marker?.showInfoWindow()
                                }

                                googleMap.setOnMarkerClickListener { clickedMarker ->
                                    val story = clickedMarker.tag as? Story
                                    if (story != null) {
                                        StoryUtils.showStoryDialog(this@MapActivity, story)
                                        true
                                    } else {
                                        false
                                    }
                                }


                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("Database Error", databaseError.toString())
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Database Error", databaseError.toString())
            }
        })


    }
*/
/*
    private fun FilteredStories(stories: ArrayList<Story>) {

        for (story in stories) {
            val latitude = story.latitude
            val longitude = story.longitude
            val storyLocation = LatLng(latitude, longitude)

            val markerOptions = MarkerOptions()
                .position(storyLocation)
                .title(story.title + " (" + story.category + ")")


            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = story
            marker?.showInfoWindow()
        }

        googleMap.setOnMarkerClickListener { clickedMarker ->
            val story = clickedMarker.tag as? Story
            if (story != null) {
                StoryUtils.showStoryDialog(this, story)
                true
            } else {
                false
            }
        }
    }
*/
    @SuppressLint("MissingPermission")
    private fun filterByRadius() {
        val radius = radius.text.toString().toFloatOrNull()

        if (radius != null) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    val filteredstories = storyList.filter { story ->
                        val storyLocation = LatLng(story.latitude, story.longitude)
                        val distance = StoryUtils.calculateDistance(userLocation, storyLocation) / 1000.0f
                        distance <= radius
                    }

                    googleMap.clear()
                    addMarkerstoMap(filteredstories as ArrayList<Story>)

                } else {
                    Toast.makeText(this, "Unable to retrieve user location.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Invalid radius value.", Toast.LENGTH_SHORT).show()
        }


    }


    private fun retrieveAllStories() {

        val usersRef = StoryUtils.databaseReference.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key

                    if (userId != null) {
                        val storiesRef = StoryUtils.databaseReference.child("users").child(userId).child("stories")

                        storiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(storiesSnapshot: DataSnapshot) {
                                for (storiesSnapshot in storiesSnapshot.children) {
                                    val storyMap = storiesSnapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})
                                    if (storyMap != null) {

                                        val storyObject = Story(
                                            userId,
                                            storyMap["title"] as? String ?: "",
                                            storyMap["body"] as? String ?: "",
                                            storyMap["category"] as? String ?: "",
                                            storyMap["latitude"] as? Double ?: 0.0,
                                            storyMap["longitude"] as? Double ?: 0.0,
                                            storyMap["rating"] as? String ?: "0",
                                            storyMap["numOfRatings"] as? String ?: "0"
                                        )
                                        storyList.add(storyObject)
                                        Log.e("test2",storyList.toString())
                                    }
                                }
                                for (story in storyList) {
                                    val latitude = story.latitude
                                    val longitude = story.longitude
                                    val storyLocation = LatLng(latitude, longitude)

                                    val markerOptions = MarkerOptions()
                                        .position(storyLocation)
                                        .title(story.title + " (" + story.category + ")")

                                    val marker = googleMap.addMarker(markerOptions)
                                    marker?.tag = story
                                    marker?.showInfoWindow()
                                }

                                googleMap.setOnMarkerClickListener { clickedMarker ->
                                    val story = clickedMarker.tag as? Story
                                    if (story != null) {
                                        StoryUtils.showStoryDialog(this@MapActivity, story)
                                        true
                                    } else {
                                        false
                                    }
                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e("Database Error", databaseError.toString())

                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Database Error", databaseError.toString())
            }
        })


    }


    private fun addMarkerstoMap(sList: ArrayList<Story>){

        for (story in sList) {
            val latitude = story.latitude
            val longitude = story.longitude
            val storyLocation = LatLng(latitude, longitude)

            val markerOptions = MarkerOptions()
                .position(storyLocation)
                .title(story.title + " (" + story.category + ")")

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = story
            marker?.showInfoWindow()
        }

        googleMap.setOnMarkerClickListener { clickedMarker ->
            val story = clickedMarker.tag as? Story
            if (story != null) {
                StoryUtils.showStoryDialog(this@MapActivity, story)
                true
            } else {
                false
            }
        }


    }


}