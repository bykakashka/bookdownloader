package by.byka.bookdownloader.converter.service

import by.byka.bookdownloader.Constants.CONVERTER_TOKEN
import by.byka.bookdownloader.Constants.CONVERTER_URL
import by.byka.bookdownloader.DownloadService
import by.byka.bookdownloader.converter.data.*
import by.byka.bookdownloader.data.ConvertStatus
import by.byka.bookdownloader.data.ConverterResult
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI

@Service
class ConverterService(
    private val downloadService: DownloadService
) {
    val HEADER_NAME : String = "X-Oc-Api-Key"

    fun convert(filepath: String): ConverterResult {
        val initResp = initUpload()
        val uploadResp = sendFile(initResp, filepath)
        return getConvertedUrl(uploadResp.body)
    }

    private fun getConvertedUrl(uploadResp: SendFileResponseDto): ConverterResult {
        // TODO validate code before getting body
        val statusResp = checkStatus(uploadResp).body
        return if (statusResp.status?.code.equals("completed")) {
            ConverterResult(downloadService.download(statusResp.output[0].uri))
        } else if (statusResp.status?.code.equals("processing")) {
            Thread.sleep(10000)
            getConvertedUrl(uploadResp)
        } else {
            ConverterResult("Unexpected job status ${statusResp.status?.code}", ConvertStatus.ERROR)
        }
    }

    private fun checkStatus(uploadResp: SendFileResponseDto): ResponseEntity<StatusResponseDto> {
        val restTemplate = RestTemplateBuilder()
            .defaultHeader(HEADER_NAME, CONVERTER_TOKEN)
            .build()
        val statusResp = restTemplate.getForEntity(CONVERTER_URL + "/" + uploadResp.id?.job, StatusResponseDto::class.java)
        return statusResp
    }

    private fun sendFile(initResp: ResponseEntity<InitResponseDto>, filepath: String): ResponseEntity<SendFileResponseDto> {
        val body: MultiValueMap<String, FileSystemResource> = LinkedMultiValueMap()
        body.add("file", FileSystemResource(filepath))
        val httpEntity = HttpEntity(body)

        val restTemplate = RestTemplateBuilder()
            .defaultHeader(HEADER_NAME, CONVERTER_TOKEN)
            .defaultHeader("Content-Type", "multipart/form-data")
            .build()

        val resp = restTemplate.postForEntity(
            initResp.body.server + "/upload-file/" + initResp.body.id,
            httpEntity,
            SendFileResponseDto::class.java
        )
        return resp
    }

    private fun initUpload(): ResponseEntity<InitResponseDto> {
        val restTemplate = RestTemplateBuilder()
            .defaultHeader(HEADER_NAME, CONVERTER_TOKEN)
            .build()
        val resp = restTemplate.postForEntity(
            URI(CONVERTER_URL),
            InitRequestDto(input = null, conversion = arrayOf(TargetDto("mobi"))),
            InitResponseDto::class.java
        )

        return resp
    }
}