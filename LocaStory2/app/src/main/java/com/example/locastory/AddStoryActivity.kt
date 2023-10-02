package com.example.locastory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddStoryActivity : AppCompatActivity(){


    private lateinit var categorySpinner: Spinner
    private lateinit var title: EditText
    private lateinit var body: EditText
    private lateinit var saveBtn: Button

    private lateinit var category: String
    private lateinit var titleVar: String
    private lateinit var bodyVar: String

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        val categories = categories

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storage = FirebaseStorage.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        categorySpinner = findViewById(R.id.categorySpineer)
        title = findViewById(R.id.ettitle)
        body = findViewById(R.id.etbody)
        saveBtn = findViewById(R.id.btnSave)

        val categoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(applicationContext, "Please select a category!", Toast.LENGTH_SHORT).show()
            }
        }

        saveBtn.setOnClickListener {
            val title1 = title.text.toString()
            val body1 = body.text.toString()

            if (title1.isNullOrBlank()) {
                Toast.makeText(this, "Please write a title!", Toast.LENGTH_SHORT).show()
            } else if (body1.isNullOrBlank()) {
                Toast.makeText(this, "Please write a story!", Toast.LENGTH_SHORT).show()
            } else {
                addStory(title1, body1, category)
                val mapIntent = Intent(this, MapActivity::class.java)
                startActivity(mapIntent)
                finish()
            }
        }
    }

    private fun addStory(title: String, body: String, category: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        val story = Story(userId, title, body, category, location.latitude, location.longitude, "0", "0")
                        val storiesRef = databaseReference.child("users").child(userId).child("stories")
                        storiesRef.push().setValue(story)
                    }
                } else {
                    Log.e("Location Error", "Location not available!")
                }
            }
    }
}
