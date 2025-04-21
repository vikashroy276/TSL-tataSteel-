package com.example.tsl_app.utils

import android.content.Context
import android.content.SharedPreferences

object CacheUtils {
    private const val PREFS_NAME = "SETTING_VALUE"
    private const val KEY_USER_ID = "USER_ID"
    private const val KEY_BASE_URL = "KEY_BASE_URL"
    const val KEY_TOKEN = "TOKEN"

    fun saveString(context: Context, key: String, data: String?) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(key, data)
        editor.apply()
    }

    fun getString(context: Context, key: String): String? {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getString(key, null)
    }

    fun saveUserId(context: Context, userId: String?) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getString(KEY_USER_ID, null)
    }
    fun saveEmployeeId(context: Context, empId: Int?) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        if (empId != null) {
            editor.putInt("KEY_Employee_ID", empId)
        }
        editor.apply()
    }

    fun getEmployeeId(context: Context): Int? {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getInt("KEY_Employee_ID", 0)
    }

    fun saveBASEURL(context: Context,url: String?) {
        val settings: SharedPreferences = context.getSharedPreferences(KEY_BASE_URL, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString(KEY_BASE_URL, url)
        editor.apply()
    }

    fun getBASEURL(context: Context): String? {
        val settings: SharedPreferences = context.getSharedPreferences(KEY_BASE_URL, Context.MODE_PRIVATE)
        return settings.getString(KEY_BASE_URL, "")
    }

    fun saveShiftId(context: Context, shiftId: Int?) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        if (shiftId != null) {
            editor.putInt("KEY_shiftId", shiftId)
        }
        editor.apply()
    }

    fun getShiftId(context: Context): Int? {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getInt("KEY_shiftId", 0)
    }
    fun savePipeId(context: Context, pipeId: Int?) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        if (pipeId != null) {
            editor.putInt("KEY_pipeId", pipeId)
        }
        editor.apply()
    }

    fun getPipeId(context: Context): Int? {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getInt("KEY_pipeId", 0)
    }

    fun saveReaderPower(context: Context, power: Int) {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putInt("Readerpower", power)
        editor.apply()
    }

    fun getReaderPower(context: Context): Int {
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return settings.getInt("Readerpower", 0)
    }
}

