package by.byka.bookdownloader

import by.byka.bookdownloader.Constants.BOT_NAME
import by.byka.bookdownloader.Constants.BOT_TOKEN
import by.byka.bookdownloader.command.HelpCommand
import by.byka.bookdownloader.command.RegistrationCommand
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import javax.annotation.PostConstruct

@Service
class TelegramBot(
        private val kafkaTemplate: KafkaTemplate<String, String>,
        private val userService: UserService
    ): TelegramLongPollingCommandBot() {

    @PostConstruct
    fun init() {
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
            val userEmail = userService.getUserEmailByChatId(update.message.chatId.toString())
            if (userEmail != null) {
                kafkaTemplate.send("download", update.message.chatId.toString(), update.message.text)
            } else {
                val response = SendMessage()
                response.enableMarkdown(true)
                response.chatId = update.message.chatId.toString()
                response.text = "Fill your email first"
                execute(response)
            }
        }
        println(update)
    }
}