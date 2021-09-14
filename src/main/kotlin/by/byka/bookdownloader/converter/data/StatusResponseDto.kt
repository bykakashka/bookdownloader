package by.byka.bookdownloader.converter.data

class StatusResponseDto() {
    var status: StatusDto? = null
    lateinit var output: List<OutputDto>

    class StatusDto() {
        var code: String? = null
        var info: String? = null

        constructor(code: String, info: String): this() {
            this.code = code
            this.info = info
        }
    }

    class OutputDto() {
        lateinit var filename: String
        lateinit var uri: String
    }
}