package by.byka.bookdownloader.table

import org.jetbrains.exposed.dao.id.LongIdTable


object UserTable: LongIdTable(name = "users", columnName = "chat_id") {
    val email = varchar("email", 50)
    val activated = bool("activated").default(false)
    val code = varchar("confirm_code", 10).nullable()
}