package com.erfangc.sesamelab.corpus

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.*
import java.security.Principal

data class Content(val body: String)

@RestController
@CrossOrigin
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
    fun put(@RequestBody content: Content,
            @PathVariable corpus: String,
            @RequestParam id: String?,
            principal: Principal?): String {
        return corpusBuilderService.put(id, content.body, principal?.name ?: "anonymous", corpus)
    }

    @GetMapping("by-creator")
    fun getByCreator(@RequestParam creator: String, @PathVariable corpus: String): List<JsonNode>? {
        return corpusBuilderService.getByCreator(creator, corpus)
    }

    @GetMapping("all")
    fun getModifiedAfter(@RequestParam modifiedAfter: Long, @PathVariable corpus: String): List<JsonNode> {
        return corpusBuilderService.getModifiedAfter(modifiedAfter, corpus)
    }

}