package by.byka.bookdownloader.consumer

import by.byka.bookdownloader.TelegramBot
import by.byka.bookdownloader.converter.service.ConverterService
import by.byka.bookdownloader.data.ConvertStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
@EnableKafka
class ConverterConsumer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val telegramBot: TelegramBot,
    private val converterService: ConverterService
) {

    @KafkaListener(topics = ["convert"])
    fun convertFile(record: ConsumerRecord<String, String>) {
        val result = converterService.convert(record.value())
        if (result.status.equals(ConvertStatus.SUCCESS)) {
            kafkaTemplate.send("sendEmail", record.key(), result.convertedUrl)
        } else {
            // TODO tg message
        }
    }

}