package com.erfangc.sesamelab.corpus

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class CorpusBuilderService(private val dynamoDB: DynamoDB,
                           private val objectMapper: ObjectMapper) {

    private val tableName = "TrainingDocuments"
    private val logger = LoggerFactory.getLogger(CorpusBuilderService::class.java)

    /**
     * retrieves an single document / sentence by ID
     */
    fun getById(id: String): Document {
        val table = dynamoDB.getTable(tableName)
        val item = table.getItem("id", id)
        return objectMapper.readValue(item.toJSON())
    }

    fun delete(id: String) {
        val table = dynamoDB.getTable(tableName)
        table.deleteItem(PrimaryKey("id", id))
    }

    /**
     * tries to up-sert the record into the database
     * returns the unique id of the record
     */
    fun put(document: Document): Document {
        /*
        if id is populated, then retrieve the existing document from DynamoDB
        and preserve createdBy info

        this approach does not guarantee atomicity but is much cleaner than setting fields manually using the
        field update syntax that DynamoDB provides. Happy to take suggestions on how to improve this
         */
        val table = dynamoDB.getTable(tableName)
        return if (document.id != null) {
            val item = table.getItem(PrimaryKey("id", document.id))
            val originalDocument = objectMapper.readValue<Document>(item.toJSON())
            val documentWithAuthorPreserved = document.copy(
                    createdByEmail = originalDocument.createdByEmail,
                    createdBy = originalDocument.createdBy,
                    createdOn = originalDocument.createdOn,
                    createdByNickname = originalDocument.createdByNickname,
                    lastModifiedOn = System.currentTimeMillis()
            )
            table.putItem(Item.fromJSON(objectMapper.writeValueAsString(documentWithAuthorPreserved)))
            documentWithAuthorPreserved
        } else {
            val timestampedDocument = document.copy(
                    id = UUID.randomUUID().toString(),
                    createdOn = System.currentTimeMillis(),
                    lastModifiedOn = System.currentTimeMillis()
            )
            table.putItem(Item.fromJSON(objectMapper.writeValueAsString(timestampedDocument)))
            return timestampedDocument
        }
    }

    /**
     * query all documents created by the given creator in this corpus
     */
    fun getByCreator(creator: String, corpus: String): List<Document>? {
        val table = dynamoDB.getTable(tableName)
        val index = table.getIndex("createdBy-index")
        val query = QuerySpec()
                .withKeyConditionExpression("createdBy = :creator")
                .withFilterExpression("corpus = :corpus")
                .withValueMap(mapOf(":creator" to creator, ":corpus" to corpus))
        val queryResult = index.query(query)
        return queryResult.map { objectMapper.readValue<Document>(it.toJSON()) }
    }

    /**
     * queries all documents in a given corpus modified after a certain date
     */
    fun getModifiedAfter(modifiedAfter: Long, corpus: String): List<Document> {
        val table = dynamoDB.getTable(tableName)
        val index = table.getIndex("corpus-lastModifiedOn-index")
        val query = QuerySpec()
                .withKeyConditionExpression("corpus = :corpus AND lastModifiedOn >= :modifiedAfter")
                .withValueMap(mapOf(":modifiedAfter" to modifiedAfter, ":corpus" to corpus))
        val queryResult = index.query(query)
        return queryResult.map { objectMapper.readValue<Document>(it.toJSON()) }
    }

}