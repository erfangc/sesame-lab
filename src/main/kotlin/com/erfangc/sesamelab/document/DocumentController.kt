package com.erfangc.sesamelab.document

import com.erfangc.sesamelab.user.UserService
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@CrossOrigin
@RequestMapping("api/v1/documents")
class DocumentController(private val dynamoDBDocumentService: DynamoDBDocumentService,
                         private val elasticsearchDocumentService: ElasticsearchDocumentService,
                         private val userService: UserService) {

    @GetMapping("by-creator")
    fun byCreator(@RequestParam(required = false) creatorID: String?, principal: Principal?): List<Document> {
        return elasticsearchDocumentService.searchByCreator(creatorID = creatorID ?: principal?.name ?: "nobody")
    }

    @GetMapping("by-corpus/{corpusID}")
    fun byCorpus(@PathVariable corpusID: Long): List<Document> {
        return elasticsearchDocumentService.searchByCorpusID(corpusID = corpusID, modifiedAfter = null)
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String, principal: Principal?) {
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
        val document = dynamoDBDocumentService.getById(id)
        if (user.id != document.creatorID) {
            throw RuntimeException("you are not allowed to delete document $id")
        }
        dynamoDBDocumentService.delete(id)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: String): Document {
        return dynamoDBDocumentService.getById(id)
    }

    @PostMapping
    fun put(@RequestBody document: Document,
            principal: Principal?): Document {
        /*
        important that we override modified author fields
        based on authenticated principal and not user input
         */
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
        return dynamoDBDocumentService
                .put(document.copy(lastModifiedUserID = user.id, lastModifiedUserEmail = user.email))
    }

}