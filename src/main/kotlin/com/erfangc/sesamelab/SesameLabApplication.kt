package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class SesameLabApplication

fun main(args: Array<String>) {
    runApplication<SesameLabApplication>(*args)
}

@Configuration
class Configuration {
    @Bean
    fun dynamoDB(): AmazonDynamoDB {
        return AmazonDynamoDBClientBuilder.defaultClient()
    }
    @Bean
    fun amazonS3(): AmazonS3 {
        return AmazonS3ClientBuilder.defaultClient()
    }
}