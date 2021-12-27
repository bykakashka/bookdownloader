package by.byka.bookdownloader.service

import by.byka.bookdownloader.Constants.EMAIL
import by.byka.bookdownloader.Constants.PWD
import by.byka.bookdownloader.UserService
import org.apache.logging.log4j.LogManager
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

class EmailSenderService {

    private val log = LogManager.getLogger(EmailSenderService::class.java)

    fun sendCode(chatId: Long, code: String, userEmail: String): SendMessage {
        log.trace("Start sending code by email")

        val props = initProperties()
        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                return javax.mail.PasswordAuthentication(EMAIL, PWD)
            }
        })

        return try {
            val mimeMessage = MimeMessage(session)
            mimeMessage.setText(code)
            mimeMessage.setFrom(InternetAddress(EMAIL))
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail, false))
            mimeMessage.sentDate = Date()

            val smtpTransport = session.getTransport("smtp")
            smtpTransport.connect()
            smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
            smtpTransport.close()
            log.info("Email had been send successful")
            sendInfo(chatId.toString())
        } catch (messagingException: MessagingException) {
            log.warn("Cannot send email: ${messagingException.message}", messagingException)
            sendExceptionMessage(chatId.toString(), messagingException.message.toString())
        }
    }

    fun sendFile(chatId: Long, filePath: String, userEmail: String): SendMessage {
        log.trace("Start sending file by email")

        val props = initProperties()
        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                return javax.mail.PasswordAuthentication(EMAIL, PWD)
            }
        })

        return try {
            val mimeMessage = initMessage(session, userEmail, filePath)
            val smtpTransport = session.getTransport("smtp")
            smtpTransport.connect()
            smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
            smtpTransport.close()
            log.info("Email had been send successful")
            sendInfo(chatId.toString())
        } catch (messagingException: MessagingException) {
            log.warn("Cannot send email: ${messagingException.message}", messagingException)
            sendExceptionMessage(chatId.toString(), messagingException.message.toString())
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

    private fun sendInfo(chatId: String): SendMessage {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "Book send by email"
        return infoMessage
    }

    private fun sendExceptionMessage(chatId: String, message: String): SendMessage {
        val infoMessage = SendMessage()
        infoMessage.chatId = chatId
        infoMessage.text = "Error in the email sender: \n $message"
        return infoMessage
    }
}