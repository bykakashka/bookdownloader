package by.byka.bookdownloader.converter.data

import com.fasterxml.jackson.annotation.JsonProperty

data class SendFileResponseDto(
    @JsonProperty("id") val id: Id,
    @JsonProperty("completed") val completed: Boolean,
    @JsonProperty("warning") val warning: String?
) {
    data class Id(@JsonProperty("job") val job: String, @JsonProperty("input") val input: String)
}
