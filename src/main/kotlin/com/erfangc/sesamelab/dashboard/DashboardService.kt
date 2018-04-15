package com.erfangc.sesamelab.dashboard

import com.erfangc.sesamelab.user.User
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.aggregations.support.ValueType
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Service

data class Dashboard(val topUsers: Map<String, Long>, val topCorpus: Map<String, Long>)

@Service
class SearchService(private val restHighLevelClient: RestHighLevelClient) {

    private val index = System.getenv("ES_INDEX")
    private val contributors = "contributors"
    private val corpus = "corpus"
    private val corpusField = "corpus.keyword"
    private val contributorsField = "createdByEmail.keyword"

    fun dashboard(user: User): Dashboard {
        // display top level stats
        val aggregationByContributor = TermsAggregationBuilder(contributors, ValueType.STRING)
                .field(contributorsField)
                .order(Terms.Order.count(false))
                .size(10)
                .minDocCount(1)
        val aggregationByCorpus = TermsAggregationBuilder(corpus, ValueType.STRING)
                .field(corpusField)
                .order(Terms.Order.count(false))
                .size(10)
                .minDocCount(1)

        val request = SearchRequest(index)
                .source(
                        SearchSourceBuilder()
                                .aggregation(aggregationByContributor)
                                .aggregation(aggregationByCorpus)
                )
        val response = restHighLevelClient.search(request)
        val aggByContributors: ParsedStringTerms = response.aggregations[contributors]
        val aggByCorpus: ParsedStringTerms = response.aggregations[corpus]
        return Dashboard(
                topCorpus = aggByCorpus.buckets.map { it.key.toString() to it.docCount }.toMap(),
                topUsers = aggByContributors.buckets.map { it.key.toString() to it.docCount }.toMap()
        )
    }
}