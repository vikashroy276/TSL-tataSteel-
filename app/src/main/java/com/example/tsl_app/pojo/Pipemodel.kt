package com.example.tsl_app.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Pipemodel(
    @SerializedName("pipeid") @Expose var pipeid: Int? = null,

    @SerializedName("pipeNo") @Expose var pipeNo: String? = null,

    @SerializedName("aslno") @Expose var aslno: String? = null


) {
    override fun toString(): String {
        return "$pipeNo (ASL: $aslno)"
    }
}
