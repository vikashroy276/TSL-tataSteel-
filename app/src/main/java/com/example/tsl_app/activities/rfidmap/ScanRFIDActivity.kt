package com.example.tsl_app.activities.rfidmap


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.tsl_app.adapter.PipeNumberAutoCompleteAdapter
import com.example.tsl_app.databinding.ActivityScanRfidBinding
import com.example.tsl_app.pojo.Pipemodel
import com.example.tsl_app.pojo.request.PipeByPsNoRequest
import com.example.tsl_app.pojo.request.RfidMapPipeIdRequest
import com.example.tsl_app.pojo.response.PipeByPsNoResponse
import com.example.tsl_app.pojo.response.RfidMapPipeIdResponse
import com.example.tsl_app.restapi.ApiClient
import com.example.tsl_app.rfid.BaseActivity
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.Converter
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import com.example.tsl_app.utils.ProgressDialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * The RFIDProcessInfoActivity page is designed to facilitate RFID-based tracking and data
 * retrieval for process sheets. It allows users to input a year and sequence number to fetch
 * corresponding process sheet details and RFID-related information.
 * Retrieve Process Sheet Information,Fetch RFID Details,Validate and Display Data
 * Prepare Data for Next Step (RFID Scanning
 * Error Handling & User Interaction
 * View binding for easier access to UI components
 * List to store available years for selection
 */

class ScanRFIDActivity : BaseActivity() {
    lateinit var binding: ActivityScanRfidBinding
    private val pipeNumberList: ArrayList<Pipemodel> = ArrayList()
    private var rfidCode: String? = null
    lateinit var processSheetSeqNo: String
    lateinit var processSheetCode: String
    lateinit var processSheetYear: String
    private lateinit var progressDialog: ProgressDialog
    private lateinit var pipeNumberAutoCompleteAdapter: PipeNumberAutoCompleteAdapter
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanRfidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Reading Tags...") // Set the message
        progressDialog.setCancelable(true) // Prevent dismissal by tapping outside
        autoCompleteTextView = binding.autoComplete
        pipeNumberAutoCompleteAdapter = PipeNumberAutoCompleteAdapter(this, pipeNumberList)

        getIntentDataAndUI()

    }

    private fun autoCompleteText(pipeNumberList: ArrayList<Pipemodel>) {
        autoCompleteTextView.setAdapter(pipeNumberAutoCompleteAdapter)
        pipeNumberAutoCompleteAdapter.notifyDataSetChanged()
        autoCompleteTextView.showDropDown() // Force dropdown to appea
        autoCompleteTextView.setText(pipeNumberList.firstOrNull()?.pipeNo ?: "")
        // Set listener for selection
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedPipe = pipeNumberAutoCompleteAdapter.getItem(position)
            Toast.makeText(this, "Selected: ${selectedPipe.pipeNo}", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun getIntentDataAndUI() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            NoInternetConnectionDialog.showDialog(
                this@ScanRFIDActivity, "Please check your internet connection and try again."
            )
        }
        val intent = intent
        val clientName = intent.getStringExtra("clientName")
        val projectName = intent.getStringExtra("projectName")
        val pipeSize = intent.getStringExtra("pipeSize")
        processSheetCode = intent.getStringExtra("processSheetCode").toString()
        processSheetSeqNo = intent.getStringExtra("processSheetSeqNo").toString()
        processSheetYear = intent.getStringExtra("processSheetYear").toString()
        val processSheetId = intent.getStringExtra("processSheetId")
        val pipeId = intent.getStringExtra("pipeId")?.toInt()
        CacheUtils.savePipeId(this@ScanRFIDActivity, pipeId)

        binding.clientName.text = clientName
        binding.processSheetNo.text = projectName
        binding.projectName.text = pipeSize
        getNoOfPipeBypSnoApi(processSheetCode, processSheetSeqNo.toInt(), processSheetYear.toInt())

        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.saveBtn.setOnClickListener {
            rfidMapPipeIdApi(
                binding.autoComplete.text.toString(),
                rfidCode.toString().trim(),
                processSheetId?.toInt()
            )
        }
    }


    private fun getNoOfPipeBypSnoApi(
        processSheetCode: String?,
        processSeq: Int?,
        processYear: Int?
    ) {
        val request = PipeByPsNoRequest().apply {
            this.procSheetNo = processSheetCode
            this.procseqno = processSeq
            this.procyear = processYear
        }

        ApiClient.getClient(this)?.pipeNoByPsNoRequest(request)
            ?.enqueue(object : Callback<List<PipeByPsNoResponse>> {
                override fun onResponse(
                    call: Call<List<PipeByPsNoResponse>>,
                    response: Response<List<PipeByPsNoResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        pipeNumberList.clear()
                        response.body()?.let { data ->
                            pipeNumberList.addAll(data.map { Pipemodel(it.pipeid, it.pipeNo,it.aslno) })
                            println("Pipe Numbers List: ${pipeNumberList.map { it.pipeNo }}")
                        }
                        autoCompleteText(pipeNumberList)

                    }
                }

                override fun onFailure(call: Call<List<PipeByPsNoResponse>>, t: Throwable) {
                    DialogManager.showErrorDialog(this@ScanRFIDActivity, t.message.toString())
                }
            })
    }


    private fun rfidMapPipeIdApi(
        pipeCode: String, rfidCode: String, processSheetId: Int?
    ) {
        ProgressDialogUtils.showLoader(this)
        val rfidMapPipeIdRequest = RfidMapPipeIdRequest()
        rfidMapPipeIdRequest.procsheetid = processSheetId
        rfidMapPipeIdRequest.pipeCode = pipeCode
        rfidMapPipeIdRequest.rfidCode = rfidCode
        rfidMapPipeIdRequest.emp_id = CacheUtils.getEmployeeId(this)
        rfidMapPipeIdRequest.shiftid = CacheUtils.getShiftId(this)
        rfidMapPipeIdRequest.pipe_id = CacheUtils.getPipeId(this@ScanRFIDActivity)
        rfidMapPipeIdRequest.rfidtagid = "0"
        rfidMapPipeIdRequest.rfidepc = "0"
        Log.e("Map", "map---" + CacheUtils.getPipeId(this@ScanRFIDActivity))

        ApiClient.getClient(this)?.rfidMapPipeIdApi(rfidMapPipeIdRequest)
            ?.enqueue(object : Callback<RfidMapPipeIdResponse> {
                override fun onResponse(
                    call: Call<RfidMapPipeIdResponse>, response: Response<RfidMapPipeIdResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        ProgressDialogUtils.hideLoader()
                        val rfidMapPipeIdResponse: RfidMapPipeIdResponse = response.body()!!
                        if (rfidMapPipeIdResponse.response.equals("Y")) {
                            getNoOfPipeBypSnoApi(
                                processSheetCode,
                                processSheetSeqNo.toInt(),
                                processSheetYear.toInt()
                            )
                            binding.autoComplete.setText("")
                            binding.rfid.text = ""

                            DialogManager.showSuccessNavigate(
                                this@ScanRFIDActivity,
                                rfidMapPipeIdResponse.responseMessage.toString()
                            )
                        } else {
                            Log.e(
                                "processSheet code , Year , Seq No",
                                "$processSheetCode--$processSheetYear-$processSheetSeqNo"
                            )
                            DialogManager.showErrorDialog(
                                this@ScanRFIDActivity,
                                rfidMapPipeIdResponse.responseMessage.toString()
                            )
                        }

                    }
                }

                override fun onFailure(call: Call<RfidMapPipeIdResponse>, t: Throwable) {
                    DialogManager.showErrorDialog(this@ScanRFIDActivity, t.message.toString())
                }
            })
    }

    @SuppressLint("SetTextI18n")
    fun handleTagData(tagData: String) {
        if (tagData.isNotEmpty()) {
            val tmpTagData = Converter.hexaToString(tagData).replace("\\u0000".toRegex(), "")
                .replace("\n", "")  // Remove newline characters
                .replace("\r", "")  // Remove carriage returns
                .replace(Regex("[^A-Za-z0-9]"), "")  // Keep only alphanumeric characters
                .trim()
            runOnUiThread {
                binding.rfid.text = tmpTagData // Set new scanned tag directly
                rfidCode = tmpTagData
            }
        } else {
            runOnUiThread {
                binding.rfid.text = "Please scan again\n"
            }
        }
    }


    override fun handleSetText(msg: String?) {
    }

    override fun onScannedData(data: String?) {
    }

}