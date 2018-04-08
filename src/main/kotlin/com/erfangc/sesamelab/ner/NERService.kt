package com.erfangc.sesamelab.ner

import com.amazonaws.services.s3.AmazonS3
import com.erfangc.sesamelab.corpus.CorpusBuilderService
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSampleDataStream
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.Span
import opennlp.tools.util.TrainingParameters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

@Service
class NERService(private val corpusBuilderService: CorpusBuilderService, private val amazonS3: AmazonS3) {

    private val logger = LoggerFactory.getLogger(NERService::class.java)
    private val bucketName = "sesame-lab"

    fun delete(modelName: String) {
        amazonS3.deleteObject(bucketName, "$modelName.bin")
    }

    fun train(corpus: String,
              modelName: String,
              modifiedAfter: Long
    ): String {
        val trainingJSONs = corpusBuilderService.getModifiedAfter(modifiedAfter = modifiedAfter, corpus = corpus)
        val text = trainingJSONs.joinToString("\n") { it.content.replace("\n", "") }
        val lineStream = PlainTextByLineStream({ ByteArrayInputStream(text.toByteArray()) }, StandardCharsets.UTF_8)
        val sampleStream = NameSampleDataStream(lineStream)
        val model = NameFinderME.train(
                "en",
                null,
                sampleStream,
                TrainingParameters.defaultParams(),
                TokenNameFinderFactory()
        )
        // TODO record model metadata in a table somewhere
        val modelOutFile = File.createTempFile(modelName, ".bin")
        model.serialize(modelOutFile)
        amazonS3.putObject(bucketName, "$modelName.bin", modelOutFile)
        modelOutFile.delete()
        return modelName
    }

    fun run(modelName: String,
            sentence: String): Array<out Span>? {
        // TODO we need to cache the InputStream objects loaded from S3, and evict said cache via message broker since we may be running multiple instances of this server
        /*
        create the tokenizer - we need it to break up the incoming sentence
         */
        logger.info("Creating tokenizer for $modelName, sentence=$sentence")
        val tokenModelIS = amazonS3.getObject(bucketName, "en-token.bin").objectContent
        val tokenModel = TokenizerModel(tokenModelIS)
        val tokenizer = TokenizerME(tokenModel)

        /*
        load the trained model from S3
         */
        val modelInputStream = amazonS3
                .getObject(bucketName, "$modelName.bin")
                .objectContent
        val model = TokenNameFinderModel(modelInputStream)

        logger.info("Loaded model: $modelName.bin")
        val nameFinder = NameFinderME(model)
        val tokens = tokenizer.tokenize(sentence)
        logger.info("Tokenized $sentence into ${tokens.map { it }}")
        val nameSpans = nameFinder.find(tokens)
        nameFinder.clearAdaptiveData()
        return nameSpans
    }

}