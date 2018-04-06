package com.erfangc.sesamelab.ner

import opennlp.tools.util.Span
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/ner")
class NERController(private val trainingService: NERService) {
    @PostMapping("train")
    fun train(@RequestParam type: String,
              @RequestParam corpus: String,
              @RequestParam(required = false) modelName: String = "default",
              @RequestParam(required = false) modifiedAfter: Long = 0): String {
        return trainingService.train(corpus = corpus, type = type, modifiedAfter = modifiedAfter, modelName = modelName)
    }

    @GetMapping("run")
    fun run(@RequestParam modelName: String,
            @RequestParam type: String,
            @RequestParam corpus: String,
            @RequestParam sentence: String): Array<out Span>? {
        return trainingService.run(modelName = modelName, type = type, corpus = corpus, sentence = sentence)
    }

    @DeleteMapping("{modelName}/{corpus}/{type}")
    fun delete(@PathVariable modelName: String, @PathVariable corpus: String, @RequestParam type: String) {
        return trainingService.delete(modelName = modelName, corpus = corpus, type = type)
    }
}