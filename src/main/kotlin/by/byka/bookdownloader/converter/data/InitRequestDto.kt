package by.byka.bookdownloader.converter.data

class InitRequestDto() {
    var input: Array<TypeSourceDto>? = null
    var conversion: Array<TargetDto>? = null

    constructor(
        input: Array<TypeSourceDto>?,
        conversion: Array<TargetDto>?) : this(){
        this.input = input
        this.conversion = conversion
    }
}

class TypeSourceDto(
    var type: String,
    var source: String
)

class TargetDto(
    var target: String
)