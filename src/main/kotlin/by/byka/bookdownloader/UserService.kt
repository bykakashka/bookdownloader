package by.byka.bookdownloader

import by.byka.bookdownloader.entity.User
import by.byka.bookdownloader.service.EmailSenderService
import by.byka.bookdownloader.table.UserTable
import org.apache.commons.lang3.RandomStringUtils
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


class UserService(
    private val database: Database,
    private val emailSenderService: EmailSenderService
) {
    private val log = LogManager.getLogger(UserService::class.java)

    fun getUserByChatId(chatId: Long): User? {
        return serializableTransaction(database) {
            User.findById(chatId)
        }
    }

    fun updateUserEmail(chatId: Long, newEmail: String) {
        val randomCode =  RandomStringUtils.randomAlphanumeric(5)
        transaction(database) {
            val user = User.findById(chatId)
            if (user != null) {
                UserTable.update({ UserTable.id eq chatId }) {
                    it[email] = newEmail
                    it[code] = randomCode
                    it[activated] = false
                }
            } else {
                User.new(chatId) {
                    email = newEmail
                    code = randomCode
                }
            }
        }

        emailSenderService.sendCode(chatId, randomCode, newEmail)
    }

    fun activateUser(id: EntityID<Long>) {
        transaction(database) {
            UserTable.update({ UserTable.id eq id }) {
                it[activated] = true
            }
        }
    }
}