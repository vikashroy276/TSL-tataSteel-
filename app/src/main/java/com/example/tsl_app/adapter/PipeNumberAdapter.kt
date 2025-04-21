package com.example.tsl_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tsl_app.R
import com.example.tsl_app.pojo.Pipemodel

class PipeNumberAdapter(
    private var pipeNumberList: List<Pipemodel>,
    private val onItemClick: (Pipemodel) -> Unit // Lambda function to handle click
) : RecyclerView.Adapter<PipeNumberAdapter.ViewHolder>(), Filterable {

    private var filteredPipeNumberList: List<Pipemodel> = pipeNumberList

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pipeNumberText: TextView = itemView.findViewById(R.id.pipeNumberText)

        init {
            // Setting the click listener for the individual item
            itemView.setOnClickListener {
                onItemClick(filteredPipeNumberList[adapterPosition])  // Call the callback function with the pipe number
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pipe_number, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pipeNumberText.text = filteredPipeNumberList[position].pipeNo
    }

    override fun getItemCount(): Int {
        return filteredPipeNumberList.size
    }

    // Implement Filterable interface
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim() ?: ""

                filteredPipeNumberList = if (query.isEmpty()) {
                    pipeNumberList
                } else {
                    pipeNumberList.filter {
                        it.pipeNo!!.contains(query, ignoreCase = true)
                    }
                }

                return FilterResults().apply { values = filteredPipeNumberList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPipeNumberList = results?.values as List<Pipemodel>
                notifyDataSetChanged()
            }
        }
    }
}
