package com.example.tsl_app.activities.searchProcTallyPipe.searchPipeNumber

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tsl_app.pojo.request.SearchReportByTSPipeRequest
import com.example.tsl_app.pojo.response.AndroidGetPipeRemarksResponse
import com.example.tsl_app.pojo.response.AndroidGetStationNameResponse
import com.example.tsl_app.pojo.request.PipeRemarksRequest
import com.example.tsl_app.activities.selectRFIDandSearch.TagPipeRFIDActivity
import com.example.tsl_app.adapter.RemarksAdapter
import com.example.tsl_app.adapter.StationAdapter
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import com.example.tsl_app.databinding.FragmentPipeNumberBinding
import com.example.tsl_app.pojo.request.StationNameRequest
import com.example.tsl_app.pojo.response.RfidMapPipeIdResponse
import com.example.tsl_app.pojo.response.SearchReportByPsTsNResponse
import com.example.tsl_app.restapi.ApiClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PipeNumberFragment : Fragment() {
    lateinit var binding: FragmentPipeNumberBinding
    private var stationList: List<AndroidGetStationNameResponse>? = null
    private var stationAdapter: StationAdapter? = null
    private var remarksList: List<AndroidGetPipeRemarksResponse>? = null
    private var remarksAdapter: RemarksAdapter? = null
    lateinit var processSheetId: String
    private lateinit var stationResponse: MutableList<AndroidGetStationNameResponse>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPipeNumberBinding.inflate(inflater, container, false)

        stationResponse = mutableListOf()
        initUi()
        setupListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        clearFields()
    }


    private fun initUi() {
        if (!NetworkUtils.isNetworkAvailable(requireActivity())) {
            NoInternetConnectionDialog.showDialog(
                requireActivity(), "Please check your internet connection and try again."
            )
        }


        remarksList = ArrayList()
        stationList = ArrayList()

        binding.remarksRecycler.layoutManager = LinearLayoutManager(requireActivity())
        binding.stationrecycler.layoutManager = LinearLayoutManager(requireActivity())
    }


    private fun setupListeners() {

        binding.SubmitBtn.setOnClickListener {
            if (binding.remarksEdt.text.toString() == "") {
                DialogManager.showErrorDialog(requireActivity(), "Please enter remarks !")
            } else {
                getAndroidInsertPipeRemarksApi(binding.remarksEdt.text.toString())
            }
        }

        binding.AddBtn.setOnClickListener {
            val isVisible = binding.remarkCard.visibility == View.GONE
            binding.remarkCard.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.remarkLayout.visibility = View.GONE
        }
        binding.ShowBtn.setOnClickListener {
            getAndroidGetPipeRemarksApi()
            val isVisible = binding.remarkLayout.visibility == View.GONE
            binding.remarkLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.remarkCard.visibility = View.GONE
        }
        binding.pipeNumTxt.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val inputText = binding.pipeNumTxt.text.toString().trim()
                if (inputText.isEmpty()) {
                    DialogManager.showErrorDialog(requireActivity(), "Please Enter pipe no!!")
                } else {
                    binding.pipeNumTxt.isCursorVisible = false
                    getAndroidGetSearchReportByTSPipeRequestApi("", inputText)
                    // Hide the keyboard
                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.pipeNumTxt.windowToken, 0)
                }
                true
            } else {
                false
            }
        }
    }


    private fun getAndroidGetSearchReportByTSPipeRequestApi(
        tallysheetno: String?, pipeNo: String?
    ) {
        val searchReportByTSPipeRequest = SearchReportByTSPipeRequest()
        searchReportByTSPipeRequest.tallysheetno = tallysheetno
        searchReportByTSPipeRequest.pipeno = pipeNo

        ApiClient.getClient(requireActivity())
            ?.searchReportByTSPipeRequestApi(searchReportByTSPipeRequest)
            ?.enqueue(object : Callback<List<SearchReportByPsTsNResponse>> {
                override fun onResponse(
                    call: Call<List<SearchReportByPsTsNResponse>>,
                    response: Response<List<SearchReportByPsTsNResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val rfidDetailResponse: List<SearchReportByPsTsNResponse> =
                            response.body()!!
                        if (rfidDetailResponse.isEmpty()) {
                            clearFields()
                            DialogManager.showErrorDialog(
                                requireActivity(), "Please Input Valid Details !"
                            )
                        } else {
                            androidGetStationNameWithPipeNo(binding.pipeNumTxt.text.toString())
                            var count = 0
                            for (item in rfidDetailResponse) {
                                binding.clientNameTxt.text = item.clientName
                                binding.projectNameTxt.text = item.projectName
                                binding.pipeSizeTxt.text = item.pipeSize
                                binding.procSheetCode.text = item.procsheetCode
                                binding.pipeNoTxt.text = item.pipeNumber

                                processSheetId = item.procsheetid.toString()
                                CacheUtils.savePipeId(requireActivity(), item.pipeid)
                                CacheUtils.saveString(
                                    requireActivity(), "procsheetno", item.procsheetSeqNo.toString()
                                )
                                if (binding.procSheetCode.text == item.procsheetCode && binding.pipeNoTxt.text == item.pipeNumber) {
                                    count++ // Increment the count if both conditions are met
                                }
                            }
                            binding.sNoTxt.text = count.toString()
                        }


                    }
                }

                override fun onFailure(
                    call: Call<List<SearchReportByPsTsNResponse>>, t: Throwable
                ) {
                    DialogManager.showErrorDialog(requireActivity(), t.message.toString())
                }
            })

    }

    private fun androidGetStationNameWithPipeNo(pipeNo: String) {
        val stationNameRequest = StationNameRequest()
        stationNameRequest.seqno = 0
        stationNameRequest.year = 0
        stationNameRequest.psCode = pipeNo


        ApiClient.getClient(requireActivity())
            ?.stationNameWithPipeNo(stationNameRequest)
            ?.enqueue(object : Callback<List<AndroidGetStationNameResponse>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<List<AndroidGetStationNameResponse>>,
                    response: Response<List<AndroidGetStationNameResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        stationResponse = response.body()?.toMutableList() ?: mutableListOf()
                        if (stationResponse.isNotEmpty()) {
                            stationAdapter = StationAdapter(stationResponse)
                            binding.stationrecycler.setAdapter(stationAdapter)
                            stationResponse.firstOrNull()?.let { item ->
                                binding.currentStationName.text = item.currentstationname ?: "N/A"
                                binding.currentTestDate.text = item.currenttestdate ?: "N/A"
                                binding.taggedByName.text = "-${item.taggedBy ?: "Unknown"}"
                                binding.taggedondate.text = "-${item.taggedOn ?: "Unknown"}"
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<List<AndroidGetStationNameResponse>>, t: Throwable
                ) {
                    DialogManager.showErrorDialog(requireActivity(), t.message.toString())
                }
            })

    }


    private fun getAndroidInsertPipeRemarksApi(remarksEdt: String) {
        val pipeRemarksRequest = PipeRemarksRequest()
        pipeRemarksRequest.deviceid = DialogManager.getDeviceId(requireActivity())
        pipeRemarksRequest.psid =
            CacheUtils.getString(requireActivity(), "procsheetno")
        pipeRemarksRequest.pipeid = CacheUtils.getPipeId(requireActivity())
        pipeRemarksRequest.piperemarks = remarksEdt
        pipeRemarksRequest.emp_id = CacheUtils.getEmployeeId(requireActivity())

        ApiClient.getClient(requireActivity())
            ?.getandroidInsertPipeRemarksApi(pipeRemarksRequest)
            ?.enqueue(object : Callback<RfidMapPipeIdResponse> {
                override fun onResponse(
                    call: Call<RfidMapPipeIdResponse>,
                    response: Response<RfidMapPipeIdResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val insertPipeRemarksResponse: RfidMapPipeIdResponse? =
                            response.body()
                        if (insertPipeRemarksResponse?.response.equals("N")) {
                            DialogManager.showErrorDialog(
                                requireActivity(),
                                insertPipeRemarksResponse?.responseMessage.toString()
                            )
                        } else {
                            DialogManager.showSuccessDialog(
                                requireActivity(),
                                insertPipeRemarksResponse?.responseMessage.toString(),
                                TagPipeRFIDActivity::class.java
                            )

                        }

                    }
                }

                override fun onFailure(call: Call<RfidMapPipeIdResponse>, t: Throwable) {
                    DialogManager.showErrorDialog(requireActivity(), t.message.toString())
                }
            })

    }

    private fun getAndroidGetPipeRemarksApi() {
        val androidGetPipeRemarksRequest = PipeRemarksRequest()
        androidGetPipeRemarksRequest.PipeNo = binding.pipeNumTxt.text.toString()

        ApiClient.getClient(requireActivity())
            ?.getandroidGetPipeRemarksApi(androidGetPipeRemarksRequest)
            ?.enqueue(object : Callback<List<AndroidGetPipeRemarksResponse>> {
                override fun onResponse(
                    call: Call<List<AndroidGetPipeRemarksResponse>>,
                    response: Response<List<AndroidGetPipeRemarksResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val rfidDetailResponse: List<AndroidGetPipeRemarksResponse> =
                            response.body()!!
                        if (rfidDetailResponse.isNotEmpty()) {
                            remarksAdapter = RemarksAdapter(rfidDetailResponse)
                            binding.remarksRecycler.setAdapter(remarksAdapter)
                        }
                    }
                }

                override fun onFailure(
                    call: Call<List<AndroidGetPipeRemarksResponse>>, t: Throwable
                ) {
                    DialogManager.showErrorDialog(requireActivity(), t.message.toString())
                }
            })

    }



    /**
     * The clearField() method is responsible for resetting UI fields and clearing the list
    of RFID details when needed. It ensures that old or incorrect data is removed before
    new data is fetched.*/
    @SuppressLint("NotifyDataSetChanged")
    private fun clearFields() {
        binding.pipeNumTxt.setText("")
        binding.sNoTxt.text = ""
        binding.clientNameTxt.text = ""
        binding.projectNameTxt.text = ""
        binding.pipeSizeTxt.text = ""
        binding.procSheetCode.text = ""
        binding.pipeNoTxt.text = ""
        binding.currentStationName.text = ""
        binding.currentTestDate.text = ""
        binding.taggedByName.text = ""
        binding.taggedondate.text = ""
        stationResponse.clear()
        stationAdapter?.notifyDataSetChanged()

    }
}