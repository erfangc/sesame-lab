package com.erfangc.sesamelab.corpus

import com.erfangc.sesamelab.user.UserService
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@CrossOrigin
@RequestMapping("api/v1/corpus/{corpus}")
class CorpusBuilderController(private val corpusBuilderService: CorpusBuilderService,
                              private val userService: UserService) {

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String, principal: Principal?) {
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
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
    fun put(@RequestBody document: Document,
            principal: Principal?): Document {
        /*
        important that we override modified author fields
        based on authenticated principal and not user input
         */
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
        return corpusBuilderService
                .put(document.copy(
                        lastModifiedBy = user.id,
                        lastModifiedByEmail = user.email,
                        lastModifiedByNickname = user.nickname)
                )
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