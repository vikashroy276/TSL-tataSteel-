package com.example.tsl_app.activities.mappedpipe

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tsl_app.R
import com.example.tsl_app.adapter.MapPipeAdapter
import com.example.tsl_app.databinding.ActivityMapPipeBinding
import com.example.tsl_app.pojo.request.InsertManualRequest
import com.example.tsl_app.pojo.request.MappedPipeRequest
import com.example.tsl_app.pojo.request.SaveUntagPipeRequest
import com.example.tsl_app.pojo.response.MappedPipeResponse
import com.example.tsl_app.pojo.response.SaveUntagPipeResponse
import com.example.tsl_app.restapi.ApiClient
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** The MapPipeActivity is an Android AppCompatActivity that serves as a screen in your app to
 * display and manage a list of mapped pipes. It interacts with an API to fetch, display,
 * and update data related to mapped pipes.
 * 1.Display a list of mapped pipes fetched from an API.
 * 2.Allow users to refresh the list using a SwipeRefreshLayout
 * 3.Handle user interactions like clicking on a pipe to unmap it
 * 4.Provide navigation back to the previous screen
 * 5.Handle API responses and errors
 * 6.ViewBinding for the activity
 * 7.Adapter for the RecyclerView to display the list of mapped pipes
 * Created By Vikas Roy */

class MapPipeActivity : AppCompatActivity(), MapPipeAdapter.OnItemClickListener {
    lateinit var binding: ActivityMapPipeBinding
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mapPipeAdapter: MapPipeAdapter
    private val mappedPipeList = mutableListOf<MappedPipeResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        getTagMappedDetails()
        searchPipe()

        binding.backIcon.setOnClickListener {
            finish()
        }
        swipeRefreshLayout = binding.swapRefresh

        swipeRefreshLayout.setOnRefreshListener {
            getTagMappedDetails()  // Refresh data from the API
            swipeRefreshLayout.isRefreshing = false
        }

        binding.stage1.setOnClickListener {
            mapPipeAdapter.sortBy("I")
        }

        binding.stage2.setOnClickListener {
            mapPipeAdapter.sortBy("T")
        }

        binding.stage3.setOnClickListener {
            mapPipeAdapter.sortBy("EF")
        }
    }

    /**
     * Sets the layout manager for the RecyclerView to display items in a vertical list
     * Assigns the adapter to the RecyclerView
     * Adds a divider between list items for better UI separation**/
    private fun setupRecyclerView() {
        mapPipeAdapter = MapPipeAdapter(mappedPipeList, this)
        binding.tagRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tagRecyclerView.adapter = mapPipeAdapter
        binding.tagRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL
            )
        )
    }

    /**
     * Adds a text change listener to the search box input field
     * Calls the adapter's filter function to filter data based on user input
     * Ensure mapPipeAdapter.filter() handles null or empty input gracefully
     */
    private fun searchPipe() {
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mapPipeAdapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getTagMappedDetails() {
        val request = MappedPipeRequest()
        ApiClient.getClient(this)?.getTagMappedDetailsAPI(request)
            ?.enqueue(object : Callback<List<MappedPipeResponse>> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<List<MappedPipeResponse>>,
                    response: Response<List<MappedPipeResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()
                        if (!data.isNullOrEmpty()) {
                            mappedPipeList.clear()
                            mappedPipeList.addAll(data)
                            setupRecyclerView()
                            mapPipeAdapter.notifyDataSetChanged()
                            binding.searchBox.text.clear()
                        }
                    }
                }

                override fun onFailure(call: Call<List<MappedPipeResponse>>, t: Throwable) {
                    Log.e("API Response 12345", "Failure: ${t.message}")
                }
            })
    }

    /**
     *Handles item click events for mapped pipes
     * Calls a function to save or unmap the selected pipe
     */
    override fun onItemClick(mappedPipe: MappedPipeResponse) {
        saveUnmapPipe(mappedPipe)
    }

    /** Handles the event when a dialog needs to be shown
     *  Calls a function to insert remarks based on the pipe and dialog type*/
    override fun onShowDialog(mappedPipe: MappedPipeResponse, type: String?) {
        insertRemarks(mappedPipe, type)
    }

    /**Handles the event when a status dialog is triggered
     * Displays a dialog indicating the pending status of the selected pipe*/
    override fun onStatusDialog(mappedPipe: MappedPipeResponse) {
        showPendingDialog(mappedPipe)
    }


    private fun saveUnmapPipe(mappedPipe: MappedPipeResponse) {
        val request = SaveUntagPipeRequest().apply {
            pipeid = mappedPipe.pm_pipe_id
            tagno = mappedPipe.TagNo
        }

        ApiClient.getClient(this)?.saveUntagPipeAPI(request)
            ?.enqueue(object : Callback<SaveUntagPipeResponse> {
                override fun onResponse(
                    call: Call<SaveUntagPipeResponse>, response: Response<SaveUntagPipeResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.response == true) {
                            val successMessage =
                                body.responseMessage ?: "Pipe unmapped successfully"
                            DialogManager.showSuccessNavigate(this@MapPipeActivity, successMessage)
                            getTagMappedDetails()
                        } else {
                            val errorMessage =
                                body?.responseMessage ?: "Failed to unmap pipe: Unknown error"
                            DialogManager.showErrorDialog(this@MapPipeActivity, errorMessage)
                        }
                    } else {
                        val errorMessage =
                            response.errorBody()?.string() ?: "Failed to unmap pipe: Unknown error"
                        DialogManager.showErrorDialog(this@MapPipeActivity, errorMessage)
                    }
                }

                override fun onFailure(call: Call<SaveUntagPipeResponse>, t: Throwable) {
                    DialogManager.showErrorDialog(this@MapPipeActivity, "Failed to unmap pipe")
                }
            })

    }

    /** This APi is created for insert remarks in manual entry */
    private fun insertTagInManualAPI(
        mappedPipe: MappedPipeResponse, remarksText: String, type: String?
    ) {
        val request = InsertManualRequest().apply {
            pm_rfid_station = type
            pm_pipeid = mappedPipe.pm_pipe_id
            pm_pipecode = mappedPipe.pm_pipe_code
            pm_procsheetid = mappedPipe.pm_procsheet_id
            pm_tagno = mappedPipe.TagNo
            pm_rfid_IP = ""
            emp_id = CacheUtils.getEmployeeId(this@MapPipeActivity)?.toString()
            remarks = remarksText
        }

        ApiClient.getClient(this)?.insertTagInManualAPI(request)
            ?.enqueue(object : Callback<SaveUntagPipeResponse> {
                override fun onResponse(
                    call: Call<SaveUntagPipeResponse>, response: Response<SaveUntagPipeResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.response == true) {
                            val successMessage =
                                body.responseMessage ?: "Remarks saved successfully"
                            DialogManager.showSuccessNavigate(this@MapPipeActivity, successMessage)
                            getTagMappedDetails()
                        } else {
                            val errorMessage =
                                body?.responseMessage ?: "Failed to save remarks: Unknown error"
                            DialogManager.showErrorDialog(this@MapPipeActivity, errorMessage)

                        }
                    } else {
                        val errorMessage =
                            response.errorBody()?.string() ?: "Failed to save remark: Unknown error"
                        DialogManager.showErrorDialog(this@MapPipeActivity, errorMessage)

                    }
                }

                override fun onFailure(call: Call<SaveUntagPipeResponse>, t: Throwable) {
                    DialogManager.showErrorDialog(
                        this@MapPipeActivity, "Failed to save remark: ${t.message}"
                    )
                }
            })
    }

    /** This is dialog box for insert Remarks status of pipe*/
    @SuppressLint("SetTextI18n")
    private fun insertRemarks(mappedPipe: MappedPipeResponse, type: String?) {
        val dialog = Dialog(binding.root.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.remark_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val okButton = dialog.findViewById<CardView>(R.id.okBtn)
        val crossImage = dialog.findViewById<ImageView>(R.id.crossimage)
        val title = dialog.findViewById<TextView>(R.id.message)
        val remarksEditText = dialog.findViewById<EditText>(R.id.remarks)

        title.text = "Pipe/Tag :${mappedPipe.pm_pipe_code}/${mappedPipe.TagNo},  $type"

        crossImage.setOnClickListener {
            dialog.dismiss()
        }

        okButton.setOnClickListener {
            val remarks = remarksEditText.text.toString()
            dialog.dismiss()
            insertTagInManualAPI(mappedPipe, remarks, type)
        }

        dialog.show()
    }

    /** This is dialog box for pending status of pipe */
    private fun showPendingDialog(mappedPipe: MappedPipeResponse) {
        val dialog = Dialog(binding.root.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.passwordvalidate_layout)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val okButton = dialog.findViewById<CardView>(R.id.okay_btn)
        val errorMessage = dialog.findViewById<TextView>(R.id.errorText)
        errorMessage.text = getPendingStatusMessage(mappedPipe.pending_status)

        okButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /** using this function we are showing message in dialog box based on pending status*/
    private fun getPendingStatusMessage(pendingStatus: Int?): String {
        return when (pendingStatus) {
            1 -> "Pending in Inlet Entry"
            2 -> "Pending in Blasting Entry"
            3 -> "Pending in Application Entry"
            else -> "Unknown status"
        }
    }
}