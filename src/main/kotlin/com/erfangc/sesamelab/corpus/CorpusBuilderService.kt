package com.erfangc.sesamelab.corpus

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.System.currentTimeMillis
import java.util.*

@Service
class CorpusBuilderService(private val amazonDynamoDB: AmazonDynamoDB, private val dynamoDB: DynamoDB) {

    private val objectMapper = ObjectMapper()
    private val tableName = "NamedEntities"
    private val logger = LoggerFactory.getLogger(CorpusBuilderService::class.java)

    /**
     * retrieves an single document / sentence by ID
     */
    fun getById(id: String): JsonNode {
        val table = dynamoDB.getTable(tableName)
        val item = table.getItem("Id", id)
        return objectMapper.readValue(item.toJSON())
    }

    fun delete(id: String) {
        val table = dynamoDB.getTable(tableName)
        table.deleteItem(PrimaryKey("Id", id))
    }

    /**
     * tries to up-sert the record into the database
     * returns the unique id of the record
     */
    fun put(id: String?,
            content: String,
            user: String,
            corpus: String
    ): String {
        val resolvedID = id ?: UUID.randomUUID().toString()
        val request = UpdateItemRequest()
                .withTableName(tableName)
                .withUpdateExpression(
                        "SET Content = :content, " +
                                "CreatedBy = if_not_exists(CreatedBy, :user), " +
                                "CreatedOn = if_not_exists(CreatedOn,:now), " +
                                "LastModifiedBy = :user, " +
                                "LastModified = :now, " +
                                "Corpus = :corpus"
                )
                .withKey(mapOf("Id" to AttributeValue().withS(resolvedID)))
                .withExpressionAttributeValues(
                        mapOf(
                                ":content" to AttributeValue().withS(content),
                                ":user" to AttributeValue().withS(user),
                                ":now" to AttributeValue().withN("${currentTimeMillis()}"),
                                ":corpus" to AttributeValue().withS(corpus)
                        )
                )
                .withReturnValues(ReturnValue.UPDATED_NEW)
        val updateItemResult = amazonDynamoDB.updateItem(request)
        logger.info(updateItemResult.toString())
        return resolvedID
    }

    /**
     * query all documents created by the given creator in this corpus
     */
    fun getByCreator(creator: String, corpus: String): List<JsonNode>? {
        val table = dynamoDB.getTable(tableName)
        val index = table.getIndex("CreatedBy-index")
        val query = QuerySpec()
                .withKeyConditionExpression("CreatedBy = :creator")
                .withFilterExpression("Corpus = :corpus")
                .withValueMap(mapOf(":creator" to creator, ":corpus" to corpus))
        val queryResult = index.query(query)
        return queryResult.map { objectMapper.readTree(it.toJSON()) }
    }

    /**
     * queries all documents in a given corpus modified after a certain date
     */
    fun getModifiedAfter(modifiedAfter: Long, corpus: String): List<JsonNode> {
        val table = dynamoDB.getTable(tableName)
        val index = table.getIndex("Corpus-LastModified-index")
        val query = QuerySpec()
                .withKeyConditionExpression("Corpus = :corpus AND LastModified >= :modifiedAfter")
                .withValueMap(mapOf(":modifiedAfter" to modifiedAfter, ":corpus" to corpus))
        val queryResult = index.query(query)
        return queryResult.map { objectMapper.readTree(it.toJSON()) }
    }

}