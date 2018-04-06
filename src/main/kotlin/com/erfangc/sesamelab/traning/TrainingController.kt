package com.erfangc.sesamelab.traning

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/train")
class TrainingController(private val trainingService: TrainingService) {
    @PostMapping
    fun train(@RequestParam modelName: String, @RequestParam corpus: String, @RequestParam modifiedAfter: Long?): String {
        return trainingService.train(corpus, modifiedAfter, modelName)
    }
}