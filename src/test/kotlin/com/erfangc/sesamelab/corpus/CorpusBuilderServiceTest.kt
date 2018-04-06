package com.erfangc.sesamelab.corpus

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemResult
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.lang.System.nanoTime

class CorpusBuilderServiceTest {

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
        val service = CorpusBuilderService(mock)
        val id = service.put(id = "123", corpus = "news", content = "<START:foo> Foo <END> bar", user = "joe")
        assertEquals("123", id)
    }
}