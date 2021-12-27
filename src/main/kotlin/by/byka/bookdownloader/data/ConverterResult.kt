package by.byka.bookdownloader.data

data class ConverterResult(
    val convertedUrl: String,
    val status: ConvertStatus
) {
    constructor(url: String) : this(url, ConvertStatus.SUCCESS)
}

enum class ConvertStatus {
    SUCCESS, ERROR
}