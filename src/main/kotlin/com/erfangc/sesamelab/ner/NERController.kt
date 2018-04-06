package com.erfangc.sesamelab.ner

import opennlp.tools.util.Span
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/ner")
class NERController(private val trainingService: NERService) {
    @PostMapping("train")
    fun train(@RequestParam corpus: String,
              @RequestParam(required = false) modelName: String?,
              @RequestParam(required = false) modifiedAfter: Long?): String {
        return trainingService.train(corpus = corpus, modifiedAfter = modifiedAfter ?: 0L, modelName = modelName ?: "default")
    }

    @GetMapping("{modelName}/run")
    fun run(@PathVariable modelName: String,
            @RequestParam sentence: String): Array<out Span>? {
        return trainingService.run(modelName = modelName, sentence = sentence)
    }

    @DeleteMapping("{modelName}")
    fun delete(@PathVariable modelName: String, @PathVariable corpus: String) {
        return trainingService.delete(modelName = modelName)
    }
}