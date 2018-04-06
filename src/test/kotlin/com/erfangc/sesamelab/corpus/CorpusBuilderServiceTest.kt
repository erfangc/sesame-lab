package com.erfangc.sesamelab.corpus

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
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
        val amazonDynamoDB = mock<AmazonDynamoDB> {

        }
        val dynamoDB = mock<DynamoDB> {

        }
        val service = CorpusBuilderService(amazonDynamoDB = amazonDynamoDB, dynamoDB = dynamoDB)
        val id = service.put(id = "123", corpus = "news", content = "<START:foo> Foo <END> bar", user = "joe")
        assertEquals("123", id)
    }
}