package com.example.tsl_app.activities.searchProcTallyPipe.searchProcesssheet

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tsl_app.adapter.PipeNumberAdapter
import com.example.tsl_app.pojo.response.SearchReportByPsTsNResponse
import com.example.tsl_app.pojo.request.SearchReportByPSNoRequest
import com.example.tsl_app.pojo.response.GetProcSheetNoResponse
import com.example.tsl_app.pojo.request.ProcSheetNoRequest
import com.example.tsl_app.pojo.response.ProcSheetYearResponse
import com.example.tsl_app.pojo.Pipemodel
import com.example.tsl_app.pojo.request.PipesByPsNoTsNoRequest
import com.example.tsl_app.R
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import com.example.tsl_app.utils.ProgressDialogUtils
import com.example.tsl_app.databinding.FragmentSearchSheetBinding
import com.example.tsl_app.restapi.ApiClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchSheetFragment  : Fragment() {
    lateinit var binding: FragmentSearchSheetBinding
    val yearsList: ArrayList<String> = ArrayList()
    val pipenumberList: ArrayList<Pipemodel> = ArrayList()
    private lateinit var pipeNumberAdapter: PipeNumberAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchSheetBinding.inflate(inflater, container, false)
        setupListener()

      return binding.root
    }
    override fun onResume() {
        super.onResume()
        clearField()
    }


    private fun setupListener() {
        if (!NetworkUtils.isNetworkAvailable(requireActivity())) {
            NoInternetConnectionDialog.showDialog(
                requireActivity(),
                "Please check your internet connection and try again."
            )
        }

        getYearAPI()

        binding.yeartxt.setOnClickListener {
            showYearDialog()
        }
        if (!binding.seqNo.text.toString().equals("")){
            getnoofpipesbypsnotsnoAPI(binding.seqNo.text.toString())
        }
        binding.pipeNumberRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        pipeNumberAdapter = PipeNumberAdapter(pipenumberList) { selectedPipeNumber ->
            binding.pipenumberrtxt.setText(selectedPipeNumber.pipeNo)
            CacheUtils.savePipeId(requireActivity(), selectedPipeNumber.pipeid)
            binding.pipeNumberRecyclerView.visibility = View.GONE
            DialogManager.hideKeyboard(requireActivity())
        }
        binding.pipeNumberRecyclerView.adapter = pipeNumberAdapter
        binding.pipeNumberRecyclerView.visibility = View.VISIBLE
        binding.pipenumberrtxt.addTextChangedListener(object : TextWatcher {
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

        binding.NextBtn.isEnabled = binding.pipenumberrtxt.text.isNotEmpty()
        binding.pipenumberrtxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.NextBtn.isEnabled = s?.isNotEmpty() ?: false
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.seqNo.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.seqNo.isCursorVisible = true // Make the cursor visible again when touched
            }
            false // Let other touch events proceed as usual
        }
        binding.seqNo.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val inputText = binding.seqNo.text.toString().trim()
                if (inputText.isEmpty()) {
                    DialogManager.showErrorDialog(requireActivity(), "Please Enter Valid Seq. No!!")
                }else if (binding.yeartxt.text.toString() == ""){
                    DialogManager.showErrorDialog(requireActivity(),"Please Select Year!!".toString());
                } else {
                    binding.seqNo.isCursorVisible=false
                    if (binding.yeartxt.text.toString() != "Year"){
                        getProcsheetNorequestAPI(binding.yeartxt.text.toString(),inputText)
                    }
                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.seqNo.windowToken, 0)
                }
                true
            } else {
                false
            }
        }
     }

    private fun getnoofpipesbypsnotsnoAPI(procseq: String?) {
        val pipesByPsNoTsNoRequest = PipesByPsNoTsNoRequest()
        pipesByPsNoTsNoRequest.proc_type=2
        pipesByPsNoTsNoRequest.id=procseq

        ApiClient.getClient(requireActivity())?.getnoofpipesbypsnotsnoAPI(pipesByPsNoTsNoRequest)?.enqueue(object :
            Callback<List<SearchReportByPsTsNResponse>> {
            override fun onResponse(
                call: Call<List<SearchReportByPsTsNResponse>>,
                response: Response<List<SearchReportByPsTsNResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val pipenuberResponse: List<SearchReportByPsTsNResponse> = response.body()!!
                    if (pipenuberResponse.isEmpty()) {
                        DialogManager.showErrorDialog(requireActivity(),"Pipe Number List Empty!!");
                    } else {
                        pipenumberList.clear()
                        for (item in pipenuberResponse) {
                            pipenumberList.add(Pipemodel(item.pipeid,item.pipeNumber))                          //  binding.pipenumberrtxt.setText(item.pipeNo)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<SearchReportByPsTsNResponse>>, t: Throwable) {
                DialogManager.showErrorDialog(requireActivity(),t.message.toString());
            }
        })
    }


    private fun getYearAPI() {
        try {
            ApiClient.getClient(requireActivity())?.getProcSheetYear()?.enqueue(object : Callback<List<ProcSheetYearResponse>> {
                override fun onResponse(call: Call<List<ProcSheetYearResponse>>, response: Response<List<ProcSheetYearResponse>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val yearResponse: List<ProcSheetYearResponse> = response.body()!!
                        for (item in yearResponse) {
                            val pipenumber = item.year
                            yearsList.add(pipenumber.toString())
                        }
                    } else {
                        DialogManager.showErrorDialog(requireActivity(), " API Error: ${response.code()} ${response.message()}")
                    }
                }
                override fun onFailure(call: Call<List<ProcSheetYearResponse>>, t: Throwable) {
                    DialogManager.showErrorDialog(requireActivity(), t.message.toString())
                }
            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    private fun showYearDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Select Year")

        // Assuming yearsList is accessible here
        builder.setItems(yearsList.toTypedArray()) { _, which ->
            val selectedItem: String = yearsList[which]
            binding.yeartxt.text = selectedItem
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun getProcsheetNorequestAPI(yearstr: String, seqnostr: String) {
        val procsheetNorequest = ProcSheetNoRequest()
        procsheetNorequest.seqno=seqnostr.toString()
        procsheetNorequest.year=yearstr.toInt()

        ApiClient.getClient(requireActivity())?.getProcSheetNoRequest(procsheetNorequest)?.enqueue(object : Callback<GetProcSheetNoResponse>{
            override fun onResponse(
                call: Call<GetProcSheetNoResponse>,
                response: Response<GetProcSheetNoResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val shipmentResponse: GetProcSheetNoResponse? = response.body()
                    val procsheetno: String? = shipmentResponse?.procsheetno
                    binding.processSheetNoTxt.text=procsheetno
                    getandroidGetSearchReportByPSNOApi(shipmentResponse?.procsheetno,seqnostr.toInt(),yearstr.toInt())
                }
            }
            override fun onFailure(call: Call<GetProcSheetNoResponse>, t: Throwable) {
                DialogManager.showErrorDialog(requireActivity(),t.message.toString());
            }
        })
    }


     private fun getandroidGetSearchReportByPSNOApi(procsheetno: String?, seqnostr: Int?, yearstr: Int?) {
        val searchReportByPSNoRequest =
            SearchReportByPSNoRequest()
        searchReportByPSNoRequest.procsheetNo=procsheetno
        searchReportByPSNoRequest.procseq=seqnostr
        searchReportByPSNoRequest.procYear=yearstr

        ApiClient.getClient(requireActivity())?.searchReportByPSNOApi(searchReportByPSNoRequest)?.enqueue(object :
            Callback<List<SearchReportByPsTsNResponse>> {
            override fun onResponse(
                call: Call<List<SearchReportByPsTsNResponse>>,
                response: Response<List<SearchReportByPsTsNResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val rfddetailresponse: List<SearchReportByPsTsNResponse> = response.body()!!
                    if (rfddetailresponse.isEmpty()) {
                        clearField()
                        DialogManager.showErrorDialog(requireActivity(),"Please Input Valid Details !".toString());
                    }else{
                        getnoofpipesbypsnotsnoAPI(seqnostr.toString())
                        for (item in rfddetailresponse) {
                            binding.clientNameTxt.text=item.clientName
                            binding.projectNameTxt.text=item.projectName
                            binding.pipesizetxt.text=item.pipeSize
                            binding.pipenumberrtxt.setText(item.pipeNumber)
                            CacheUtils.savePipeId(requireActivity(),item.pipeid)
                            binding.NextBtn.setOnClickListener {

                                ProgressDialogUtils.showLoader(requireActivity())
                                ProgressDialogUtils.setMessage("Please Wait...")

                                // Simulating a delay before loading the new fragment
                                Handler().postDelayed({
                                    // Create a new instance of the fragment
                                    val searchProcessSheetFragment = SearchProcessSheetFragment()

                                    // Prepare arguments to pass to the new fragment
                                    val args = Bundle().apply {
                                        putString("clientName", item.clientName)
                                        putString("projectName", item.projectName)
                                        putString("pipeSize", item.pipeSize)
                                        putString("procsheetCode", item.procsheetCode)
                                        putString("procsheetSeqNo", binding.seqNo.text.toString())
                                        putString("procsheetYear", binding.yeartxt.text.toString())
                                        putString("pipeNumber", binding.pipenumberrtxt.text.toString())
                                        putString("taggedBy", item.taggedBy)
                                        putString("taggedOn", item.taggedOn)
                                        putString("procsheetid", item.procsheetid.toString())
                                    }
                                    searchProcessSheetFragment.arguments = args

                                    // Hide the progress loader once the fragment is ready
                                    ProgressDialogUtils.hideLoader()

                                    // Replace the current fragment with the new one
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.fragments_container, searchProcessSheetFragment) // Use the correct ID
                                        .addToBackStack(null) // Optional: add fragment to back stack to allow back navigation
                                        .commit()
                                }, 3000) // Simulate a 4-second delay
                            }



                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<SearchReportByPsTsNResponse>>, t: Throwable) {
                DialogManager.showErrorDialog(requireActivity(),t.message.toString());
            }
        })

    }

    /**
     * The clearField() method is responsible for resetting UI fields and clearing the list
    of RFID details when needed. It ensures that old or incorrect data is removed before
    new data is fetched.*/
    private fun clearField() {
        binding.seqNo.setText("")
        binding.yeartxt.text="Year"
        binding.processSheetNoTxt.text=""
        binding.clientNameTxt.text=""
        binding.projectNameTxt.text=""
        binding.pipesizetxt.text=""
        binding.pipenumberrtxt.setText("")

    }

}