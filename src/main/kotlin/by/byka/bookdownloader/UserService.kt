package by.byka.bookdownloader

import by.byka.bookdownloader.entity.User
import org.springframework.stereotype.Service

@Service
class UserService {
    private val db = HashMap<String, User>()

    fun getUserEmailByChatId(chatId: String) : String? {
        return db[chatId]?.email
    }

    fun isEmailExist(chatId: String): Boolean {
        return db.containsKey(chatId)
    }

    fun updateUserInfo(id: Long, chatId: String, email: String) {
        val user = db.getOrDefault(chatId, User())
        user.email = email
        user.chatId = chatId
        user.id = id
        db[chatId] = user
    }
}