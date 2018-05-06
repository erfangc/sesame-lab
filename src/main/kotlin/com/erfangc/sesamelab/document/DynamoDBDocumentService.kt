package com.erfangc.sesamelab.document

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class DynamoDBDocumentService(private val dynamoDB: DynamoDB,
                              private val objectMapper: ObjectMapper) {

    private val tableName = "NERModelDocuments"

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
        val now = Instant.now()
        return if (document.id != null) {
            val item = table.getItem(PrimaryKey("id", document.id))
            val originalDocument = objectMapper.readValue<Document>(item.toJSON())
            val documentWithAuthorPreserved = document.copy(
                    creatorEmail = originalDocument.creatorEmail,
                    creatorID = originalDocument.creatorID,
                    createdOn = originalDocument.createdOn,
                    lastModifiedOn = now
            )
            table.putItem(Item.fromJSON(objectMapper.writeValueAsString(documentWithAuthorPreserved)))
            documentWithAuthorPreserved
        } else {
            val timestampedDocument = document.copy(
                    id = UUID.randomUUID().toString(),
                    createdOn = now,
                    lastModifiedOn = now
            )
            table.putItem(Item.fromJSON(objectMapper.writeValueAsString(timestampedDocument)))
            return timestampedDocument
        }
    }

}