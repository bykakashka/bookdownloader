package by.byka.bookdownloader.command

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

class HelpCommand: IBotCommand {
    override fun getCommandIdentifier(): String {
        return "help"
    }

    override fun getDescription(): String {
        return "test help description"
    }

    override fun processMessage(absSender: AbsSender?, message: Message?, arguments: Array<out String>?) {
        val sendMessage = SendMessage()
        sendMessage.text = "Register with command /email and your email. For example /email test@example.com"
        sendMessage.chatId = message?.chatId.toString()
        absSender?.execute(sendMessage)
    }
}