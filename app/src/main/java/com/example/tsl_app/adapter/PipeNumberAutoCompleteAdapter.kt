package com.example.tsl_app.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.example.tsl_app.pojo.Pipemodel

class PipeNumberAutoCompleteAdapter(
    context: Context,
    private val pipeNumberList: ArrayList<Pipemodel> = ArrayList()
) : ArrayAdapter<Pipemodel>(context, android.R.layout.simple_dropdown_item_1line, pipeNumberList), Filterable {

    private var filteredPipeNumberList: List<Pipemodel> = pipeNumberList

    override fun getCount(): Int {

        return filteredPipeNumberList.size
    }

    override fun getItem(position: Int): Pipemodel {
        return filteredPipeNumberList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase() ?: ""

                filteredPipeNumberList = if (query.isEmpty()) {
                    pipeNumberList
                } else {
                    pipeNumberList.filter {
                        it.pipeNo?.lowercase()?.contains(query) == true ||
                                it.aslno?.lowercase()?.contains(query) == true
                    }
                }

                return FilterResults().apply {
                    values = filteredPipeNumberList
                    count = filteredPipeNumberList.size
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPipeNumberList = results?.values as? List<Pipemodel> ?: emptyList()
                notifyDataSetChanged()  // Refresh the adapter
            }
        }
    }

}
