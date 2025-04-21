package com.example.tsl_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tsl_app.pojo.response.AndroidGetStationNameResponse
import com.example.tsl_app.R

/**
 * Create ViewHolder for each item in the RecyclerView*/
class StationAdapter(private val stationList: List<AndroidGetStationNameResponse>) :
    RecyclerView.Adapter<StationAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var stationTraName: TextView = itemView.findViewById(R.id.stationTraName)
        var stationTravelDate: TextView = itemView.findViewById(R.id.stationTravelDate)
    }

    /**
     * Inflate the item layout and create a new ViewHolder*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.stationrecycler, parent, false)
        return MyViewHolder(itemView)
    }

    /**Bind data to the ViewHolder*/
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemText = stationList[position]
        holder.stationTraName.text = itemText.stationtravelledname
        holder.stationTravelDate.text = itemText.stationtravelledtestdate
    }

    /**Return the size of the data list*/
    override fun getItemCount(): Int {
        return stationList.size
    }
}

