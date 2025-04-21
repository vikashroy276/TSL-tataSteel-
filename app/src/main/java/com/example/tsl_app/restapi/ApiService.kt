package com.example.tsl_app.restapi


import com.example.tsl_app.pojo.request.AuthLoginRequest
import com.example.tsl_app.pojo.request.InsertManualRequest
import com.example.tsl_app.pojo.request.MappedPipeRequest
import com.example.tsl_app.pojo.response.AuthLoginResponse
import com.example.tsl_app.pojo.response.SearchReportByPsTsNResponse
import com.example.tsl_app.pojo.request.SearchReportByPSNoRequest
import com.example.tsl_app.pojo.request.SearchReportByTSPipeRequest
import com.example.tsl_app.pojo.response.AndroidGetShiftIdResponse
import com.example.tsl_app.pojo.request.ShiftIdRequest
import com.example.tsl_app.pojo.response.GetRFIDDetailsResponse
import com.example.tsl_app.pojo.request.RFIDDetailsRequest
import com.example.tsl_app.pojo.response.PipeByPsNoResponse
import com.example.tsl_app.pojo.request.PipeByPsNoRequest
import com.example.tsl_app.pojo.response.GetProcSheetNoResponse
import com.example.tsl_app.pojo.request.ProcSheetNoRequest
import com.example.tsl_app.pojo.response.ProcSheetYearResponse
import com.example.tsl_app.pojo.response.RfidMapPipeIdResponse
import com.example.tsl_app.pojo.request.RfidMapPipeIdRequest
import com.example.tsl_app.pojo.response.GetPipeDataByTagIdResponse
import com.example.tsl_app.pojo.request.PipeDataByTagIdRequest
import com.example.tsl_app.pojo.response.AndroidGetPipeRemarksResponse
import com.example.tsl_app.pojo.response.AndroidGetStationNameResponse
import com.example.tsl_app.pojo.request.StationNameRequest
import com.example.tsl_app.pojo.request.PipeRemarksRequest
import com.example.tsl_app.pojo.request.PipesByPsNoTsNoRequest
import com.example.tsl_app.pojo.request.SaveUntagPipeRequest
import com.example.tsl_app.pojo.response.MappedPipeResponse
import com.example.tsl_app.pojo.response.SaveUntagPipeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {

    @POST("DeviceUserAuthLogin")
    fun getAuthLogin(@Body authloginrequest: AuthLoginRequest): Call<AuthLoginResponse>

    @POST("GetRFIDDetails")
    fun getRFIDDetails(@Body getrfidetailsrequest: RFIDDetailsRequest?): Call<List<GetRFIDDetailsResponse>>

    @GET("getprocsheetyear")
    fun getProcSheetYear(): Call<List<ProcSheetYearResponse>>

    @POST("GetProcsheetNo")
    fun getProcSheetNoRequest(@Body procsheetNorequest: ProcSheetNoRequest): Call<GetProcSheetNoResponse>

    @POST("Getnoofpipesbypsno")
    fun pipeNoByPsNoRequest(@Body getProcsheetNorequest: PipeByPsNoRequest): Call<List<PipeByPsNoResponse>>

    @POST("rfidmappipeid")
    fun rfidMapPipeIdApi(@Body getProcsheetNorequest: RfidMapPipeIdRequest): Call<RfidMapPipeIdResponse>

    @POST("AndroidGetSearchReportByPSNO")
    fun searchReportByPSNOApi(@Body androidgetsearchbypsno: SearchReportByPSNoRequest): Call<List<SearchReportByPsTsNResponse>>

    @POST("AndroidGetSearchReportByTSPipe")
    fun searchReportByTSPipeRequestApi(@Body androidgetsearchbytspipe: SearchReportByTSPipeRequest): Call<List<SearchReportByPsTsNResponse>>

    @POST("AndroidGetStationNameWithPipeNo")
    fun stationNameWithPipeNo(@Body androidgetstationnamerequest: StationNameRequest): Call<List<AndroidGetStationNameResponse>>

    @POST("AndroidGetPipeRemarks")
    fun getandroidGetPipeRemarksApi(@Body androidgetstationnamerequest: PipeRemarksRequest): Call<List<AndroidGetPipeRemarksResponse>>

    @POST("AndroidInsertPipeRemarks")
    fun getandroidInsertPipeRemarksApi(@Body androidgetstationnamerequest: PipeRemarksRequest): Call<RfidMapPipeIdResponse>

    @POST("AndroidGetshiftid")
    fun getshiftidApi(@Body getshiftidrequest: ShiftIdRequest): Call<List<AndroidGetShiftIdResponse>>

    @POST("Getnoofpipesbypsnotsno")
    fun getnoofpipesbypsnotsnoAPI(@Body getnoofpipesbypsnotso: PipesByPsNoTsNoRequest): Call<List<SearchReportByPsTsNResponse>>

    @POST("GetPipedatabytagid")
    fun getPipedatabytagidAPI(@Body getpipedatabytagidrequest: PipeDataByTagIdRequest): Call<List<GetPipeDataByTagIdResponse>>

    @POST("GetTagMappedDetails")
    fun getTagMappedDetailsAPI(@Body getMappedPipeRequest: MappedPipeRequest): Call<List<MappedPipeResponse>>

    @POST("Saveuntagpipe")
    fun saveUntagPipeAPI(@Body request: SaveUntagPipeRequest): Call<SaveUntagPipeResponse>

    @POST("InsertTagInManual")
    fun insertTagInManualAPI(@Body request: InsertManualRequest): Call<SaveUntagPipeResponse>

}
