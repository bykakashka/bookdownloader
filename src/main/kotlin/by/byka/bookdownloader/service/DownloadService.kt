package by.byka.bookdownloader.service

import by.byka.bookdownloader.Constants.BOT_TOKEN
import by.byka.bookdownloader.jacksonMapper
import by.byka.bookdownloader.retrofit.TgApiClient
import com.fasterxml.jackson.annotation.JsonProperty
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager
import org.springframework.util.StreamUtils
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

//const val PATH = "./tmp"
const val HOME_FOLDER = "E:\\img"

class DownloadService(httpClient: OkHttpClient) {

    private val log = LogManager.getLogger(DownloadService::class.java)

    private val tgRetrofitClient = Retrofit.Builder()
        .baseUrl("https://api.telegram.org/")
        .client(httpClient)
        .addConverterFactory(JacksonConverterFactory.create(jacksonMapper()))
        .build().create(TgApiClient::class.java)


    fun download(link: String): String {

        val conn = URL(link).openConnection()
        val contentHeader = conn.getHeaderField("content-disposition")
        val filename = contentHeader?.substringAfter("filename=", ";")?.replace("\"", "")
            ?: "TODO get from link"

        log.trace("Init file downloading: $filename")

        if (conn is HttpURLConnection) {
            conn.getInputStream().use { input ->
                FileOutputStream(File(HOME_FOLDER + File.separator + filename)).use { output ->
                    input.copyTo(output)
                }
            }
        }

        log.trace("File $filename downloaded")
        return HOME_FOLDER +  File.separator + filename
    }

    fun downloadForTg(fileId: String, folder: String, fileName: String): String? {
        log.trace("Init file download for tg. filename = $fileName")

        val fileInfo = tgRetrofitClient.getInfo(URLEncoder.encode("bot$BOT_TOKEN"), fileId).execute()

        log.info("Got info response")

        return fileInfo.body()?.let {
            val fileLinkResp = tgRetrofitClient.getFileLink(URLEncoder.encode("bot$BOT_TOKEN"), it.result.filePath).execute()

            log.info("File $fileName for tg downloaded")

            fileLinkResp.body()?.let { fileContent ->
                val fileDir = File(HOME_FOLDER + File.separator + folder)
                if (!fileDir.exists()) {
                    fileDir.mkdir()
                }

                val file = File(HOME_FOLDER + File.separator + folder + File.separator + fileName)

                StreamUtils.copy(fileContent.byteStream(), FileOutputStream(file))
                log.trace("File $fileName for tg saved")
                file.absolutePath
            }
        }
    }

    data class TgFileInfo(
        @JsonProperty("ok")
        val ok: String,
        @JsonProperty("result")
        val result: Result
    ) {
        data class Result(
            @JsonProperty("file_id")
            val fileId: String,
            @JsonProperty("file_path")
            val filePath: String
        )
    }
}