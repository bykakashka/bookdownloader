package by.byka.bookdownloader.command

import by.byka.bookdownloader.UserService
import org.apache.logging.log4j.LogManager
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

class RegistrationCommand(private val userService: UserService) : IBotCommand {

    private val log = LogManager.getLogger(RegistrationCommand::class.java)

    override fun getCommandIdentifier(): String {
        return "email"
    }

    override fun getDescription(): String {
        return "test description"
    }

    override fun processMessage(absSender: AbsSender?, message: Message?, arguments: Array<out String>?) {
        if (message != null) {
            if (arguments != null && arguments.isNotEmpty() && arguments[0].matches(Regex("\\w+@\\w+\\.\\w+"))) {
                userService.updateUserEmail(message.chatId, arguments[0])
                log.info("Update email")
                val sendMessage = SendMessage()
                sendMessage.text = "Provide code sent to your email address"
                sendMessage.chatId = message.chatId.toString()
                absSender?.execute(sendMessage)
            } else {
                log.warn("Cannot update email = ${arguments?.let { it[0] }} for user ${message.chatId}")
                val sendMessage = SendMessage()
                sendMessage.text = "Invalid input"
                sendMessage.chatId = message.chatId.toString()
                absSender?.execute(sendMessage)
            }
        }
    }
}