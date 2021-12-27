package by.byka.bookdownloader.converter.service

import by.byka.bookdownloader.Constants.CONVERTER_TOKEN
import by.byka.bookdownloader.Constants.CONVERTER_URL
import by.byka.bookdownloader.converter.data.InitRequestDto
import by.byka.bookdownloader.converter.data.InitResponseDto
import by.byka.bookdownloader.converter.data.SendFileResponseDto
import by.byka.bookdownloader.jacksonMapper
import by.byka.bookdownloader.retrofit.SendFileClient
import by.byka.bookdownloader.retrofit.StatusResponseClient
import by.byka.bookdownloader.service.DownloadService
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import org.apache.logging.log4j.LogManager
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File

class ConverterService(
    private val downloadService: DownloadService,
    private val httpClient: OkHttpClient
) {
    private val log = LogManager.getLogger(ConverterService::class.java)

    private val converterRetrofitClient = Retrofit.Builder()
        .baseUrl(CONVERTER_URL)
        .client(httpClient)
        .addConverterFactory(JacksonConverterFactory.create(jacksonMapper()))
        .build().create(StatusResponseClient::class.java)

    fun convert(filepath: String): String? {
        log.trace("Init conversion for file $filepath")

        val resp = converterRetrofitClient.initUpload(
            CONVERTER_TOKEN,
            InitRequestDto(listOf(InitRequestDto.TargetDto("mobi")))
        ).execute()
        resp.body()?.let {
            val uploadResp = sendFile(it, filepath)
            uploadResp.body()?.let {body ->
                return getConvertedUrl(body)
            }
            uploadResp.errorBody()?.let { body ->
                log.warn("Cannot get converted file url: ${body.string()}")
            }
        }
        log.warn("Null body for init upload request")
        return null
    }

    private fun getConvertedUrl(uploadResp: SendFileResponseDto): String? {
        val resp = converterRetrofitClient.getStatus(CONVERTER_TOKEN, uploadResp.id.job).execute()
        val statusResp = resp.body()
        return when (statusResp?.status?.code) {
            "completed" -> {
                log.info("Converted file downloaded")
                downloadService.download(statusResp.output[0].uri)
            }
            "processing" -> {
                Thread.sleep(10000)
                getConvertedUrl(uploadResp)
            }
            else -> {
                if (statusResp == null) {
                    log.warn("Exception in the job status request: ${resp.code()} - ${resp.errorBody()}")
                }
                null
            }
        }
    }

    private fun sendFile(
        initResp: InitResponseDto,
        filepath: String
    ): Response<SendFileResponseDto> {
        val sendFileRetrofit = Retrofit.Builder().baseUrl(initResp.server + "/").client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create(jacksonMapper())).build()
            .create(SendFileClient::class.java)
        val file = File(filepath)

        val filePart = MultipartBody.Part.createFormData("file", file.name, File(filepath).asRequestBody())
        return sendFileRetrofit.upload(CONVERTER_TOKEN, initResp.id, filePart).execute()
    }
}