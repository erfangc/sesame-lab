package com.erfangc.sesamelab

import com.erfangc.sesamelab.corpus.CorpusDescriptor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("api/v1/ui-config")
class UIConfigurationController(private val objectMapper: ObjectMapper) {
    @GetMapping
    fun get(): UIConfiguration {
        val corpusDescriptors = objectMapper
                .readValue<List<CorpusDescriptor>>(UIConfigurationController::class.java.classLoader.getResourceAsStream("corpus-descriptors.json"))
        return UIConfiguration(corpusDescriptors = corpusDescriptors)
    }
}