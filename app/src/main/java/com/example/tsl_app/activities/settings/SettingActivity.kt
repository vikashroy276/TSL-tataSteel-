package com.example.tsl_app.activities.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tsl_app.activities.login.LoginActivity
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.databinding.ActivitySettingBinding
import com.example.tsl_app.restapi.ApiClient

/**
 * the purpose of the SettingActivity. This class allows users to update the
 * API base URL and saves it using shared preferences (CacheUtils).
 * Sets the EditText with the saved base URL or a default value (http://).
 * Validates the URL, saves it, and refreshes the API client with the new URL.
 * */

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
        setUpListener()
    }

    /**
     * Sets up click listeners for the back button and save button.
     */
    private fun setUpListener() {
        binding.backBtn.setOnClickListener {
            val intent = Intent(this@SettingActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.SaveBtn.setOnClickListener {
            val url: String = binding.url.text.toString()
            if (url.isNotEmpty()) {
                CacheUtils.saveBASEURL(this,url)
                ApiClient.refreshRetrofit()
                Toast.makeText(this,"Saved Successfully !!",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SettingActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

/**
 * Initializes the UI components by setting the EditText with the saved URL.
 * If no URL is saved, it defaults to "http://".
 **/
    @SuppressLint("SetTextI18n")
    private fun initUi() {
        if (CacheUtils.getBASEURL(this).equals("")) {
            binding.url.setText("http://")
        } else {
            binding.url.setText(CacheUtils.getBASEURL(this))
        }
    }
}

