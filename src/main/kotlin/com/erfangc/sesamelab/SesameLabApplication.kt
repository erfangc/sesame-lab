package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.auth0.client.auth.AuthAPI
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import javax.sql.DataSource

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
    /*
    we are running on Heroku instead of directly on AWS EC2 or EBS
    we need to specify region specifically for S3 and DynamoDB instead of relying on instance profile
     */
    private val region = System.getenv("AWS_REGION")
    /*
    needed to validate JWT tokens issued by our trusted authorization provider
     */
    private val issuer = System.getenv("AUTH0_ISSUER")
    /*
    client ID representing this server as an OAuth 2 client (this is NOT the same Client ID as the UI)
     */
    private val clientId = System.getenv("AUTH0_CLIENT_ID")
    private val clientSecret = System.getenv("AUTH0_CLIENT_SECRET")
    /*
    SQL database connection string is injected by Heroku at run time, for development we can use whatever
     */
    private val clearDBDatabaseURL = System.getenv("CLEARDB_DATABASE_URL")

    @Bean
    @Primary
    fun dataSource(): DataSource {
        // TODO add SSL to ensure secure connection to MySQL, but we are not storing anything interesting for now
        val dbUri = URI(clearDBDatabaseURL)
        val userInfo = dbUri.userInfo.split(":")
        val configuration = HikariConfig()
        configuration.jdbcUrl = "jdbc:mysql://${dbUri.host}${dbUri.path}"
        configuration.username = userInfo[0]
        configuration.password = userInfo[1]
        return HikariDataSource(configuration)
    }

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

    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }
}