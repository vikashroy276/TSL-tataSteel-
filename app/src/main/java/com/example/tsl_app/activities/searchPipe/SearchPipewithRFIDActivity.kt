package com.example.tsl_app.activities.searchPipe

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.tsl_app.databinding.ActivitySearchPipewithRfidactivityBinding
import com.example.tsl_app.pojo.request.PipeDataByTagIdRequest
import com.example.tsl_app.pojo.response.GetPipeDataByTagIdResponse
import com.example.tsl_app.restapi.ApiClient
import com.example.tsl_app.rfid.BaseActivity
import com.example.tsl_app.utils.Converter
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPipeWithRFIDActivity : BaseActivity() {
    lateinit var binding: ActivitySearchPipewithRfidactivityBinding
    private var rfidCode: String? = null
    private lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchPipewithRfidactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Reading Tags...") // Set the message
        progressDialog.setCancelable(true) // Prevent dismissal by tapping outside

        getIntentDataAndUI()

        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    /*  */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getIntentDataAndUI() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            NoInternetConnectionDialog.showDialog(
                this@SearchPipeWithRFIDActivity,
                "Please check your internet connection and try again."
            )
        }
    }


    @SuppressLint("SetTextI18n")
    fun handleTagData(tagData: String) {
        if (tagData.isNotEmpty()) {
            val tmpTagData = Converter.hexaToString(tagData)

            runOnUiThread {
                binding.rfid.text = tmpTagData // Set new scanned tag directly
                rfidCode = tmpTagData

                val cleanedTagId =
                    binding.rfid.text.toString().replace("\n", "")  // Remove newline characters
                        .replace("\r", "")  // Remove carriage returns
                        .replace(Regex("[^A-Za-z0-9]"), "")  // Keep only alphanumeric characters
                        .trim()  // Trim spaces

                getPipeDataByTagIdAPI(cleanedTagId)
            }
        } else {
            runOnUiThread {
                binding.rfid.text = "Please scan again\n"
            }
        }
    }

    private fun getPipeDataByTagIdAPI(tagId: String) {
        val pipeDataByTagIdRequest = PipeDataByTagIdRequest()
        pipeDataByTagIdRequest.tagid = tagId
        Log.e("Tag Id", "s$tagId")

        ApiClient.getClient(this)?.getPipedatabytagidAPI(pipeDataByTagIdRequest)
            ?.enqueue(object : Callback<List<GetPipeDataByTagIdResponse>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<List<GetPipeDataByTagIdResponse>>,
                    response: Response<List<GetPipeDataByTagIdResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val pipeNumberResponse: List<GetPipeDataByTagIdResponse> = response.body()!!
                        if (pipeNumberResponse.isNotEmpty()) {
                            var count = 0
                            for (item in pipeNumberResponse) {
                                val pipeNumber = item.pipeNumber
                                Log.e("Pipe Number", "" + item.pipeNumber)
                                binding.pipeNo.text = pipeNumber
                                binding.clientName.text = item.clientName
                                binding.projectName.text = item.projectName
                                binding.pipeSize.text = item.pipeSize
                                binding.processSheetCode.text = item.procsheetCode
                                binding.pipeNum.setText(item.pipeNumber)
                                binding.taggedBy.text = "-${item.taggedBy ?: "Unknown"}"
                                binding.taggedDate.text = "-${item.taggedOn ?: "Unknown"}"
                                if (binding.processSheetCode.text == item.procsheetCode && binding.pipeNo.text == item.pipeNumber) {
                                    count++ // Increment the count if both conditions are met
                                }
                            }
                            binding.sNo.text = count.toString()
                        } else {
                            clearFields()
                        }
                    }
                }

                override fun onFailure(call: Call<List<GetPipeDataByTagIdResponse>>, t: Throwable) {
                    DialogManager.showErrorDialog(
                        this@SearchPipeWithRFIDActivity, t.message.toString()
                    )
                }
            })
    }


    override fun handleTriggerPress(pressed: Boolean) {
        try {
            if (pressed) {
                runOnUiThread { binding.rfid.text = "" }
                rfidHandler?.performInventory()
                Thread.sleep(1000)
                rfidHandler?.stopInventory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun handleSetText(msg: String?) {
    }

    override fun onScannedData(data: String?) {
    }



    /**
     * The clearField() method is responsible for resetting UI fields and clearing the list
    of RFID details when needed. It ensures that old or incorrect data is removed before
    new data is fetched.*/
    private fun clearFields() {
        binding.pipeNum.setText("")
        binding.sNo.text = ""
        binding.clientName.text = ""
        binding.projectName.text = ""
        binding.pipeSize.text = ""
        binding.processSheetCode.text = ""
        binding.pipeNo.text = ""
        binding.taggedBy.text = ""
        binding.taggedDate.text = ""
    }
}