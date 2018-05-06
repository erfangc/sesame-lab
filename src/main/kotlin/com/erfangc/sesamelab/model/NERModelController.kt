package com.erfangc.sesamelab.model

import com.erfangc.sesamelab.model.entities.NERModel
import com.erfangc.sesamelab.model.repositories.NERModelRepository
import com.erfangc.sesamelab.user.User
import com.erfangc.sesamelab.user.UserService
import org.springframework.web.bind.annotation.*
import java.security.Principal

data class NERModelWithCreatorInfo(val model: NERModel, val user: User?)

@CrossOrigin
@RestController
@RequestMapping("api/v1/ner")
class ModelController(private val trainingService: NERModelService,
                      private val userService: UserService,
                      private val nerModelRepository: NERModelRepository) {

    @GetMapping("all-models")
    fun allModels(): List<NERModelWithCreatorInfo> {
        val allModels = nerModelRepository.findAll()
        val subs = allModels.map { it.userID }
        val userByID = userService.getUsers(subs).associateBy { it.id }
        return allModels.map { NERModelWithCreatorInfo(model = it, user = userByID[it.userID]) }
    }

    @PostMapping("train")
    fun train(@RequestParam corpusID: Long,
              @RequestParam(required = false) name: String?,
              @RequestParam(required = false) description: String?,
              @RequestParam(required = false) modifiedAfter: Long?, principal: Principal?): String {
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

    @GetMapping("{modelID}/run")
    fun run(@PathVariable modelID: String,
            @RequestParam sentence: String): String {
        return trainingService.run(modelID = modelID, sentence = sentence)
    }

    @DeleteMapping("{modelID}")
    fun delete(@PathVariable modelID: Long) {
        return trainingService.delete(id = modelID)
    }
}