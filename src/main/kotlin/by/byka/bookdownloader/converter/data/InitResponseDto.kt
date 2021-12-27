package by.byka.bookdownloader.converter.data

import com.fasterxml.jackson.annotation.JsonProperty

data class InitResponseDto(@JsonProperty("id") val id: String, @JsonProperty("server") val server: String)
