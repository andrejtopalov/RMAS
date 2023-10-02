package com.example.locastory

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide


class ProfileActivity : AppCompatActivity() {

    private lateinit var profilePic: ImageView
    private lateinit var name: TextView
    private lateinit var userName: TextView
    private lateinit var email: TextView
    private lateinit var phoneNo: TextView
    private lateinit var btnHome: Button

    private lateinit var progressBar: ProgressBar
/*
    Mozda nepotrebne pomocne promenljive
    private lateinit var nameVar: String
    private lateinit var userNameVar: String
    private lateinit var emailVar: String
    private lateinit var phoneNoVar: String
*/
    private lateinit var authProfile: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePic = findViewById(R.id.ivProfile)
        name = findViewById(R.id.tvName)
        userName = findViewById(R.id.tvUsername)
        email = findViewById(R.id.tvEmail)
        phoneNo = findViewById(R.id.tvPhoneNo)
        btnHome = findViewById(R.id.btnHome)
        progressBar = findViewById(R.id.progressBar)



        authProfile = FirebaseAuth.getInstance()
        val currentUser = authProfile.currentUser

        if(currentUser == null) {
            Toast.makeText(this,"Something went wrong! User's details are not available at the moment.",Toast.LENGTH_LONG).show()
        }
        else{
            progressBar.visibility = View.VISIBLE
            showUserProfile(currentUser)

        }

        btnHome.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
        }


    }

    private fun showUserProfile(currentUser: FirebaseUser) {


        authProfile = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.value as? Map<String, Any>
                    userData?.let {
                        val profilePicUrl = userData["profilePicUrl"] as? String
                        val firstName = userData["firstName"] as? String
                        val lastName = userData["lastName"] as? String
                        val umail = userData["email"] as? String
                        val phoneN = userData["phoneNo"] as? String
                        val usName = userData["username"] as? String


                        profilePicUrl?.let {
                            Glide.with(this@ProfileActivity)
                                .load(it)
                                .into(profilePic)
                        }

                        val fullNameText = "$firstName $lastName"
                        name.text = fullNameText
                        email.text = umail
                        phoneNo.text = phoneN
                        userName.text = usName
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Database Error: ${databaseError.message}")
            }
        })
        progressBar.visibility = View.INVISIBLE
    }
}