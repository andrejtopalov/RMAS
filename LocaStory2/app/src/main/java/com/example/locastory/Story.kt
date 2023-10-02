package com.example.locastory

import android.location.Location
import java.io.Serializable
import java.security.Timestamp
import java.util.Date

data class Story (
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val rating: String = "",
    val numOfRatings: String = ""
) : Serializable

