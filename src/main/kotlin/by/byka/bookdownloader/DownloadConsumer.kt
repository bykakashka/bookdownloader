package by.byka.bookdownloader

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@EnableKafka
@Service
class DownloadConsumer(private val kafkaTemplate: KafkaTemplate<String, String>, private val telegramBot: TelegramBot) {
    var PATH = "E://img/"

    fun download(link: String): String {
        val conn = URL(link).openConnection()

        val filename = "TODO get from link"
        if (conn is HttpURLConnection) {
            conn.getInputStream().use { input ->
                FileOutputStream(File(PATH + filename)).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return PATH + filename
    }

    @KafkaListener(topics = ["download"])
    fun listener(record: ConsumerRecord<String, String>) {
        try {
            val filePath = download(record.value())
            kafkaTemplate.send("sendEmail", record.key(), filePath)
            sendInfo(record.key())
        } catch (e: Exception) {
            sendException(record.key(), e.message.toString())
        }
    }

    fun sendException(chatId: String, message: String) {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "Exception in the file donwloading: \n $message"
        telegramBot.execute(infoMessage)
    }

    fun sendInfo(chatId: String) {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "Downloaded"
        telegramBot.execute(infoMessage)
    }
}