package com.example.tsl_app.activities.searchGraph

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.tsl_app.R
import com.example.tsl_app.utils.RangeGraph
import com.example.tsl_app.databinding.ActivityLocatorBinding
import com.example.tsl_app.rfid.BaseActivity
import com.example.tsl_app.rfid.RFIDHandler
import com.example.tsl_app.rfid.RFIDHandler.ResponseHandlerInterface
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.TagData

class LocatorActivity : BaseActivity(), ResponseHandlerInterface {
    private lateinit var binding: ActivityLocatorBinding
    private var ctx: Context? = null
    private var tagEPC: String? = null
    private var locationBar: RangeGraph? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ctx = this

        tagEPC = intent.getStringExtra("epc")
        println("epc Id : $tagEPC")

        locationBar = binding.locationBar
        epc = tagEPC
        binding.tagEPC.text = tagEPC

        binding.lvLogo.tLogo.setOnClickListener {
            connectRfid()
        }

    }


    override fun onResume() {
        super.onResume()
        rfidHandler = RFIDHandler.getInstance(30, this) // 30-second timeout
        rfidHandler.setPower(rfidHandler.initialPower * 10)
        if (rfidHandler != null) {
            rfidHandler.setHandler(this)
        } else {
            Log.e(TAG, "RFIDHandler instance is null!")
            handleSetText("RFID Initialization Failed")
        }
    }

    override fun handleTagData(tagData: Array<TagData>) {
        for (tag in tagData) {
            val percent = tag.LocationInfo.relativeDistance.toInt()
            runOnUiThread {
                locationBar?.value = percent
                locationBar?.invalidate()
            }
        }
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            rfidHandler?.locate(epc) //reads and shows inventory
        } else {
            rfidHandler?.stopInventory() //on release stops showing any new inventory
        }
    }

    override fun handleSetText(msg: String) {

    }

    override fun showStatus(): Boolean {
        return false
    }

    override fun onScannedData(data: String?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rfidHandler != null) {
            try {
                rfidHandler.stopInventory()
                rfidHandler.stopLocate()
                rfidHandler.setHandler(null)
                //                rfidHandler.releaseResources();
            } catch (e: OperationFailureException) {
                Log.e(TAG, "Error during RFIDHandler cleanup: " + e.message, e)
            } catch (e: InvalidUsageException) {
                Log.e(TAG, "Error during RFIDHandler cleanup: " + e.message, e)
            }
        }
    }

    companion object {
        const val TAG: String = "LocatorActivity"
        var epc: String? = null
        var name: String? = null
    }
}
