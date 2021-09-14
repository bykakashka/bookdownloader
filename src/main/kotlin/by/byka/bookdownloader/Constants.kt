package by.byka.bookdownloader

import org.springframework.boot.system.SystemProperties

object Constants {
    val EMAIL: String = SystemProperties.get("SMTP_LOGIN")
    val PWD : String = SystemProperties.get("SMTP_PWD")
    val BOT_TOKEN : String = SystemProperties.get("BOT_TOKEN")
    val BOT_NAME : String = SystemProperties.get("BOT_NAME")
    val CONVERTER_URL : String = SystemProperties.get("CONVERTER_URL")
    val CONVERTER_TOKEN : String = SystemProperties.get("CONVERTER_TOKEN")
}