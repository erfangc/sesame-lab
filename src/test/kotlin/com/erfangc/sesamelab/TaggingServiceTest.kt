package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemResult
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.lang.System.nanoTime

class TaggingServiceTest {

    @Test
    fun tag() {
        val mock = mock<AmazonDynamoDB> {
            on { getItem(anyString(), any()) } doReturn GetItemResult().withItem(
                    mapOf(
                            "createdOn" to AttributeValue().withN(nanoTime().toString()),
                            "createdBy" to AttributeValue().withS("kyle")
                    )
            )
            on { putItem(any()) } doReturn PutItemResult()
        }
        val service = TaggingService(mock)
        val id = service.tag(id = "123", corpus = "news", text = "<START:foo> Foo <END> bar", user = "joe")
        assertEquals("123", id)
    }
}