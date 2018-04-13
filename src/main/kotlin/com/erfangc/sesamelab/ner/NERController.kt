package com.erfangc.sesamelab.ner

import com.erfangc.sesamelab.user.UserService
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin
@RestController
@RequestMapping("api/v1/ner")
class NERController(private val trainingService: NERService, private val userService: UserService) {

    @GetMapping("all-models")
    fun allModels(): List<NERModel> {
        return trainingService.allModels()
    }

    @PostMapping("train")
    fun train(@RequestParam corpus: String,
              @RequestParam(required = false) modelName: String?,
              @RequestParam(required = false) modelDescription: String?,
              @RequestParam(required = false) modifiedAfter: Long?, principal: Principal?): String {
        val user = userService.getUser(principal)
        val request = TrainModelRequest(
                corpus = corpus,
                modifiedAfter = modifiedAfter ?: 0L,
                modelName = modelName ?: "default",
                user = user,
                modelDescription = modelDescription
        )
        return trainingService.train(request)
    }

    @GetMapping("{modelID}/run")
    fun run(@PathVariable modelID: String,
            @RequestParam sentence: String): String {
        return trainingService.run(modelID = modelID, sentence = sentence)
    }

    @DeleteMapping("{modelID}")
    fun delete(@PathVariable modelID: String) {
        return trainingService.delete(modelID = modelID)
    }
}