package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
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
    fun dynamoDB(): AmazonDynamoDB {
        return AmazonDynamoDBClientBuilder.defaultClient()
    }
}