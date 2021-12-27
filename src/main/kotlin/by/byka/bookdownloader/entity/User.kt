package by.byka.bookdownloader.entity

import by.byka.bookdownloader.table.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<User>(UserTable)
    var email by UserTable.email
    var activated by UserTable.activated
    var code by UserTable.code
}