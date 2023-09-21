package com.example.locastory

import android.app.Activity
import android.app.Dialog
import android.location.Location
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

object StoryUtils {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun retrieveAllStories(callback: (List<Story>) -> Unit) {
        val storyList = mutableListOf<Story>()

        val usersRef = databaseReference.child("users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key

                    if (userId != null) {
                        val storiesRef = databaseReference.child("users").child(userId).child("stories")

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

                                callback(storyList)
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



    fun calculateDistance(location1: LatLng, location2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            location1.latitude, location1.longitude,
            location2.latitude, location2.longitude,
            results
        )
        return results[0]
    }

    fun showStoryDialog(activity: Activity, story: Story) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.story_dialog)
        val userID = story.userId

        val textViewStoryTitle: TextView = dialog.findViewById(R.id.sdTitle)
        val textViewStoryBody: TextView = dialog.findViewById(R.id.sdBody)
        val textViewStoryCategory: TextView = dialog.findViewById(R.id.sdCategory)
        val likeButton: Button = dialog.findViewById(R.id.LikeButton)


        textViewStoryTitle.text = story.title
        textViewStoryBody.text = story.body
        textViewStoryCategory.text = story.category

        likeButton.setOnClickListener{
            LikeStory(story,userID)
            activity.finish()
        }

        dialog.show()
    }

    private fun LikeStory(story: Story, userID: String) {
        val userRef = databaseReference.child("users").child(userID)
        userRef.child("score").get().addOnSuccessListener { dataSnapshot ->
            val currentScore = dataSnapshot.value as? Long ?: 0
            userRef.child("score").setValue(currentScore + 50)

        }
    }



}