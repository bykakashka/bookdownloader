package by.byka.bookdownloader.data

class ConverterResult() {
    lateinit var status: ConvertStatus
    var info: String? = null
    var convertedUrl: String? = null

    constructor(url: String) : this() {
        this.convertedUrl = url
        this.status = ConvertStatus.SUCCESS
    }

    constructor(info: String, status: ConvertStatus) : this() {
        this.info = info
        this.status = status
    }
}

enum class ConvertStatus {
    SUCCESS, ERROR
}