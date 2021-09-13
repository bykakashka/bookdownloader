package by.byka.bookdownloader.command

import by.byka.bookdownloader.UserService
import org.springframework.stereotype.Service
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

@Service
class RegistrationCommand(private val userService: UserService) : IBotCommand {
    override fun getCommandIdentifier(): String {
        return "email"
    }

    override fun getDescription(): String {
        return "test description"
    }

    override fun processMessage(absSender: AbsSender?, message: Message?, arguments: Array<out String>?) {
        if (message != null) {
            if (arguments != null && arguments.isNotEmpty() && arguments[0].matches(Regex("\\w+@\\w+\\.\\w+"))) {
                userService.updateUserInfo(message.from.id, message.chatId.toString(), arguments[0])
                val sendMessage = SendMessage()
                sendMessage.text = "Email updated"
                sendMessage.chatId = message.chatId.toString()
                absSender?.execute(sendMessage)
            } else {
                val sendMessage = SendMessage()
                sendMessage.text = "Invalid input"
                sendMessage.chatId = message.chatId.toString()
                absSender?.execute(sendMessage)
            }
        }
    }
}