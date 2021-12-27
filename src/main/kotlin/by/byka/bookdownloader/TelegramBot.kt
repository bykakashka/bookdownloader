package by.byka.bookdownloader

import by.byka.bookdownloader.Constants.BOT_NAME
import by.byka.bookdownloader.Constants.BOT_TOKEN
import by.byka.bookdownloader.command.HelpCommand
import by.byka.bookdownloader.command.RegistrationCommand
import by.byka.bookdownloader.converter.service.ConverterService
import by.byka.bookdownloader.service.DownloadService
import by.byka.bookdownloader.service.EmailSenderService
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Document
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class TelegramBot(
    private val userService: UserService,
    private val emailSenderService: EmailSenderService,
    private val downloadService: DownloadService,
    private val converterService: ConverterService,
    ): TelegramLongPollingCommandBot() {

    init {
        val botApi = TelegramBotsApi(DefaultBotSession::class.java)
        botApi.registerBot(this)
        this.register(RegistrationCommand(userService))
        register(HelpCommand())
    }

    override fun getBotToken(): String {
        return BOT_TOKEN
    }

    override fun getBotUsername(): String {
        return BOT_NAME
    }

    override fun processNonCommandUpdate(update: Update?) {
        if (update != null) {
            val user = userService.getUserByChatId(update.message.chatId)
            if (user != null) {
                if (user.activated) {
                    if (update.message.text != null) {
                        processLink(update.message.text, update.message.chatId, user.email)
                    }
                    if (update.message.document != null) {
                        processDocument(update.message.document, update.message.chatId, user.email)
                    }
                } else {
                    if (user.code == update.message.text) {
                        userService.activateUser(user.id)
                        sendMessage("User activated", update.message.chatId.toString())
                    } else {
                        sendMessage("Incorrect code", update.message.chatId.toString())
                    }
                }
            } else {
                val response = SendMessage()
                response.enableMarkdown(true)
                response.chatId = update.message.chatId.toString()
                response.text = "Fill your email first"
                execute(response)
            }
        }
    }

    private fun processDocument(document: Document, chatId: Long, userEmail: String) {
        val fileId = document.fileId
        val fileName = document.fileName
        val filePath = downloadService.downloadForTg(fileId, chatId.toString(), fileName)
        if (filePath != null) {
            processLocalFile(filePath, chatId, userEmail)
        }
    }

    private fun processLink(url: String, chatId: Long, userEmail: String) {
        val filePath = downloadService.download(url)
        processLocalFile(filePath, chatId, userEmail)
    }

    private fun processLocalFile(filePath: String, chatId: Long, userEmail: String) {
        if (filePath.endsWith(".mobi")) {
            val infoMessage = emailSenderService.sendFile(chatId, filePath, userEmail)
            this.execute(infoMessage)
            println(filePath)
        } else {
            val result = converterService.convert(filePath)
            if (result!= null) {
                val message = emailSenderService.sendFile(chatId, result, userEmail)
                execute(message)
            } else {
                sendMessage("Cannot convert file, check logs", chatId.toString())
            }
        }
    }

    private fun sendMessage(text: String, chatId: String) {
        val response = SendMessage()
        response.enableMarkdown(true)
        response.chatId = chatId
        response.text = text
        execute(response)
    }
}