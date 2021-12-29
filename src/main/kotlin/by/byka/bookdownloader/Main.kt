package by.byka.bookdownloader

import by.byka.bookdownloader.Constants.DB_HOST
import by.byka.bookdownloader.Constants.DB_NAME
import by.byka.bookdownloader.Constants.DB_PWD
import by.byka.bookdownloader.Constants.DB_USER
import by.byka.bookdownloader.Constants.HOME_FOLDER
import by.byka.bookdownloader.converter.service.ConverterService
import by.byka.bookdownloader.service.DownloadService
import by.byka.bookdownloader.service.EmailSenderService
import by.byka.bookdownloader.table.UserTable
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection
import java.util.concurrent.TimeUnit


private val log = LogManager.getLogger(Main::class.java)

fun jacksonMapper(): ObjectMapper =
    ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

fun <T> serializableTransaction(db: Database? = null, function: Transaction.() -> T)
        = transaction(Connection.TRANSACTION_SERIALIZABLE, 3, db, function)

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val db = Database.connect(
                "jdbc:postgresql://$DB_HOST:5432/$DB_NAME",
                driver = "org.postgresql.Driver",
                user = DB_USER,
                password = DB_PWD
            )
            transaction(db) { SchemaUtils.create(UserTable) }

            val httpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor {
                    log.trace(it.request())
                    it.proceed(it.request())
                }
                .build()

            val emailSenderService = EmailSenderService()
            val userService = UserService(db, emailSenderService)
            val downloadService = DownloadService(httpClient)
            val convertService = ConverterService(downloadService, httpClient)

            val fileDirs = File(HOME_FOLDER)
            if (!fileDirs.exists()) {
                fileDirs.mkdir()
            }

            TelegramBot(userService, emailSenderService, downloadService, convertService)
        }
    }
}
