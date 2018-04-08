package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.auth0.client.auth.AuthAPI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SesameLabApplication

fun main(args: Array<String>) {
    runApplication<SesameLabApplication>(*args)
}

@RestController
class HeartBeatController {
    @GetMapping("/")
    fun get(): String {
        return "ok"
    }
}

@Configuration
class Configuration {
    private val region = System.getenv("AWS_REGION")
    private val issuer = System.getenv("AUTH0_ISSUER")
    private val clientId = System.getenv("AUTH0_CLIENT_ID")
    private val clientSecret = System.getenv("AUTH0_CLIENT_SECRET")

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(region)
                .build()
    }

    @Bean
    fun amazonS3(): AmazonS3 {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build()
    }

    @Bean
    fun dynamoDB(amazonDynamoDB: AmazonDynamoDB): DynamoDB {
        return DynamoDB(amazonDynamoDB)
    }

    @Bean
    fun authAPI(): AuthAPI {
        return AuthAPI(issuer, clientId, clientSecret)
    }
}