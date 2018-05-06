package com.erfangc.sesamelab.model

import com.amazonaws.services.s3.AmazonS3
import com.erfangc.sesamelab.document.Document
import com.erfangc.sesamelab.document.DynamoDBDocumentService
import com.erfangc.sesamelab.document.ElasticsearchDocumentService
import com.erfangc.sesamelab.model.entities.NERModel
import com.erfangc.sesamelab.model.repositories.NERModelRepository
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSampleDataStream
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.TrainingParameters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class NERModelService(private val amazonS3: AmazonS3,
                      private val elasticsearchDocumentService: ElasticsearchDocumentService,
                      private val nerModelRepository: NERModelRepository) {
    private val logger = LoggerFactory.getLogger(NERModelService::class.java)
    private val bucketName = "sesame-lab"

    fun delete(id: Long) {
        logger.info("Deleting model $id")
        nerModelRepository.deleteById(id)
        amazonS3.deleteObject(bucketName, "$id.bin")
        logger.info("Deleted model $id")
    }

    /**
     * trains the model against the given corpus
     * subject to the passed in constraints
     *
     * we store the output model to S3, furthermore we store some metadata about the model to be reused later in
     * a SQL database
     */
    fun train(requestNER: TrainNERModelRequest): String {
        /*
        preparing parameters / local variables
         */
        val id = UUID.randomUUID().toString()
        val modifiedAfter = requestNER.modifiedAfter
        val user = requestNER.user

        val trainingJSONs = elasticsearchDocumentService
                .searchByCorpusID(corpusID = requestNER.corpusID, modifiedAfter = Instant.ofEpochMilli(modifiedAfter))
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
        val modelOutFile = File.createTempFile(id, ".bin")
        model.serialize(modelOutFile)
        amazonS3.putObject(bucketName, "$id.bin", modelOutFile)
        nerModelRepository
                .save(
                        NERModel()
                                .setCreatedOn(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                                .setName(requestNER.name)
                                .setUserID(user.id)
                                .setFileLocation("s3://$bucketName/$id.bin")
                                .setDescription(requestNER.description)
                )
        logger.info("Wrote model metadata into database")
        modelOutFile.delete()
        return id
    }

    fun run(modelID: String,
            sentence: String): String {
        /*
        create the tokenizer - we need it to break up the incoming sentence
         */
        logger.info("Creating tokenizer for $modelID, sentence=$sentence")
        val tokenModelIS = amazonS3.getObject(bucketName, "en-token.bin").objectContent
        logger.info("Loaded tokenizer model from S3")

        val tokenModel = TokenizerModel(tokenModelIS)
        val tokenizer = TokenizerME(tokenModel)

        /*
        load the trained model from S3
         */
        val modelInputStream = amazonS3
                .getObject(bucketName, "$modelID.bin")
                .objectContent
        val model = TokenNameFinderModel(modelInputStream)

        logger.info("Loaded model: $modelID.bin")
        val nameFinder = NameFinderME(model)
        val tokens = tokenizer.tokenize(sentence)
        logger.info("Tokenized $sentence into ${tokens.map { it }}")
        val nameSpans = nameFinder.find(tokens)
        nameFinder.clearAdaptiveData()
        /*
        build the NER tagged string based on name span
         */
        val stringBuilder = StringBuilder()
        var entityNo = 0
        var nextStart = if (nameSpans.isNotEmpty()) nameSpans[0].start else Int.MAX_VALUE
        var nextEnd = if (nameSpans.isNotEmpty()) nameSpans[0].end else Int.MAX_VALUE
        for (i in 0 until tokens.size) {
            /*
            if this token is the start of an entity, we append the tag
             */
            if (i == nextStart) {
                stringBuilder.append("<START:${nameSpans[entityNo].type}>")
            }
            stringBuilder
                    .append(" ")
                    .append(tokens[i])
            if (i == nextEnd - 1) {
                stringBuilder.append(" <END>")
                // update entityNo to the next name span
                entityNo++
                nextStart = if (entityNo >= nameSpans.size) Int.MAX_VALUE else nameSpans[entityNo].start
                nextEnd = if (entityNo >= nameSpans.size) Int.MAX_VALUE else nameSpans[entityNo].end
            }
        }
        return stringBuilder.toString()
    }

}