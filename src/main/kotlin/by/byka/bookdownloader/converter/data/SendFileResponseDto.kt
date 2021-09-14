package by.byka.bookdownloader.converter.data

class SendFileResponseDto {
    class Id() {
        var job: String? = null
        var input: String? = null
    }
    var id: Id? = null
    var completed: Boolean = false
    var warning: String? = null
}