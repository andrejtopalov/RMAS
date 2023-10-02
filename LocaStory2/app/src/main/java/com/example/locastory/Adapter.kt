package com.example.locastory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class Adapter(private val userItemList : ArrayList<User>) : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){


        val lname: TextView = itemView.findViewById(R.id.listName)
        val lscore: TextView = itemView.findViewById(R.id.listScore)

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent , false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userItemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = userItemList[position]
        holder.lname.text = user.username
        holder.lscore.text = user.score.toString()



    }
}