package by.byka.bookdownloader.converter.data

class InitResponseDto() {
    var id: String? = null
    var server: String? = null

    constructor(id: String, server: String) : this() {
        this.id = id
        this.server = server
    }
}
