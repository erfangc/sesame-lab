package mock.providers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import objectMapper
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.text.Text
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.internal.InternalSearchResponse
import java.util.*

/**
 * mocks a response made to ES REST client
 */
fun mockResponseFromResource(resource: String): SearchResponse {
    val sources = objectMapper.readValue<List<JsonNode>>(
            SearchResponse::class.java.classLoader.getResource(resource)
    )
    val searchHits = sources.map {
        val searchHit = SearchHit(Random().nextInt(), UUID.randomUUID().toString(), Text("sample"), null)
        val bytesArray = BytesArray(it.toString())
        searchHit.sourceRef(bytesArray)
        searchHit.score(1f)
        searchHit
    }
    val internalSearchHits = SearchHits(searchHits.toTypedArray(), 2, 1f)
    val internalSearchResponse = InternalSearchResponse(
            internalSearchHits,
            null,
            null,
            null,
            false,
            false,
            0
    )
    return SearchResponse(
            internalSearchResponse,
            "empty",
            1,
            1,
            0,
            1,
            emptyArray()
    )
}
