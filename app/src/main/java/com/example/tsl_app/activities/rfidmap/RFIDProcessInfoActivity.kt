package com.example.tsl_app.activities.rfidmap

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tsl_app.pojo.response.AndroidGetShiftIdResponse
import com.example.tsl_app.pojo.request.ShiftIdRequest
import com.example.tsl_app.pojo.response.GetRFIDDetailsResponse
import com.example.tsl_app.pojo.request.RFIDDetailsRequest
import com.example.tsl_app.pojo.response.GetProcSheetNoResponse
import com.example.tsl_app.pojo.request.ProcSheetNoRequest
import com.example.tsl_app.pojo.response.ProcSheetYearResponse
import com.example.tsl_app.pojo.RFIDDetail
import com.example.tsl_app.adapter.RFIDTallyDetailsAdapter
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import com.example.tsl_app.databinding.ActivityRfidprocessSheetBinding
import com.example.tsl_app.restapi.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

/**
 * This activity is created for RFID-based processing.
 * It serves as an entry point for RFID-based processing, allowing users to fetch
and verify RFID details before proceeding further.
 * It provides a structured way to handle user input, API communication, and data display.
 * It improves user experience by ensuring data validation, error handling, and intuitive navigation.
 * It integrates with backend APIs to retrieve relevant processing information.
 * UI Initialization:Uses ActivityRfidProcessSheetBinding for view binding
 * Network Connectivity Check:Before making any API calls, it verifies if an
internet connection is available.Displays an error dialog if no connection is found.
 * Year Selection & API Call
 * Error Handling & Dialog Management
 * RecyclerView Adapter Setup */

class RFIDProcessInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityRfidprocessSheetBinding
    val yearsList: ArrayList<String> = ArrayList()
    private val rfidDetailsList = ArrayList<RFIDDetail>()
    private lateinit var rfidDetailsAdapter: RFIDTallyDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRfidprocessSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            NoInternetConnectionDialog.showDialog(
                this@RFIDProcessInfoActivity, "Please check your internet connection and try again."
            )
        }
        getYearAPI()

        rfidDetailsAdapter = RFIDTallyDetailsAdapter(rfidDetailsList)
        binding.tallyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tallyRecyclerView.adapter = rfidDetailsAdapter

        binding.year.setOnClickListener {
            showYearDialog()
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.seqNo.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.seqNo.isCursorVisible = true // Make the cursor visible again when touched
            }
            false
        }

        binding.seqNo.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val inputText = binding.seqNo.text.toString().trim()
                if (binding.year.text.toString().isEmpty()) {
                    DialogManager.showErrorDialog(
                        this@RFIDProcessInfoActivity, "Please Select Year!!"
                    )
                } else if (inputText.isEmpty()) {
                    DialogManager.showErrorDialog(
                        this@RFIDProcessInfoActivity, "Please Enter Valid Seq. No!!"
                    )
                } else {
                    binding.seqNo.isCursorVisible = false
                    getProcessSheetNoRequestApi(binding.year.text.toString(), inputText)
                    getShiftIidApi(inputText.toInt())

                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.seqNo.windowToken, 0)
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * This method fetches available years from the API and populates the yearsList,
     * which is likely used for selecting a year in the UI */
    private fun getYearAPI() {
        ApiClient.getClient(this)?.getProcSheetYear()
            ?.enqueue(object : Callback<List<ProcSheetYearResponse>> {
                override fun onResponse(
                    call: Call<List<ProcSheetYearResponse>>,
                    response: Response<List<ProcSheetYearResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val yearResponse: List<ProcSheetYearResponse> = response.body()!!
                        for (item in yearResponse) {
                            val pipeNumber = item.year
                            yearsList.add(pipeNumber.toString())
                            Log.e("Year List", "" + yearsList.toString())
                        }
                    } else {
                        DialogManager.showErrorDialog(
                            this@RFIDProcessInfoActivity,
                            "Error: ${response.code()} ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<List<ProcSheetYearResponse>>, t: Throwable) {
                    DialogManager.showErrorDialog(this@RFIDProcessInfoActivity, "Server Error !!")
                }
            })
    }

    /**
     * This method displays a dialog that allows the user to select a year from a predefined list
    (yearsList). When the user selects a year, it updates the year field (binding.year.text)
    in the UI.*/
    private fun showYearDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Select Year")
        builder.setItems(yearsList.toTypedArray()) { _, which ->
            val selectedItem: String = yearsList[which]
            binding.year.text = selectedItem
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * This method fetches process sheet details from an API based on the selected year and
    sequence number (seqNo). The retrieved process sheet number is displayed in the UI
    and then used to fetch RFID details.
     * */
    private fun getProcessSheetNoRequestApi(year: String, seqNo: String) {
        val getProcessSheetNoRequest = ProcSheetNoRequest()
        getProcessSheetNoRequest.year = year.toInt()
        getProcessSheetNoRequest.seqno = seqNo

        ApiClient.getClient(this)?.getProcSheetNoRequest(getProcessSheetNoRequest)
            ?.enqueue(object : Callback<GetProcSheetNoResponse> {
                override fun onResponse(
                    call: Call<GetProcSheetNoResponse>, response: Response<GetProcSheetNoResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val shipmentResponse: GetProcSheetNoResponse? = response.body()
                        val procSheetNo: String? = shipmentResponse?.procsheetno
                        binding.processSheetNo.text = procSheetNo
                        getRFIDDetailsAPI(shipmentResponse?.procsheetno, year, seqNo)
                    } else {
                        binding.processSheetNo.text = ""
                    }
                }

                override fun onFailure(call: Call<GetProcSheetNoResponse>, t: Throwable) {
                    DialogManager.showErrorDialog(
                        this@RFIDProcessInfoActivity, t.message.toString()
                    )
                }
            })
    }

    /**
     * This method is responsible for fetching RFID details based on the provided process
    sheet number, year, and sequence number. The details are then displayed in the UI,
    and if valid, allow navigation to the RFID scanning activity.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getRFIDDetailsAPI(procsheetno: String?, year: String, seqNo: String) {
        val rfidDetailRequest = RFIDDetailsRequest().apply {
            this.procsheetno = procsheetno
            this.procseq = seqNo
            this.procyear = year.toInt()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.getClient(this@RFIDProcessInfoActivity)
                    ?.getRFIDDetails(rfidDetailRequest)?.awaitResponse()
                withContext(Dispatchers.Main) {
                    if (response != null && response.isSuccessful && response.body() != null) {
                        val rfidDetailResponse: List<GetRFIDDetailsResponse> = response.body()!!
                        if (rfidDetailResponse.isEmpty()) {
                            clearField()
                            DialogManager.showErrorDialog(
                                this@RFIDProcessInfoActivity, "Please Input Valid Details!"
                            )
                        } else {
                            rfidDetailsList.clear()
                            for (item in rfidDetailResponse) {
                                binding.clientName.text = item.clientName
                                binding.projectName.text = item.projectName
                                binding.pipeSize.text = item.pipeSize
                                rfidDetailsList.add(
                                    RFIDDetail(
                                        item.tallysheetno, item.numberofpipes
                                    )
                                )

                                binding.nextBtn.setOnClickListener {
                                    when {
                                        binding.year.text.isNullOrEmpty() -> {
                                            DialogManager.showErrorDialog(
                                                this@RFIDProcessInfoActivity, "Please Select Year!"
                                            )
                                        }

                                        binding.seqNo.text.isNullOrEmpty() -> {
                                            DialogManager.showErrorDialog(
                                                this@RFIDProcessInfoActivity,
                                                "Please Enter Seq. No!"
                                            )
                                        }

                                        else -> {
                                            val intent = Intent(
                                                this@RFIDProcessInfoActivity,
                                                ScanRFIDActivity::class.java
                                            ).apply {
                                                putExtra("clientName", item.clientName)
                                                putExtra("projectName", item.projectName)
                                                putExtra("pipeSize", item.pipeSize)
                                                putExtra("processSheetId", item.procsheetid)
                                                putExtra("processSheetCode", procsheetno)
                                                putExtra("processSheetSeqNo", seqNo)
                                                putExtra("processSheetYear", year)
                                                putExtra("pipeId", item.pipeid)
                                            }
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                            rfidDetailsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        DialogManager.showErrorDialog(
                            this@RFIDProcessInfoActivity,
                            "Failed to retrieve RFID details: ${response?.errorBody()?.string()}"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    DialogManager.showErrorDialog(
                        this@RFIDProcessInfoActivity, "Server Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * The clearField() method is responsible for resetting UI fields and clearing the list
    of RFID details when needed. It ensures that old or incorrect data is removed before
    new data is fetched.*/
    @SuppressLint("NotifyDataSetChanged")
    private fun clearField() {
        binding.clientName.text = ""
        binding.projectName.text = ""
        binding.pipeSize.text = ""
        rfidDetailsList.clear()
        rfidDetailsAdapter.notifyDataSetChanged()
    }


    /**
     * The getShiftIidApi() function is responsible for retrieving a Shift ID associated
    with a given sequence number (seqNoStr). The Shift ID is then cached for further use.
    This method performs an API request and handles both success and failure scenarios.*/
    private fun getShiftIidApi(seqNoStr: Int) {
        val androidGetShiftIdRequest = ShiftIdRequest()
        androidGetShiftIdRequest.id = seqNoStr

        ApiClient.getClient(this)?.getshiftidApi(androidGetShiftIdRequest)
            ?.enqueue(object : Callback<List<AndroidGetShiftIdResponse>> {
                override fun onResponse(
                    call: Call<List<AndroidGetShiftIdResponse>>,
                    response: Response<List<AndroidGetShiftIdResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val pipeNumberResponse: List<AndroidGetShiftIdResponse> = response.body()!!
                        if (pipeNumberResponse.isNotEmpty()) {
                            for (item in pipeNumberResponse) {
                                val shiftID = item.shiftid
                                CacheUtils.saveShiftId(this@RFIDProcessInfoActivity, shiftID)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<AndroidGetShiftIdResponse>>, t: Throwable) {
                    DialogManager.showErrorDialog(
                        this@RFIDProcessInfoActivity, t.message.toString()
                    )
                }
            })
    }

}