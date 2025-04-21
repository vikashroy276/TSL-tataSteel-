package com.example.tsl_app.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tsl_app.R
import com.example.tsl_app.databinding.ItemMappedPipeBinding
import com.example.tsl_app.pojo.response.MappedPipeResponse
import java.text.SimpleDateFormat
import java.util.Locale


class MapPipeAdapter(
    private val mappedPipeList: List<MappedPipeResponse>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<MapPipeAdapter.MapPipeViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(mappedPipe: MappedPipeResponse)
        fun onShowDialog(mappedPipe: MappedPipeResponse, type: String?)
        fun onStatusDialog(mappedPipe: MappedPipeResponse)
    }

    private var filteredList: List<MappedPipeResponse> = mappedPipeList
    private var isAscending = true

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            mappedPipeList.toMutableList()
        } else {
            mappedPipeList.filter { item ->
                item.pm_pipe_code.contains(query, ignoreCase = true) || item.TagNo.contains(
                    query, ignoreCase = true
                ) || item.pm_procsheet_id?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPipeViewHolder {
        val binding = ItemMappedPipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MapPipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapPipeViewHolder, position: Int) {
        holder.bind(filteredList[position], position)
    }

    override fun getItemCount(): Int = filteredList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortBy(field: String) {
        filteredList = if (isAscending) {
            filteredList.sortedBy { pipe ->
                when (field) {
                    "I" -> pipe.I
                    "T" -> pipe.T
                    "EF" -> pipe.EF
                    else -> 0
                }
            }
        } else {
            filteredList.sortedByDescending { pipe ->
                when (field) {
                    "I" -> pipe.I
                    "T" -> pipe.T
                    "EF" -> pipe.EF
                    else -> 0
                }
            }
        }

        isAscending = !isAscending // Toggle sorting order
        notifyDataSetChanged()
    }


    inner class MapPipeViewHolder(private val binding: ItemMappedPipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(mappedPipe: MappedPipeResponse, position: Int) {
            binding.apply {
                sNo.text = (position + 1).toString()
                pipe.text = formatPipeText(
                    mappedPipe.pm_pipe_code,
                    mappedPipe.TagNo,
                    mappedPipe.pm_IsNtc,
                    mappedPipe.pm_procsheet_id
                )
                setDotColor(inlate, mappedPipe.I)
                setDotColor(thick, mappedPipe.T)
                setDotColor(ef, mappedPipe.EF)

                iTime.text = formatDate(mappedPipe.I_date)
                tTime.text = formatDate(mappedPipe.T_date)
                efTime.text = formatDate(mappedPipe.EF_Date)

                val blueColor = Color.BLUE
                val redColor = Color.RED

                iTime.setTextColor(if (mappedPipe.I_ismannual == 1) blueColor else redColor)
                tTime.setTextColor(if (mappedPipe.T_ismannual == 1) blueColor else redColor)
                efTime.setTextColor(if (mappedPipe.EF_ismannual == 1) blueColor else redColor)

                inLetLay.isEnabled = mappedPipe.I == 0
                thickLay.isEnabled = mappedPipe.I == 1 && mappedPipe.T == 0
                efLay.isEnabled = mappedPipe.T == 1 && mappedPipe.EF == 0

                if (mappedPipe.I == 1 && mappedPipe.T == 1 && mappedPipe.EF == 1) {
                    inLetLay.isEnabled = false
                    thickLay.isEnabled = false
                    efLay.isEnabled = false
                }
            }

            binding.pipe.setOnClickListener {
                confirmDialog(mappedPipe)
            }

            binding.inLetLay.setOnClickListener {
                onItemClickListener.onShowDialog(mappedPipe, "Inlet")
            }
            binding.thickLay.setOnClickListener {
                if (mappedPipe.pending_status == 0) {
                    onItemClickListener.onShowDialog(mappedPipe, "Thickness")
                } else {
                    onItemClickListener.onStatusDialog(mappedPipe)
                }
            }
            binding.efLay.setOnClickListener {
                onItemClickListener.onShowDialog(mappedPipe, "EF")
            }
        }

        private fun formatPipeText(
            pipeCode: String, tagNo: String?, isNtc: Int?, psNo: String?
        ): SpannableString {
            val tag = tagNo ?: ""
            val ntcText = if (isNtc != null && isNtc != 0) "($isNtc)" else ""
            val fullText = "$pipeCode\n$tag $ntcText\n$psNo"
            val spannable = SpannableString(fullText)

            val tagStartIndex = fullText.indexOf(tag)
            if (tag.isNotEmpty() && tagStartIndex >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.BLUE),
                    tagStartIndex,
                    tagStartIndex + tag.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            return spannable
        }

        @SuppressLint("UseCompatTextViewDrawableApis")
        private fun setDotColor(textView: TextView, value: Int) {
            val dotColor = if (value == 1) Color.GREEN else Color.RED
            textView.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.dot, 0)
            textView.compoundDrawableTintList = ColorStateList.valueOf(dotColor)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        }

        /**
         * This function displays a confirmation dialog to the user before unmapping a pipe.
        If the user confirms, it triggers an action.*/
        @SuppressLint("SetTextI18n")
        private fun confirmDialog(mappedPipe: MappedPipeResponse) {
            val dialog = Dialog(binding.root.context)
            dialog.setContentView(R.layout.activity_unmapped)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialog.findViewById<TextView>(R.id.alertTitle).text = "Confirm Action"
            dialog.findViewById<TextView>(R.id.error).text =
                "Do you want to unmap this pipe: ${mappedPipe.pm_pipe_code}?"
            dialog.findViewById<TextView>(R.id.no_btn).setOnClickListener { dialog.dismiss() }
            dialog.findViewById<TextView>(R.id.yes_btn).setOnClickListener {
                onItemClickListener.onItemClick(mappedPipe)
                dialog.dismiss()
            }
            dialog.findViewById<ImageView>(R.id.crossimage).setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        /**
         * This function formats a date string from one format to another. It is designed to
         * handle timestamps and convert them into a more readable format.
         * */
        private fun formatDate(dateString: String?): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd-MMM HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateString ?: "")
                date?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }
        }
    }
}
