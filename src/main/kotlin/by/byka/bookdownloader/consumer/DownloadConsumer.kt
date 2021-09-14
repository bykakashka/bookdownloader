package by.byka.bookdownloader.consumer

import by.byka.bookdownloader.DownloadService
import by.byka.bookdownloader.TelegramBot
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.io.File

@EnableKafka
@Service
class DownloadConsumer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val telegramBot: TelegramBot,
    private val downloadService: DownloadService
) {

    @KafkaListener(topics = ["download"])
    fun listener(record: ConsumerRecord<String, String>) {
        try {
            val filePath = downloadService.download(record.value())
            sendInfo(record.key(), filePath.substringAfterLast(File.separator))
            if (filePath.endsWith(".mobi")) {
                kafkaTemplate.send("sendEmail", record.key(), filePath)
            } else {
                kafkaTemplate.send("convert", record.key(), filePath)
            }
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

    fun sendInfo(chatId: String, filename: String) {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "$filename downloaded"
        telegramBot.execute(infoMessage)
    }
}