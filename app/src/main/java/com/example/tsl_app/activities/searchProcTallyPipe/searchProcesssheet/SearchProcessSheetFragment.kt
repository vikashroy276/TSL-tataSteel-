package com.example.tsl_app.activities.searchProcTallyPipe.searchProcesssheet

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tsl_app.adapter.PipeNumberAdapter
import com.example.tsl_app.pojo.Pipemodel
import com.example.tsl_app.pojo.response.AndroidGetPipeRemarksResponse
import com.example.tsl_app.pojo.response.AndroidGetStationNameResponse
import com.example.tsl_app.pojo.request.PipeRemarksRequest
import com.example.tsl_app.pojo.request.PipesByPsNoTsNoRequest
import com.example.tsl_app.activities.selectRFIDandSearch.TagPipeRFIDActivity
import com.example.tsl_app.adapter.RemarksAdapter
import com.example.tsl_app.adapter.StationAdapter
import com.example.tsl_app.R
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import com.example.tsl_app.databinding.FragmentSearchProcessSheetBinding
import com.example.tsl_app.pojo.request.StationNameRequest
import com.example.tsl_app.pojo.response.RfidMapPipeIdResponse
import com.example.tsl_app.pojo.response.SearchReportByPsTsNResponse
import com.example.tsl_app.restapi.ApiClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchProcessSheetFragment : Fragment() {
    lateinit var binding: FragmentSearchProcessSheetBinding
    private var remarksList: List<AndroidGetPipeRemarksResponse>? = null
    private var remarksAdapter: RemarksAdapter? = null
    private var stationList: List<AndroidGetStationNameResponse>? = null
    private var stationAdapter: StationAdapter? = null
    private lateinit var processSheetId: String
    private lateinit var pipeNumber: String
    val pipeNumberList: ArrayList<Pipemodel> = ArrayList()
    private lateinit var pipeNumberAdapter: PipeNumberAdapter
    private lateinit var stationResponse: MutableList<AndroidGetStationNameResponse>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchProcessSheetBinding.inflate(inflater, container, false)

        stationResponse = mutableListOf()
        setupListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getIntentFieldSetText()
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
            val isVisible = binding.reamrksCard.visibility == View.GONE
            binding.reamrksCard.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.remarkLayout.visibility = View.GONE
        }
        binding.ShowBtn.setOnClickListener {
            getAndroidGetPipeRemarksApi(pipeNumber)
            val isVisible = binding.remarkLayout.visibility == View.GONE
            binding.remarkLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.reamrksCard.visibility = View.GONE
        }
    }


    private fun getIntentFieldSetText() {
        if (!NetworkUtils.isNetworkAvailable(requireActivity())) {
            NoInternetConnectionDialog.showDialog(
                requireActivity(), "Please check your internet connection and try again."
            )
        }
        val clientName = arguments?.getString("clientName")
        val projectName = arguments?.getString("projectName")
        val pipeSize = arguments?.getString("pipeSize")
        val processSheetCode = arguments?.getString("procsheetCode")
        val processSheetSeqNo = arguments?.getString("procsheetSeqNo")
        val processSheetYear = arguments?.getString("procsheetYear")
        pipeNumber = arguments?.getString("pipeNumber").toString()
        processSheetId = arguments?.getString("procsheetid").toString()
        getNoOfPipesByPsNoTsNoAPI(processSheetSeqNo.toString())
        binding.clientNameTxt.text = clientName
        binding.pipeSizeTxt.text = pipeSize
        binding.projectNameTxt.text = projectName
        binding.processSheetNoTxt.text = processSheetCode
        binding.yearTxt.text = processSheetYear.toString()
        binding.seqNo.text = processSheetSeqNo.toString()
        binding.pipeNumberTxt.setText(pipeNumber)
        binding.pipeNumberRecyclerView.visibility = View.GONE

        binding.pipeNumberRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        pipeNumberAdapter = PipeNumberAdapter(pipeNumberList) { selectedPipeNumber ->
            binding.pipeNumberTxt.setText(selectedPipeNumber.pipeNo)
            getStationNameWithPipeNo(selectedPipeNumber.pipeNo.toString())
            CacheUtils.savePipeId(requireActivity(), selectedPipeNumber.pipeid)
            binding.pipeNumberRecyclerView.visibility = View.GONE
            DialogManager.hideKeyboard(requireActivity())
        }
        binding.pipeNumberRecyclerView.adapter = pipeNumberAdapter

        binding.pipeNumberTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isEmpty() == true || pipeNumberList.isEmpty()) {
                    binding.pipeNumberRecyclerView.visibility = View.GONE
                } else {
                    binding.pipeNumberRecyclerView.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > 3) {
                    pipeNumberAdapter.filter.filter(s)
                    scrollToView(binding.pipeNumberTxt)
                }
            }
        })
        getStationNameWithPipeNo(binding.pipeNumberTxt.text.toString())
        remarksList = ArrayList()
        stationList = ArrayList()
        println("Station List :$stationList")

        binding.remarkRecycler.layoutManager = LinearLayoutManager(requireActivity())
        binding.stationRecycler.layoutManager = LinearLayoutManager(requireActivity())

    }

    private fun getAndroidInsertPipeRemarksApi(remarksEdt: String) {
        val pipeRemarksRequest = PipeRemarksRequest()
        pipeRemarksRequest.deviceid = DialogManager.getDeviceId(requireActivity())
        pipeRemarksRequest.psid = processSheetId
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

    private fun scrollToView(view: View) {
        val scrollView = requireActivity().findViewById<ScrollView>(R.id.rootLayout)
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        scrollView.smoothScrollTo(0, rect.top)
    }

    private fun getStationNameWithPipeNo(pipeNo: String) {
        val stationNameRequest = StationNameRequest()
        stationNameRequest.seqno = 0
        stationNameRequest.year = 0
        stationNameRequest.psCode = pipeNo

        ApiClient.getClient(requireActivity())
            ?.stationNameWithPipeNo(stationNameRequest)
            ?.enqueue(object : Callback<List<AndroidGetStationNameResponse>> {
                @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<List<AndroidGetStationNameResponse>>,
                    response: Response<List<AndroidGetStationNameResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        stationResponse = response.body()?.toMutableList() ?: mutableListOf()
                        if (stationResponse.isNotEmpty()) {
                            stationAdapter = StationAdapter(stationResponse)
                            binding.stationRecycler.setAdapter(stationAdapter)
                            for (item in stationResponse) {
                                binding.taggedByName.text = "-" + item.taggedBy
                                binding.taggedOnDate.text = "-" + item.taggedOn
                                binding.currentStationNameTxt.text = item.currentstationname
                                binding.currentTestDateTxt.text = item.currenttestdate

                            }
                        } else {
                            binding.taggedByName.text = ""
                            binding.taggedOnDate.text = ""
                            binding.currentStationNameTxt.text = ""
                            binding.currentTestDateTxt.text = ""
                            stationResponse.clear()
                            stationAdapter?.notifyDataSetChanged()
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


    private fun getAndroidGetPipeRemarksApi(pipeNo: String?) {
        val pipeRemarksRequest = PipeRemarksRequest()
        pipeRemarksRequest.PipeNo = pipeNo

        ApiClient.getClient(requireActivity())
            ?.getandroidGetPipeRemarksApi(pipeRemarksRequest)
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
                            binding.remarkRecycler.setAdapter(remarksAdapter)
                        } else {
                            binding.remarkLayout.visibility = View.GONE
                            binding.reamrksCard.visibility = View.GONE
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


    private fun getNoOfPipesByPsNoTsNoAPI(processEq: String?) {
        val getNoPipeByRequest = PipesByPsNoTsNoRequest()
        getNoPipeByRequest.proc_type = 2
        getNoPipeByRequest.id = processEq

        ApiClient.getClient(requireActivity())
            ?.getnoofpipesbypsnotsnoAPI(getNoPipeByRequest)
            ?.enqueue(object : Callback<List<SearchReportByPsTsNResponse>> {
                override fun onResponse(
                    call: Call<List<SearchReportByPsTsNResponse>>,
                    response: Response<List<SearchReportByPsTsNResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val pipeNumberResponse: List<SearchReportByPsTsNResponse> =
                            response.body()!!
                        if (pipeNumberResponse.isEmpty()) {
                            DialogManager.showErrorDialog(
                                requireActivity(), "Pipe Number List Empty!!"
                            )
                        } else {
                            pipeNumberList.clear()
                            for (item in pipeNumberResponse) {
                                pipeNumberList.add(Pipemodel(item.pipeid, item.pipeNumber))

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