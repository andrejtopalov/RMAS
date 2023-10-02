package com.example.locastory

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class HomeActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var btnMap: Button
    private lateinit var authProfile: FirebaseAuth

    private lateinit var database: DatabaseReference
    private lateinit var recycleView: RecyclerView
    private lateinit var users: ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ivProfile = findViewById(R.id.ivProfile)
        btnMap = findViewById(R.id.btnMap)

        recycleView = findViewById(R.id.recyclerview)
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.setHasFixedSize(true)

        users = arrayListOf<User>()
        getUsersData()

        ivProfile.setOnClickListener {
           val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

        btnMap.setOnClickListener {
            val intent = Intent(this,MapActivity::class.java)
            startActivity(intent)
        }
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")




    }

    private fun getUsersData() {
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = arrayListOf<User>()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                userList.sortByDescending { it.score }

                val adapter = Adapter(userList)
                recycleView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database Error: ${error.message}")
            }
        })

    }






}