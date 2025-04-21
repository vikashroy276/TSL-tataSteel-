package com.example.tsl_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tsl_app.pojo.RFIDDetail
import com.example.tsl_app.R

class RFIDTallyDetailsAdapter(private val rfidDetails: List<RFIDDetail>) :
    RecyclerView.Adapter<RFIDTallyDetailsAdapter.RFIDViewHolder>() {

    inner class RFIDViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tallySheetNoTxt: TextView = itemView.findViewById(R.id.tallesheetnotxt)
        val numberOfPipesTxt: TextView = itemView.findViewById(R.id.noofpipestxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RFIDViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tallysheetlayoutproc, parent, false) // Replace with your layout
        return RFIDViewHolder(view)
    }

    override fun onBindViewHolder(holder: RFIDViewHolder, position: Int) {
        val currentItem = rfidDetails[position]
        holder.tallySheetNoTxt.text = currentItem.tallysheetno
        holder.numberOfPipesTxt.text = currentItem.numberofpipes
    }

    override fun getItemCount(): Int {
        return rfidDetails.size
    }
}

