package com.example.tsl_app.activities.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tsl_app.BuildConfig
import com.example.tsl_app.activities.settings.SettingActivity
import com.example.tsl_app.activities.selectRFIDandSearch.TagPipeRFIDActivity
import com.example.tsl_app.databinding.ActivityLoginBinding
import com.example.tsl_app.pojo.request.AuthLoginRequest
import com.example.tsl_app.restapi.ApiClient
import com.example.tsl_app.rfid.BaseActivity.BLUETOOTH_PERMISSION_REQUEST_CODE
import com.example.tsl_app.rfid.BaseActivity.LOCATION_PERMISSION_REQUEST_CODE
import com.example.tsl_app.utils.CacheUtils
import com.example.tsl_app.utils.DialogManager
import com.example.tsl_app.utils.NetworkUtils
import com.example.tsl_app.utils.NoInternetConnectionDialog
import com.example.tsl_app.utils.ProgressDialogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.awaitResponse

/** Login page is created to allow users to securely authenticate themselves before gaining access to the app's features
 *It helps ensure that only authorized users can interact with sensitive data or perform specific actions in the app.
 * The main reasons for creating a login page include:
 * User Authentication
 * User Personalization
 * Security
 * Access Control
 * Improved User Experience
 */

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var deviceId: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUiAndListeners()
        askForPermission()
        deviceId = binding.deviceId
        binding.versionClick.vClick.text =
            BuildConfig.VERSION_NAME  //For show the Application version

        val deviceIdValue = getDeviceIdd()
        deviceId.text = "Device ID:$deviceIdValue"  //For show the Device Id On the Screen

    }

    /** Check if the device is running Android 12 (API level 31) or higher
     *  For Android 12 and above, Bluetooth permissions are more granular
     *  BLUETOOTH_CONNECT is required to connect to Bluetooth devices
     *  BLUETOOTH_SCAN is required to scan for nearby Bluetooth devices
     *  For Android 11 and lower, only ACCESS_FINE_LOCATION permission is requiredIf either of these permissions is not granted, request them. */

    private fun askForPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val permissions = arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN
                )
                if (!EasyPermissions.hasPermissions(this, *permissions)) {
                    EasyPermissions.requestPermissions(
                        this,
                        "Bluetooth permissions are required",
                        BLUETOOTH_PERMISSION_REQUEST_CODE,
                        *permissions
                    )
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (!EasyPermissions.hasPermissions(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    EasyPermissions.requestPermissions(
                        this,
                        "Location permission is required for Bluetooth scanning",
                        LOCATION_PERMISSION_REQUEST_CODE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }

            else -> {
                val permissions = arrayOf(
                    Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (!EasyPermissions.hasPermissions(this, *permissions)) {
                    EasyPermissions.requestPermissions(
                        this,
                        "Bluetooth and location permissions are required",
                        LOCATION_PERMISSION_REQUEST_CODE,
                        *permissions
                    )
                }
            }
        }
    }

    /** First, check for internet connectivity when the UI is initialized
     *  Set up a click listener for the logo (usually this might take the user to a settings screen)
     *  When the logo is clicked, navigate to the SettingsActivity,
     *  Set up a click listener for the login button
     */
    private fun initUiAndListeners() {
        checkInternetConnectivity()

        binding.lvLogo.tLogo.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        binding.loginBtn.setOnClickListener {
            when {
                binding.userName.text.toString().isEmpty() || binding.password.text.toString()
                    .isEmpty() -> {
                    DialogManager.showErrorDialog(this, "Please enter both username and password!")
                }

                !NetworkUtils.isNetworkAvailable(this) -> {
                    DialogManager.showErrorDialog(this, "Please check your internet connection!")
                }

                else -> {
                    getLoginAPI(binding.userName.text.toString(), binding.password.text.toString())
                }
            }
        }
    }

    /** First, check for internet connectivity when the UI is initialized */
    private fun checkInternetConnectivity() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            NoInternetConnectionDialog.showDialog(
                this@LoginActivity, "Please check your internet connection and try again."
            )
        }
    }

    // This function is created to get the device ID and display it on the screen.
    @SuppressLint("HardwareIds")
    private fun getDeviceIdd(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    /** This is the Login API where we send the username and password to the server.
     *  The server validates the credentials and responds with a value,
     *  such as an authentication token or user details   */
    private fun getLoginAPI(username: String, password: String) {
        val authLoginRequest = AuthLoginRequest().apply {
            loginUserName = username
            loginPassword = password
        }
        // Launch coroutine on IO dispatcher for network operations
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ProgressDialogUtils.showLoader(this@LoginActivity) // Show progress dialog on the main thread
                val response =
                    ApiClient.getClient(this@LoginActivity)?.getAuthLogin(authLoginRequest)
                        ?.awaitResponse()

                // Switch back to the main thread for UI-related operations
                withContext(Dispatchers.Main) {
                    if (response != null && response.isSuccessful && response.body() != null) {
                        ProgressDialogUtils.hideLoader()
                        val loginResponse = response.body()!!

                        if (!loginResponse.response!!) {
                            DialogManager.showErrorDialog(
                                this@LoginActivity,
                                (loginResponse.responseMessage ?: loginResponse.response).toString()
                            )
                        } else {
                            if (loginResponse.responseMessage == "1") {
                                Log.d("AuthToken", "Token received: ${loginResponse.token}")

                                CacheUtils.saveUserId(this@LoginActivity, loginResponse.userid)
                                CacheUtils.saveEmployeeId(this@LoginActivity, loginResponse.emp_id)
                                CacheUtils.saveString(
                                    this@LoginActivity, "AUTH_TOKEN", loginResponse.token
                                )
                                DialogManager.showSuccessDialog(
                                    this@LoginActivity,
                                    "Login Successfully!",
                                    TagPipeRFIDActivity::class.java
                                )
                                ApiClient.refreshRetrofit()
                            } else {
                                DialogManager.showErrorDialog(
                                    this@LoginActivity,
                                    "Internal Server Error Please contact to Admin"
                                )
                            }
                        }
                    } else {
                        ProgressDialogUtils.hideLoader()
                        DialogManager.showErrorDialog(
                            this@LoginActivity, "Login failed: ${response?.errorBody()?.string()}"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ProgressDialogUtils.hideLoader()
                    DialogManager.showErrorDialog(this@LoginActivity, "Server Error: ${e.message}")
                }
            }
        }
    }
}