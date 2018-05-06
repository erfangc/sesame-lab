package com.erfangc.sesamelab.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ElasticsearchDocumentService(private val highLevelClient: RestHighLevelClient,
                                   private val objectMapper: ObjectMapper) {
    private val index = "ner_model_documents"

    fun searchByCorpusID(corpusID: Long,
                         modifiedAfter: Instant?): List<Document> {
        val boolQueryBuilder = BoolQueryBuilder().filter(TermQueryBuilder("corpusID", corpusID))
        modifiedAfter?.run { boolQueryBuilder.filter(RangeQueryBuilder("lastModifiedOn").gte(this)) }
        val searchRequest = SearchRequest(
                arrayOf(index),
                SearchSourceBuilder()
                        .query(
                                boolQueryBuilder
                        )
        )
        val searchResponse = highLevelClient.search(searchRequest)
        return searchResponse.hits.map { objectMapper.readValue<Document>(it.sourceAsString) }
    }

    fun searchByCreator(creatorID: String): List<Document> {
        val searchRequest = SearchRequest(
                arrayOf(index),
                SearchSourceBuilder()
                        .query(
                                BoolQueryBuilder()
                                        .filter(TermQueryBuilder("creatorID", creatorID))
                        )
        )
        val searchResponse = highLevelClient.search(searchRequest)
        return searchResponse.hits.map { objectMapper.readValue<Document>(it.sourceAsString) }
    }

}