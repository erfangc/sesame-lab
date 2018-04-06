package com.erfangc.sesamelab.ner

import com.amazonaws.services.s3.AmazonS3
import com.erfangc.sesamelab.corpus.CorpusBuilderService
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSampleDataStream
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.Span
import opennlp.tools.util.TrainingParameters
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

@Service
class NERService(private val corpusBuilderService: CorpusBuilderService, private val amazonS3: AmazonS3) {

    fun delete(modelName: String, type: String, corpus: String) {
        val modelFilename = getModelFilename(modelName = modelName, corpus = corpus, type = type)
        amazonS3.deleteObject("sesame-lab", "$modelFilename.bin")
    }

    fun train(corpus: String,
              modelName: String,
              type: String,
              modifiedAfter: Long
    ): String {
        val trainingJSONs = corpusBuilderService.getModifiedAfter(modifiedAfter = modifiedAfter, corpus = corpus)
        val text = trainingJSONs.map { it.get("Content").asText().replace("\n", "") }.joinToString("\n")
        val lineStream = PlainTextByLineStream({ ByteArrayInputStream(text.toByteArray()) }, StandardCharsets.UTF_8)
        val sampleStream = NameSampleDataStream(lineStream)
        val model = NameFinderME.train(
                "en",
                type,
                sampleStream,
                TrainingParameters.defaultParams(),
                TokenNameFinderFactory()
        )
        val modelFilename = getModelFilename(modelName, corpus, type)
        val modelOutFile = File.createTempFile(modelFilename, ".bin")
        model.serialize(modelOutFile)
        amazonS3.putObject("sesame-lab", "$modelFilename.bin", modelOutFile)
        modelOutFile.delete()
        // TODO we need to configure tokenizer
        return modelFilename
    }

    fun run(modelName: String,
            corpus: String,
            type: String,
            sentence: String): Array<out Span>? {
        val modelFilename = getModelFilename(modelName, corpus, type)
        val modelInputStream = amazonS3
                .getObject("sesame-lab", "$modelFilename.bin")
                .objectContent
        val model = TokenNameFinderModel(modelInputStream)
        val nameFinder = NameFinderME(model)
        val nameSpans = nameFinder.find(sentence.split(" ").toTypedArray())
        nameFinder.clearAdaptiveData()
        // TODO we need to configure tokenizer
        return nameSpans
    }

    private fun getModelFilename(modelName: String, corpus: String, type: String) = "$modelName-$corpus-$type"

}