package com.example.tsl_app.pojo.response

data class MappedPipeResponse(
    val pm_pipe_id: String,
    val pm_pipe_code: String,
    var TagNo: String,
    var I: Int,
    var T: Int,
    var EF: Int,
    val I_date: String?,
    val T_date: String?,
    val EF_Date: String?,
    val T_ismannual : Int,
    val EF_ismannual : Int,
    val I_ismannual : Int,
    val pm_IsNtc : Int,
    val pm_procsheet_id : String?,
    val pending_status: Int?
) {
    override fun toString(): String {
        return "MappedPipeResponse(pm_pipe_id='$pm_pipe_id', pipe number='$pm_pipe_code')"
    }
}
