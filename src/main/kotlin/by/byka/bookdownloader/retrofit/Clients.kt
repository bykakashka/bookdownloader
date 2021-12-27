package by.byka.bookdownloader.retrofit

import by.byka.bookdownloader.Constants
import by.byka.bookdownloader.converter.data.InitRequestDto
import by.byka.bookdownloader.converter.data.InitResponseDto
import by.byka.bookdownloader.converter.data.SendFileResponseDto
import by.byka.bookdownloader.converter.data.StatusResponseDto
import by.byka.bookdownloader.service.DownloadService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.io.InputStream
import java.io.OutputStream
import javax.ws.rs.PathParam

interface SendFileClient {
    @Multipart
    @POST("upload-file/{id}")
    fun upload(
        @Header("X-Oc-Api-Key") apiHeader: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Call<SendFileResponseDto>
}

interface StatusResponseClient {
    @GET("/jobs/{job_id}")
    fun getStatus(
        @Header("X-Oc-Api-Key") apiHeader: String,
        @Path("job_id") jobId: String
    ): Call<StatusResponseDto>

    @POST("/jobs")
    fun initUpload(
        @Header("X-Oc-Api-Key") apiHeader: String, @Body dto: InitRequestDto
    ): Call<InitResponseDto>
}

interface TgApiClient {
    @GET("{token}/getFile")
    fun getInfo(@Path("token", encoded = true) token: String, @Query("file_id") fileId: String): Call<DownloadService.TgFileInfo>

    @GET("file/{token}/{file_path}")
    fun getFileLink(@Path("token", encoded = true) token: String, @Path("file_path") filePath: String): Call<ResponseBody>
}