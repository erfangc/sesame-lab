package com.erfangc.sesamelab

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.auth0.client.auth.AuthAPI
import com.zaxxer.hikari.HikariDataSource
import org.apache.http.HttpHeaders
import org.apache.http.HttpHost
import org.apache.http.message.BasicHeader
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Integer.parseInt
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
    Elasticsearch environment variables
     */
    private val esAuthorization = System.getenv("ES_AUTHORIZATION")
    private val esHost = System.getenv("ES_HOST")
    private val esPort = System.getenv("ES_PORT")
    private val esScheme = System.getenv("ES_SCHEME")

    @Bean
    fun restHighLevelClient(): RestHighLevelClient {
        return RestHighLevelClient(
                RestClient
                        .builder(
                                HttpHost(esHost, parseInt(esPort), esScheme)
                        )
                        .setDefaultHeaders(arrayOf(BasicHeader(HttpHeaders.AUTHORIZATION, esAuthorization)))
        )
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
    fun dataSource(): DataSource {
        val dbUri = URI(System.getenv("DATABASE_URL"))
        val username = dbUri.userInfo.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val password = dbUri.userInfo.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val jdbcUrl = "jdbc:postgresql://" + dbUri.host + ':'.toString() + dbUri.port + dbUri.path + "?sslmode=require"
        val dataSource = HikariDataSource()
        dataSource.username = username
        dataSource.jdbcUrl = jdbcUrl
        dataSource.password = password
        dataSource.driverClassName = "org.postgresql.Driver"
        return dataSource
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