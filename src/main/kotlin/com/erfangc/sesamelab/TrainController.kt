package com.erfangc.sesamelab

import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSampleDataStream
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.TrainingParameters
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("api/v1")
class TrainController {
    @GetMapping("train")
    fun train(): Unit {

    }
}