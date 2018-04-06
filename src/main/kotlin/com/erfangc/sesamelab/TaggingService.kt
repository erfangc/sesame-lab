package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import java.util.*

class TaggingService(private val dynamoDB: AmazonDynamoDB) {
    private val table = ""
    fun tag(text: String, user: String, corpus: String, id: String?): Unit {
        id?.let {
            val getItemOutcome = dynamoDB.getItem(table, mapOf("id" to AttributeValue().withS(it)))
            getItemOutcome.item
        } ?: {
            PutItemRequest(table,
                    mapOf(
                            "id" to AttributeValue().withS(UUID.randomUUID().toString()),
                            "content" to AttributeValue().withS(text),
                            "createdBy" to AttributeValue().withS(user),
                            "createdOn" to AttributeValue().withN(System.nanoTime().toString()),
                            "lastModifiedBy" to AttributeValue().withS(user),
                            "lastModified" to AttributeValue().withN(System.nanoTime().toString()),
                            "corpus" to AttributeValue().withS(corpus)
                    )
            )
        }()
    }
}