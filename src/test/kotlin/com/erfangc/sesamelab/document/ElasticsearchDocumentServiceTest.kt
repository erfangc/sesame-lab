package com.erfangc.sesamelab.document

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mock.providers.mockResponseFromResource
import objectMapper
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ElasticsearchDocumentServiceTest {

    @Test
    fun searchByCorpusID() {
        val restHighLevelClient = mockk<RestHighLevelClient>()
        every { restHighLevelClient.search(any()) }.returns(mockResponseFromResource("elasticsearch/documents/sample.json"))
        val svc = ElasticsearchDocumentService(restHighLevelClient, objectMapper)
        val modifiedAfter = LocalDate.of(2017, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        val documents: List<Document> = svc.searchByCorpusID(corpusID = 1, modifiedAfter = modifiedAfter)
        assertEquals(2, documents.size)
        documents.forEach {
            assertEquals("sample", it.content)
            assertNotNull(it.entities)
        }
        verify {
            restHighLevelClient.search(match {
                val query = it.source().query()
                query is BoolQueryBuilder
                        && query
                        .filter()
                        .find { qb ->
                            qb is RangeQueryBuilder && qb.fieldName() == "lastModifiedOn" && qb.from().toString() == modifiedAfter.toString()
                        } != null
                        && query
                        .filter()
                        .find { qb ->
                            qb is TermQueryBuilder && qb.fieldName() == "corpusID" && qb.value() == 1L
                        } != null
            })
        }
    }

    @Test
    fun searchByCreator() {
        val restHighLevelClient = mockk<RestHighLevelClient>()
        every { restHighLevelClient.search(any()) }.returns(mockResponseFromResource("elasticsearch/documents/sample.json"))
        val svc = ElasticsearchDocumentService(restHighLevelClient, objectMapper)
        val documents = svc.searchByCreator("xiongxiong")
        assertEquals(2, documents.size)
        /*
        validate that the correct query was built
         */
        verify {
            restHighLevelClient.search(match {
                val query = it.source().query()
                query is BoolQueryBuilder
                        && query.filter().find { qb -> qb is TermQueryBuilder && qb.fieldName() == "creatorID" && qb.value() == "xiongxiong" } != null
            })
        }
    }

}