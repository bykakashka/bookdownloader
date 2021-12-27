package by.byka.bookdownloader.converter.data

import com.fasterxml.jackson.annotation.JsonProperty

data class InitRequestDto(@JsonProperty("conversion") val conversion: List<TargetDto>) {
    data class TargetDto(
        @JsonProperty("target")
        val target: String
    )
}