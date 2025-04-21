package com.example.tsl_app.activities.selectRFIDandSearch

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.tsl_app.BuildConfig
import com.example.tsl_app.R
import com.example.tsl_app.activities.SearchTabScreen
import com.example.tsl_app.activities.mappedpipe.MapPipeActivity
import com.example.tsl_app.activities.rfidmap.RFIDProcessInfoActivity
import com.example.tsl_app.activities.searchPipe.SearchPipeWithRFIDActivity
import com.example.tsl_app.databinding.ActivityTagpipefidBinding
import com.example.tsl_app.restapi.ApiClient
import com.example.tsl_app.rfid.BaseActivity
import com.google.android.material.card.MaterialCardView

class TagPipeRFIDActivity : BaseActivity() {
    lateinit var binding: ActivityTagpipefidBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagpipefidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ApiClient.getClient(this)
        setUpListeners()
        connectRfid()
        binding.versionClick.vClick.text = BuildConfig.VERSION_NAME
        println("version: ${BuildConfig.VERSION_NAME}")
    }

    private fun setUpListeners() {
        binding.scanRfid.setOnClickListener {
            resetCardColors()
            binding.scanRfid.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blueColor))
            binding.pipe.setTextColor(ContextCompat.getColor(this, R.color.black))
            binding.rfid.setTextColor(ContextCompat.getColor(this, R.color.black))
            val intent = Intent(this, RFIDProcessInfoActivity::class.java)
            startActivity(intent)
        }

        binding.searchRfid.setOnClickListener {
            resetCardColors()
            binding.searchRfid.setCardBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.blueColor
                )
            )
            val intent = Intent(this, SearchTabScreen::class.java)
            startActivity(intent)
        }

        binding.mapPipe.setOnClickListener {
            resetCardColors()
            binding.mapPipe.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blueColor))
            val i = Intent(this, MapPipeActivity::class.java)
            startActivity(i)
        }

        binding.searchpipe.setOnClickListener {
            resetCardColors()
            binding.searchpipe.setCardBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.blueColor
                )
            )
            val intent = Intent(this, SearchPipeWithRFIDActivity::class.java)
            startActivity(intent)
        }
        binding.lvLogo.tLogo.setOnClickListener {
            showPasswordDialog()
        }

        binding.connectBtn.setOnClickListener {
            connectRfid()
        }
    }

    private fun resetCardColors() {
        binding.scanRfid.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.searchRfid.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.mapPipe.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.searchpipe.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun showPasswordDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.powerpasswordlayout)

        val input = dialog.findViewById<EditText>(R.id.errortxt)
        val btnOk = dialog.findViewById<MaterialCardView>(R.id.okay_btn)
        val cancelBtn = dialog.findViewById<MaterialCardView>(R.id.cancel_btn)

        btnOk.setOnClickListener {
            if (input.text.isNotEmpty()) {
                val enteredPassword = input.text.toString()
                if (enteredPassword == "123") {
                    val i = Intent(this, PowerActivity::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } else {
                input.error = "Password cannot be empty"
            }
        }
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun handleSetText(msg: String?) {
        // Check if we are on the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            binding.rfidStatusTv.text = msg
        } else {
            // If not on the main thread, post the update to the main thread
            Handler(Looper.getMainLooper()).post {
                binding.rfidStatusTv.text = msg
            }
        }
    }

    override fun onScannedData(data: String?) {

    }

    override fun showStatus(): Boolean {
        return super.showStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rfidHandler != null && rfidHandler.isReaderConnected) {
            disconnectRfid()
        }
        Log.e("Disconnect", "RFID Reader disconnected.")
    }
}