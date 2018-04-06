package com.erfangc.sesamelab.corpus

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("api/v1/corpus/{corpus}")
class CorpusBuilderController(private val corpusBuilderService: CorpusBuilderService) {

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String) {
        corpusBuilderService.delete(id)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: String): JsonNode {
        return corpusBuilderService.getById(id)
    }

    @PostMapping
    fun put(@RequestBody content: String,
            @PathVariable corpus: String,
            @RequestParam id: String?,
            principal: Principal?): String {
        return corpusBuilderService.put(id, content, principal?.name ?: "anonymous", corpus)
    }

    @GetMapping("{creator}")
    fun getByCreator(@PathVariable creator: String, @PathVariable corpus: String): List<JsonNode>? {
        return corpusBuilderService.getByCreator(creator, corpus)
    }

    @GetMapping
    fun getModifiedAfter(@RequestParam modifiedAfter: Long, @PathVariable corpus: String): List<JsonNode> {
        return corpusBuilderService.getModifiedAfter(modifiedAfter, corpus)
    }

}