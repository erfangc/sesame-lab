package com.erfangc.sesamelab.ner

import opennlp.tools.util.Span
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/ner/{modelName}/{corpus}/{type}")
class NERController(private val trainingService: NERService) {
    @PostMapping
    fun train(@PathVariable type: String,
              @PathVariable corpus: String,
              @PathVariable modelName: String,
              @RequestParam(required = false) modifiedAfter: Long = 0): String {
        return trainingService.train(corpus = corpus, type = type, modifiedAfter = modifiedAfter, modelName = modelName)
    }

    @GetMapping("run")
    fun run(@PathVariable modelName: String,
            @PathVariable type: String,
            @PathVariable corpus: String,
            @RequestParam sentence: String): Array<out Span>? {
        return trainingService.run(modelName = modelName, type = type, corpus = corpus, sentence = sentence)
    }

    @DeleteMapping("{modelName}/{corpus}/{type}")
    fun delete(@PathVariable modelName: String, @PathVariable corpus: String, @RequestParam type: String) {
        return trainingService.delete(modelName = modelName, corpus = corpus, type = type)
    }
}