package com.example.tsl_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tsl_app.pojo.response.AndroidGetPipeRemarksResponse
import com.example.tsl_app.R

class RemarksAdapter(private val remarksList: List<AndroidGetPipeRemarksResponse>) : RecyclerView.Adapter<RemarksAdapter.MyViewHolder>() {
    // Create ViewHolder for each item in the RecyclerView
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var snotxt: TextView
        var usernametxt: TextView
        var datetimetxt: TextView

        init {
            snotxt = itemView.findViewById(R.id.snotxt)
            usernametxt = itemView.findViewById(R.id.usernametxt)
            datetimetxt = itemView.findViewById(R.id.datetimetxt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.remarkslayoutrecycler, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemText = remarksList[position]
        holder.snotxt.text = (position + 1).toString()
        holder.usernametxt.text = itemText.userName
        holder.datetimetxt.text = itemText.remarkDate
    }

    override fun getItemCount(): Int {
        return remarksList.size
    }
}

