package com.erfangc.sesamelab.corpus

import com.erfangc.sesamelab.user.UserService
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.*
import java.security.Principal

data class Content(val body: String)

@RestController
@CrossOrigin
@RequestMapping("api/v1/corpus/{corpus}")
class CorpusBuilderController(private val corpusBuilderService: CorpusBuilderService,
                              private val userService: UserService) {

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String, principal: Principal?) {
        val user = userService.getUser(principal)
        val document = corpusBuilderService.getById(id)
        if (user.id != document.createdBy) {
            throw RuntimeException("you are not allowed to delete document $id")
        }
        corpusBuilderService.delete(id)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: String): Document {
        return corpusBuilderService.getById(id)
    }

    @PostMapping
    fun put(@RequestBody content: Content,
            @PathVariable corpus: String,
            @RequestParam id: String?,
            principal: Principal?): String {
        return corpusBuilderService.put(id = id, content = content.body, user = userService.getUser(principal), corpus = corpus)
    }

    @GetMapping("by-creator")
    fun getByCreator(@RequestParam(required = false) creator: String?, @PathVariable corpus: String, principal: Principal?): List<Document>? {
        return corpusBuilderService.getByCreator(creator ?: principal?.name ?: "anonymous", corpus)
    }

    @GetMapping("all")
    fun getModifiedAfter(@RequestParam modifiedAfter: Long, @PathVariable corpus: String): List<Document> {
        return corpusBuilderService.getModifiedAfter(modifiedAfter, corpus)
    }

}