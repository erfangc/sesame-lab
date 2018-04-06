package com.erfangc.sesamelab.traning

import com.erfangc.sesamelab.corpus.CorpusBuilderService
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSampleDataStream
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.TrainingParameters
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets


@Service
class TrainingService(private val corpusBuilderService: CorpusBuilderService) {
    fun train(corpus: String, modifiedAfter: Long?, modelName: String): String {
        val trainingJSONs = corpusBuilderService.getModifiedAfter(modifiedAfter = modifiedAfter ?: 0L, corpus = corpus)
        val text = trainingJSONs.map { it.get("Content").asText().replace("\n", "") }.joinToString("\n")
        val lineStream = PlainTextByLineStream({ ByteArrayInputStream(text.toByteArray()) }, StandardCharsets.UTF_8)
        val sampleStream = NameSampleDataStream(lineStream)
        val model = NameFinderME.train(
                "en",
                "person",
                sampleStream,
                TrainingParameters.defaultParams(),
                TokenNameFinderFactory()
        )
        val modelOut = File("/Users/echen/$modelName.bin")
        model.serialize(modelOut)
        return modelName
    }
}