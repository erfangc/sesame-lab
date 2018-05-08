package com.erfangc.sesamelab.model

import com.erfangc.sesamelab.model.repositories.NERModelRepository
import com.erfangc.sesamelab.user.UserService
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin
@RestController
@RequestMapping("api/v1/ner")
class ModelController(private val trainingService: NERModelService,
                      private val userService: UserService,
                      private val nerModelRepository: NERModelRepository) {

    @PostMapping("train")
    fun train(@RequestParam corpusID: Long,
              @RequestParam(required = false) name: String?,
              @RequestParam(required = false) description: String?,
              @RequestParam(required = false) modifiedAfter: Long?,
              principal: Principal?): String {
        val user = userService.getUserFromAuthenticatedPrincipal(principal)
        val request = TrainNERModelRequest(
                corpusID = corpusID,
                modifiedAfter = modifiedAfter ?: 0L,
                name = name ?: "default",
                user = user,
                description = description
        )
        return trainingService.train(request)
    }

    @GetMapping("{modelFilename}/run")
    fun run(@PathVariable modelFilename: String,
            @RequestParam sentence: String): String {
        return trainingService.run(modelFilename = modelFilename, sentence = sentence)
    }

}