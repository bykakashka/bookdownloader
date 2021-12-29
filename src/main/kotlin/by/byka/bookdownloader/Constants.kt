package by.byka.bookdownloader

object Constants {
    val EMAIL: String = System.getenv("SMTP_LOGIN")
    val PWD: String = System.getenv("SMTP_PWD")
    val BOT_TOKEN: String = System.getenv("BOT_TOKEN")
    val BOT_NAME: String = System.getenv("BOT_NAME")
    val CONVERTER_URL: String = System.getenv("CONVERTER_URL")
    val CONVERTER_TOKEN: String = System.getenv("CONVERTER_TOKEN")
    val DB_NAME: String = System.getenv("POSTGRES_DB")
    val DB_PWD: String = System.getenv("POSTGRES_PASSWORD")
    val DB_USER: String = System.getenv("POSTGRES_USER")
    val DB_HOST: String = System.getenv("DB_HOST")
    val HOME_FOLDER: String = System.getenv("HOME_FOLDER")
}