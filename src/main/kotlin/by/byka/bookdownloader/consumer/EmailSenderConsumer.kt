package by.byka.bookdownloader.consumer

import by.byka.bookdownloader.Constants.EMAIL
import by.byka.bookdownloader.Constants.PWD
import by.byka.bookdownloader.TelegramBot
import by.byka.bookdownloader.UserService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.io.File
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@EnableKafka
@Service
class EmailSenderConsumer(private val userService: UserService, private val telegramBot: TelegramBot) {
    @KafkaListener(topics = ["sendEmail"])
    fun sendEmail(record: ConsumerRecord<String, String>) {
        val filePath = record.value()
        val props = initProperties()

        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                return javax.mail.PasswordAuthentication(EMAIL, PWD)
            }
        })

        try {
            val mimeMessage = initMessage(session, userService.getUserEmailByChatId(record.key()) ?: "", filePath)
            val smtpTransport = session.getTransport("smtp")
            smtpTransport.connect()
            smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
            smtpTransport.close()
            sendInfo(record.key())
        } catch (messagingException: MessagingException) {
            sendExceptionMessage(record.key(), messagingException.message.toString())
            messagingException.printStackTrace()
        }
    }

    private fun initMessage(
        session: Session?,
        email: String,
        filePath: String
    ): MimeMessage {
        val mimeMessage = MimeMessage(session)
        mimeMessage.setFrom(InternetAddress(EMAIL))
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false))
        mimeMessage.sentDate = Date()

        val body = MimeBodyPart()
        body.dataHandler = DataHandler(FileDataSource(filePath))
        body.fileName = getFilenameFromPath(filePath)
        val multipart = MimeMultipart()
        multipart.addBodyPart(body)
        mimeMessage.setContent(multipart)
        return mimeMessage
    }

    private fun initProperties(): Properties {
        val props = Properties()
        putIfMissing(props, "mail.smtp.host", "smtp.gmail.com")
        putIfMissing(props, "mail.smtp.port", "587")
        putIfMissing(props, "mail.smtp.auth", "true")
        putIfMissing(props, "mail.smtp.starttls.enable", "true")
        return props
    }

    private fun putIfMissing(props: Properties, key: String, value: String) {
        if (!props.containsKey(key)) {
            props[key] = value
        }
    }

    private fun getFilenameFromPath(path: String): String {
        return path.substringAfterLast(File.separator)
    }

    fun sendInfo(chatId: String) {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "Book send by email"
        telegramBot.execute(infoMessage)
    }

    fun sendExceptionMessage(chatId: String, message: String) {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "Error in the email sender: \n $message"
        telegramBot.execute(infoMessage)
    }
}