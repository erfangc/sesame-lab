package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaggingService(private val dynamoDB: AmazonDynamoDB) {

    private val table = ""

    /**
     * tries to upsert the record into the database
     * returns the unique id of the record
     */
    fun tag(id: String?,
            text: String,
            user: String,
            corpus: String
    ): String {
        return id?.let {
            val getItemResult = dynamoDB.getItem(table, mapOf("id" to AttributeValue().withS(it)))
            val request = PutItemRequest(table,
                    mapOf(
                            "id" to AttributeValue().withS(it),
                            "content" to AttributeValue().withS(text),
                            "createdBy" to AttributeValue().withS(getItemResult.item.getValue("createdBy").s),
                            "createdOn" to AttributeValue().withN(getItemResult.item.getValue("createdOn").n),
                            "lastModifiedBy" to AttributeValue().withS(user),
                            "lastModified" to AttributeValue().withN(System.nanoTime().toString()),
                            "corpus" to AttributeValue().withS(corpus)
                    )
            )
            dynamoDB.putItem(request)
            it
        } ?: {
            val newID = UUID.randomUUID().toString()
            val request = PutItemRequest(table,
                    mapOf(
                            "id" to AttributeValue().withS(newID),
                            "content" to AttributeValue().withS(text),
                            "createdBy" to AttributeValue().withS(user),
                            "createdOn" to AttributeValue().withN(System.nanoTime().toString()),
                            "lastModifiedBy" to AttributeValue().withS(user),
                            "lastModified" to AttributeValue().withN(System.nanoTime().toString()),
                            "corpus" to AttributeValue().withS(corpus)
                    )
            )
            dynamoDB.putItem(request)
            newID
        }()
    }

}