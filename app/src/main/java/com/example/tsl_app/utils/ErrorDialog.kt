package com.example.tsl_app.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.tsl_app.R

object DialogManager {
    private var errorDialog: Dialog? = null
    private var successDialog: Dialog? = null

    // Show Error Dialog
    fun showErrorDialog(activity: Activity, title: String) {
        if (errorDialog?.isShowing == true) {
            // If error dialog is already showing, avoid creating a new one
            return
        }

        errorDialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setContentView(R.layout.passwordvalidate_layout)

            val okayBtn = findViewById<CardView>(R.id.okay_btn)
            val errortxt = findViewById<TextView>(R.id.errorText)

            errortxt.text = title

            okayBtn.setOnClickListener {
                ProgressDialogUtils.hideLoader()  // Call to hide the loader if any
                dismissErrorDialog() // Dismiss the error dialog when the button is clicked
            }

            show()
        }
    }

    // Dismiss Error Dialog
    private fun dismissErrorDialog() {
        errorDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        errorDialog = null
    }

    // Show Success Dialog
    fun showSuccessDialog(context: Context, title: String, targetActivity: Class<*>) {
        if (successDialog?.isShowing == true) {
            return
        }

        successDialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setContentView(R.layout.showsuccess_layout)

            val okayBtn = findViewById<CardView>(R.id.okaybtn)
            val titleTxt = findViewById<TextView>(R.id.titletxt)

            titleTxt.text = title
            okayBtn.setOnClickListener {
                dismissSuccessDialog()
                val intent = Intent(context, targetActivity)
                context.startActivity(intent)
                (context as Activity).finish()
            }
            show()
        }
    }

    fun showSuccessNavigate(context: Context, title: String) {
        if (successDialog?.isShowing == true) {
            return
        }

        successDialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setContentView(R.layout.showsuccess_layout)

            val okayBtn = findViewById<CardView>(R.id.okaybtn)
            val titleTxt = findViewById<TextView>(R.id.titletxt)

            titleTxt.text = title

            okayBtn.setOnClickListener {
                dismissSuccessDialog()
            }

            show()
        }
    }


    // Dismiss Success Dialog
    fun dismissSuccessDialog() {
        successDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        successDialog = null
    }

    // Dismiss both dialogs if necessary
    fun dismissAllDialogs() {
        dismissErrorDialog()
        dismissSuccessDialog()
    }

    // Get Device ID
    fun getDeviceId(context: Context): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // Hide Keyboard
    fun hideKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = (context as? Activity)?.currentFocus
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    // Check Internet Connectivity
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
