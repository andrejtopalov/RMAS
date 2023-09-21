package com.example.locastory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class HomeActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var btnMap: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ivProfile = findViewById(R.id.ivProfile)
        btnMap = findViewById(R.id.btnMap)

        ivProfile.setOnClickListener {
           val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

        btnMap.setOnClickListener {
            val intent = Intent(this,MapActivity::class.java)
            startActivity(intent)
        }


    }
}