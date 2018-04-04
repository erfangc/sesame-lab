package com.erfangc.sesamelab

import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSampleDataStream
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.util.PlainTextByLineStream
import opennlp.tools.util.TrainingParameters
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

@SpringBootApplication
class SesameLabApplication

fun main(args: Array<String>) {
    runApplication<SesameLabApplication>(*args)
}

@Configuration
class Configuration {
    private val logger = LoggerFactory.getLogger(com.erfangc.sesamelab.Configuration::class.java)
    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
            val lineStream = PlainTextByLineStream({ FileInputStream("./en-ner-train.txt") }, StandardCharsets.UTF_8)
            val sampleStream = NameSampleDataStream(lineStream)
            val model = NameFinderME.train(
                    "en",
                    "firm",
                    sampleStream,
                    TrainingParameters.defaultParams(),
                    TokenNameFinderFactory()
            )
            val outFile = File("./en-ner-model.bin")
            model.serialize(outFile)
            logger.info("Serialized output to ${outFile.absoluteFile}")
        }
    }
}