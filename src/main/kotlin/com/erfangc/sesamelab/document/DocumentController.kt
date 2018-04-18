package com.erfangc.sesamelab.document

import com.erfangc.sesamelab.user.UserService
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@CrossOrigin
@RequestMapping("api/v1/document")
class DocumentController(private val documentService: DocumentService,
                         private val userService: UserService) {

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String, principal: Principal?) {
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
        val document = documentService.getById(id)
        if (user.id != document.createdBy) {
            throw RuntimeException("you are not allowed to delete document $id")
        }
        documentService.delete(id)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: String): Document {
        return documentService.getById(id)
    }

    @PostMapping
    fun put(@RequestBody document: Document,
            principal: Principal?): Document {
        /*
        important that we override modified author fields
        based on authenticated principal and not user input
         */
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
        return documentService
                .put(document.copy(
                        lastModifiedBy = user.id,
                        lastModifiedByEmail = user.email,
                        lastModifiedByNickname = user.nickname)
                )
    }

    @GetMapping("by-creator")
    fun getByCreator(@RequestParam(required = false) creator: String?, @RequestParam corpusID: String, principal: Principal?): List<Document>? {
        return documentService.getByCreator(creator ?: principal?.name ?: "anonymous", corpusID)
    }

}