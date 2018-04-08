package com.erfangc.sesamelab.corpus

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.erfangc.sesamelab.user.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.System.currentTimeMillis
import java.util.*

@Service
class CorpusBuilderService(private val amazonDynamoDB: AmazonDynamoDB,
                           private val dynamoDB: DynamoDB,
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
    fun put(id: String?,
            content: String,
            user: User,
            corpus: String
    ): String {
        val resolvedID = id ?: UUID.randomUUID().toString()
        val request = UpdateItemRequest()
                .withTableName(tableName)
                .withUpdateExpression(
                        "SET content = :content, " +
                                "corpus = :corpus, " +
                                "createdOn = if_not_exists(createdOn,:now), " +
                                "createdBy = if_not_exists(createdBy, :userID), " +
                                "createdByNickname = if_not_exists(createdByNickname,:userNickname), " +
                                "createdByEmail = if_not_exists(createdByEmail,:userEmail), " +
                                "lastModifiedOn = :now, " +
                                "lastModifiedBy = :userID, " +
                                "lastModifiedByNickname = :userNickname, " +
                                "lastModifiedByEmail = :userEmail "
                )
                .withKey(mapOf("id" to AttributeValue().withS(resolvedID)))
                .withExpressionAttributeValues(
                        mapOf(
                                ":content" to AttributeValue().withS(content),
                                ":userID" to AttributeValue().withS(user.id),
                                ":userEmail" to AttributeValue().withS(user.email),
                                ":userNickname" to AttributeValue().withS(user.nickname),
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