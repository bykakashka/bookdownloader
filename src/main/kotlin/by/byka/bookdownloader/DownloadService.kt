package by.byka.bookdownloader

import org.springframework.stereotype.Service
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Service
class DownloadService {
    var PATH = "E:" + File.separator + "img" + File.separator

    fun download(link: String): String {
        val conn = URL(link).openConnection()
        val contentHeader = conn.getHeaderField("content-disposition")
        val filename = contentHeader?.substringAfter("filename=", ";")?.replace("\"", "")
            ?: "TODO get from link"
        if (conn is HttpURLConnection) {
            conn.getInputStream().use { input ->
                FileOutputStream(File(PATH + filename)).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return PATH + filename
    }
}