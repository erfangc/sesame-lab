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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

data class NERModel(
        val modelID: String,
        val modelName: String,
        val modelDescription: String,
        val createdBy: String,
        val createdOn: LocalDateTime,
        val createdByEmail: String,
        val fileLocation: String
)

@Service
class NERService(private val corpusBuilderService: CorpusBuilderService,
                 private val amazonS3: AmazonS3,
                 jdbcTemplate: JdbcTemplate) {

    private val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
    private val logger = LoggerFactory.getLogger(NERService::class.java)
    private val bucketName = "sesame-lab"

    fun allModels(): List<NERModel> {
        return namedJdbcTemplate
                .query(
                        "SELECT * FROM models",
                        { resultSet, _ ->
                            NERModel(
                                    modelID = resultSet.getString("modelID"),
                                    modelName = resultSet.getString("modelName"),
                                    modelDescription = resultSet.getString("modelDescription"),
                                    createdBy = resultSet.getString("createdBy"),
                                    createdOn = resultSet.getTimestamp("createdOn").toLocalDateTime(),
                                    createdByEmail = resultSet.getString("createdByEmail"),
                                    fileLocation = resultSet.getString("createdByEmail")
                            )
                        }
                )
    }

    fun delete(modelID: String) {
        logger.info("Deleting model $modelID")
        namedJdbcTemplate.update("DELETE FROM models WHERE modelID = :modelID", mapOf("modelID" to modelID))
        amazonS3.deleteObject(bucketName, "$modelID.bin")
        logger.info("Deleted model $modelID")
    }

    /**
     * trains the model against the given corpus
     * subject to the passed in constraints
     *
     * we store the output model to S3, furthermore we store some metadata about the model to be reused later in
     * a SQL database
     */
    fun train(request: TrainModelRequest): String {
        /*
        preparing parameters / local variables
         */
        val modelID = UUID.randomUUID().toString()
        val modelName = request.modelName
        val modifiedAfter = request.modifiedAfter
        val corpus = request.corpus
        val modelDescription = request.modelDescription
        val user = request.user

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
        val modelOutFile = File.createTempFile(modelID, ".bin")
        model.serialize(modelOutFile)
        amazonS3.putObject(bucketName, "$modelID.bin", modelOutFile)

        logger.info("Writing model metadata into database")
        val parameters = mapOf(
                "modelID" to modelID,
                "modelName" to modelName,
                "modelDescription" to (modelDescription ?: "no description"),
                "createdBy" to user.id,
                "createdByEmail" to user.email,
                "fileLocation" to "https://s3.amazonaws.com/sesame-lab/$modelID.bin"
        )
        namedJdbcTemplate
                .update(
                        "INSERT INTO models (modelID, modelName, modelDescription, createdOn, createdBy, createdByEmail, fileLocation) " +
                                "VALUES (:modelID, :modelName, :modelDescription, now(),  :createdBy, :createdByEmail, :fileLocation);",
                        parameters
                )
        logger.info("Wrote model metadata into database")

        modelOutFile.delete()
        return modelID
    }

    fun run(modelID: String,
            sentence: String): Array<out Span>? {
        // TODO we need to cache the InputStream objects loaded from S3, and evict said cache via message broker since we may be running multiple instances of this server
        /*
        create the tokenizer - we need it to break up the incoming sentence
         */
        logger.info("Creating tokenizer for $modelID, sentence=$sentence")
        val tokenModelIS = amazonS3.getObject(bucketName, "en-token.bin").objectContent
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
        return nameSpans
    }

}