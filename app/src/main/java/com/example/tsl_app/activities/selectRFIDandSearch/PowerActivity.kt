package com.example.tsl_app.activities.selectRFIDandSearch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.databinding.ActivityPowerBinding


class PowerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPowerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPowerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.backBtn.setOnClickListener {
            val intent = Intent(this, TagPipeRFIDActivity::class.java)
            startActivity(intent)
        }
        binding.SaveBtn.setOnClickListener {
            val power: String = binding.power.text.toString()
            if (power.isNotEmpty()) {
                CacheUtils.saveReaderPower(this, power.toInt())
                Toast.makeText(this, "Saved Successfully !!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.power.error = "Power cannot be empty."
            }
        }
    }

}
