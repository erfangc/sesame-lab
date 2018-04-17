package com.erfangc.sesamelab.user

import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.UserFilter
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.Principal

/**
 * this service is used by REST controllers, we assume the security interceptors are configured to authenticate the principal
 *
 * never use this service in an interceptor, servlet filter, as at that point the principal may not have been authenticated
 */
@Service
class UserService(private val authAPI: AuthAPI) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)
    private val anon = User(id = "anonymous", email = "nobody@sesame-lab", nickname = "anonymous")
    private val issuer = System.getenv("AUTH0_ISSUER")
    private val audience = System.getenv("AUTH0_MANAGEMENT_AUDIENCE")

    fun getUsers(subs: List<String>): List<User> {
        /*
        TODO consider perhaps not requesting a new token per request, but check expiration before each request
         */
        val managementAudience = audience
        val tokenHolder = authAPI.requestToken(managementAudience).execute()
        val usersEntity = ManagementAPI(issuer, tokenHolder.accessToken).users()
        return try {
            val query = subs.map { "id=\"$it\"" }.joinToString(" OR ")
            val userPage = usersEntity.list(UserFilter().withQuery(query).withFields("user_id,email,nickname", true)).execute()
            userPage.items.map { User(id = it.id, email = it.email, nickname = it.nickname) }
        } catch (e: Exception) {
            /*
            we would fail to receive a user object if the subject was a machine
             */
            logger.error(e.message)
            emptyList<User>()
        }
    }

    /**
     * this method uses the ManagementAPI to search a user by ID
     * this is not the same as obtaining the currently authenticated principal from an accessToken
     */
    fun getUserByID(sub: String): User? {
        /*
        TODO consider perhaps not requesting a new token per request, but check expiration before each request
         */
        val managementAudience = audience
        val tokenHolder = authAPI.requestToken(managementAudience).execute()
        val users = ManagementAPI(issuer, tokenHolder.accessToken).users()
        val userFilter = UserFilter().withFields("id,nickname,email", true)
        try {
            val user = users.get(sub, userFilter).execute()
            return User(id = user.id, nickname = user.nickname, email = user.email)
        } catch (e: Exception) {
            /*
            we would fail to receive a user object if the subject was a machine
             */
            logger.error(e.message)
            return null
        }
    }

    fun getUserFromAuthenticatedPrincipal(principal: Principal?): User {
        return if (principal == null) {
            anon
        } else {
            if (principal is AuthenticationJsonWebToken) {
                /*
                if "gty" -> "client-credentials" that means this is an API machine user
                 */
                val decodedJWT = principal.details as DecodedJWT
                if (decodedJWT.claims["gty"]?.asString() == "client-credentials") {
                    return User(
                            id = principal.name,
                            email = "no_email",
                            nickname = "no_nickname"
                    )
                } else {
                    val userInfo = authAPI.userInfo(principal.token).execute()
                    val values = userInfo.values
                    User(
                            id = principal.name,
                            email = values["email"]?.toString() ?: "no_email",
                            nickname = values["nickname"]?.toString() ?: "no_nickname"
                    )
                }
            } else {
                logger.debug("Cannot validate principal, only AuthenticationJsonWebToken is supported")
                anon
            }
        }
    }
}