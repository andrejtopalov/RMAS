package com.example.locastory

data class User(var username: String ="", var score: Double) {

    constructor() : this("", 0.0)
}