package com.example.tsl_app.activities.searchProcTallyPipe.searchTallySheet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tsl_app.activities.selectRFIDandSearch.TagPipeRFIDActivity
import com.example.tsl_app.adapter.PipeNumberAdapter
import com.example.tsl_app.adapter.RemarksAdapter
import com.example.tsl_app.adapter.StationAdapter
import com.example.tsl_app.databinding.FragmentTallySheetBinding
import com.example.tsl_app.pojo.Pipemodel
import com.example.tsl_app.pojo.request.SearchReportByTSPipeRequest
import com.example.tsl_app.pojo.request.PipeRemarksRequest
import com.example.tsl_app.pojo.request.PipesByPsNoTsNoRequest
import com.example.tsl_app.pojo.request.StationNameRequest
import com.example.tsl_app.pojo.response.AndroidGetPipeRemarksResponse
import com.example.tsl_app.pojo.response.AndroidGetStationNameResponse
import com.example.tsl_app.pojo.response.RfidMapPipeIdResponse
import com.example.tsl_app.pojo.response.SearchReportByPsTsNResponse
import com.example.tsl_app.restapi.ApiClient
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TallySheetFragment : Fragment() {
    lateinit var binding: FragmentTallySheetBinding
    private var stationList: List<AndroidGetStationNameResponse>? = null
    private var stationAdapter: StationAdapter? = null
    private var remarksList: List<AndroidGetPipeRemarksResponse>? = null
    private var remarksAdapter: RemarksAdapter? = null
    lateinit var procsheetid: String
    lateinit var tallysheetnostr: String
    private lateinit var stationresponse: MutableList<AndroidGetStationNameResponse>
    val pipenumberList: ArrayList<Pipemodel> = ArrayList()
    private lateinit var pipeNumberAdapter: PipeNumberAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTallySheetBinding.inflate(inflater, container, false)
        stationresponse = mutableListOf()
        initUi()
        setUpListeners()
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
        binding.stationRecycler.layoutManager = LinearLayoutManager(requireActivity())
        if (binding.tallySheetNoTxt.text.toString() != "") {
            getnoofpipesbypsnotsnoAPI(binding.tallySheetNoTxt.text.toString())
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun setUpListeners() {
        binding.SubmitBtn.setOnClickListener {
            if (binding.remarksEdt.text.toString() == "") {
                DialogManager.showErrorDialog(requireActivity(), "Please enter remarks !")
            } else {
                getandroidInsertPipeRemarksApi(binding.remarksEdt.text.toString())
            }
        }
        binding.pipeNumberRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        pipeNumberAdapter = PipeNumberAdapter(pipenumberList) { selectedPipeNumber ->
            binding.pipeNumberTxt.setText(selectedPipeNumber.pipeNo)
            androidGetStationNameWithPipeNo(selectedPipeNumber.pipeNo.toString())
            CacheUtils.savePipeId(requireActivity(), selectedPipeNumber.pipeid)
            binding.pipeNumberRecyclerView.visibility = View.GONE
            DialogManager.hideKeyboard(requireActivity())
        }
        binding.pipeNumberRecyclerView.adapter = pipeNumberAdapter

        binding.pipeNumberTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isEmpty() == true || pipenumberList.isEmpty()) {
                    binding.pipeNumberRecyclerView.visibility = View.GONE
                } else {
                    binding.pipeNumberRecyclerView.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > 3) {
                    pipeNumberAdapter.filter.filter(s)
                }
            }
        })

        binding.AddBtn.setOnClickListener {
            val isVisible = binding.reamrksCard.visibility == View.GONE
            binding.reamrksCard.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.remarksLayout.visibility = View.GONE
        }
        binding.ShowBtn.setOnClickListener {
            getandroidGetPipeRemarksApi()
            val isVisible = binding.remarksLayout.visibility == View.GONE
            binding.remarksLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.reamrksCard.visibility = View.GONE
        }

        binding.tallySheetNoTxt.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.tallySheetNoTxt.isCursorVisible =
                    true // Make the cursor visible again when touched
            }
            false // Let other touch events proceed as usual
        }
        binding.tallySheetNoTxt.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val inputText = binding.tallySheetNoTxt.text.toString().trim()
                if (inputText.isEmpty()) {
                    DialogManager.showErrorDialog(
                        requireActivity(), "Please Enter Tally Sheet No!!"
                    )
                } else {
                    binding.tallySheetNoTxt.isCursorVisible = false
                    getAndroidGetSearchReportByTSPipeRequestApi(inputText, "")

                    // Hide the keyboard
                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(
                        binding.tallySheetNoTxt.windowToken, 0
                    )
                }
                true
            } else {
                false
            }
        }
    }

    private fun getandroidInsertPipeRemarksApi(remarksEdt: String) {
        val pipeRemarksRequest = PipeRemarksRequest()
        pipeRemarksRequest.deviceid = DialogManager.getDeviceId(requireActivity())
        pipeRemarksRequest.psid = CacheUtils.getString(requireActivity(), "procsheetno")
        pipeRemarksRequest.pipeid = CacheUtils.getPipeId(requireActivity())
        pipeRemarksRequest.piperemarks = remarksEdt
        pipeRemarksRequest.emp_id = CacheUtils.getEmployeeId(requireActivity())

        ApiClient.getClient(requireActivity())?.getandroidInsertPipeRemarksApi(pipeRemarksRequest)
            ?.enqueue(object : Callback<RfidMapPipeIdResponse> {
                override fun onResponse(
                    call: Call<RfidMapPipeIdResponse>, response: Response<RfidMapPipeIdResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val insertPipeRemarksResponse: RfidMapPipeIdResponse? = response.body()
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


    private fun getAndroidGetSearchReportByTSPipeRequestApi(
        tallySheetNo: String?, pipeNo: String?
    ) {
        val searchReportByTSPipeRequest = SearchReportByTSPipeRequest()
        searchReportByTSPipeRequest.tallysheetno = tallySheetNo
        searchReportByTSPipeRequest.pipeno = pipeNo


        ApiClient.getClient(requireActivity())
            ?.searchReportByTSPipeRequestApi(searchReportByTSPipeRequest)
            ?.enqueue(object : Callback<List<SearchReportByPsTsNResponse>> {
                override fun onResponse(
                    call: Call<List<SearchReportByPsTsNResponse>>,
                    response: Response<List<SearchReportByPsTsNResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val rfddetailresponse: List<SearchReportByPsTsNResponse> =
                            response.body()!!
                        if (rfddetailresponse.isEmpty()) {
                            clearFields()
                            DialogManager.showErrorDialog(
                                requireActivity(), "Please Input Valid Details !"
                            )
                        } else {
                            for (item in rfddetailresponse) {
                                binding.clientNameTxt.text = item.clientName
                                binding.projectNameTxt.text = item.projectName
                                binding.pipeSizeTxt.text = item.pipeSize
                                binding.pipeNumberTxt.setText(item.pipeNumber.toString())

                                tallysheetnostr = item.tallysheetno.toString()
                                getnoofpipesbypsnotsnoAPI(binding.tallySheetNoTxt.text.toString())
                                CacheUtils.savePipeId(requireActivity(), item.pipeid)
                                CacheUtils.saveString(
                                    requireActivity(), "procsheetno", item.procsheetSeqNo.toString()
                                )
                            }

                        }
                        androidGetStationNameWithPipeNo(binding.pipeNumberTxt.text.toString())
                    }
                }

                override fun onFailure(
                    call: Call<List<SearchReportByPsTsNResponse>>, t: Throwable
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
        binding.tallySheetNoTxt.setText("")
        binding.clientNameTxt.text = ""
        binding.projectNameTxt.text = ""
        binding.pipeSizeTxt.text = ""
        binding.pipeNumberTxt.setText("")
        binding.currentStationNameTxt.text = ""
        binding.currentTestDateTxt.text = ""
        binding.taggedByName.text = ""
        binding.taggedOnDate.text = ""
        stationresponse.clear()
        stationAdapter?.notifyDataSetChanged()

    }

    /* Created by Kalpana Yadav on 04/10/2024
      * get all station from AndroidGetStationNameWithPipeNo*/
    private fun androidGetStationNameWithPipeNo(pipeno: String) {
        val stationNameRequest = StationNameRequest().apply {
            seqno = 0
            year = 0
            psCode = pipeno
        }
        ApiClient.getClient(requireActivity())?.stationNameWithPipeNo(stationNameRequest)
            ?.enqueue(object : Callback<List<AndroidGetStationNameResponse>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<List<AndroidGetStationNameResponse>>,
                    response: Response<List<AndroidGetStationNameResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        stationresponse = response.body()?.toMutableList() ?: mutableListOf()

                        if (stationresponse.isNotEmpty()) {
                            stationAdapter = StationAdapter(stationresponse)
                            binding.stationRecycler.adapter = stationAdapter
                            stationresponse.firstOrNull()?.let { item ->
                                binding.currentStationNameTxt.text =
                                    item.currentstationname ?: "N/A"
                                binding.currentTestDateTxt.text = item.currenttestdate ?: "N/A"
                                binding.taggedByName.text = "-${item.taggedBy ?: "Unknown"}"
                                binding.taggedOnDate.text = "-${item.taggedOn ?: "Unknown"}"
                            }
                        }
                    } else {
                        DialogManager.showErrorDialog(requireActivity(), "No data found")
                    }
                }

                override fun onFailure(
                    call: Call<List<AndroidGetStationNameResponse>>, t: Throwable
                ) {
                    DialogManager.showErrorDialog(requireActivity(), t.message.toString())
                }
            })
    }

    private fun getandroidGetPipeRemarksApi() {
        val androidGetPipeRemarksrequest = PipeRemarksRequest()
        androidGetPipeRemarksrequest.PipeNo = binding.pipeNumberTxt.text.toString()
        androidGetPipeRemarksrequest.pipeid = CacheUtils.getPipeId(requireActivity())

        ApiClient.getClient(requireActivity())
            ?.getandroidGetPipeRemarksApi(androidGetPipeRemarksrequest)
            ?.enqueue(object : Callback<List<AndroidGetPipeRemarksResponse>> {
                override fun onResponse(
                    call: Call<List<AndroidGetPipeRemarksResponse>>,
                    response: Response<List<AndroidGetPipeRemarksResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val rfddetailresponse: List<AndroidGetPipeRemarksResponse> =
                            response.body()!!
                        if (rfddetailresponse.isNotEmpty()) {
                            remarksAdapter = RemarksAdapter(rfddetailresponse)
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


    private fun getnoofpipesbypsnotsnoAPI(tallysheetnostr: String?) {
        val pipesByPsNoTsNoRequest = PipesByPsNoTsNoRequest()
        pipesByPsNoTsNoRequest.proc_type = 1
        pipesByPsNoTsNoRequest.id = tallysheetnostr

        ApiClient.getClient(requireActivity())?.getnoofpipesbypsnotsnoAPI(pipesByPsNoTsNoRequest)
            ?.enqueue(object : Callback<List<SearchReportByPsTsNResponse>> {
                override fun onResponse(
                    call: Call<List<SearchReportByPsTsNResponse>>,
                    response: Response<List<SearchReportByPsTsNResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val pipenuberResponse: List<SearchReportByPsTsNResponse> =
                            response.body()!!
                        if (pipenuberResponse.isEmpty()) {
                            Log.e("getnoofpipesbypsnotsnoAPI", "No data found")
                        } else {
                            pipenumberList.clear()
                            for (item in pipenuberResponse) {
                                pipenumberList.add(
                                    Pipemodel(
                                        item.pipeid, item.pipeNumber
                                    )
                                )                            //  binding.pipenumberrtxt.setText(item.pipeNo)
                            }
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
}