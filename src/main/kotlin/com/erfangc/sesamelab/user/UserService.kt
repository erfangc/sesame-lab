package com.erfangc.sesamelab.user

import com.auth0.client.auth.AuthAPI
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class UserService(private val authAPI: AuthAPI) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    private val anon = User(id = "anonymous", email = "nobody@sesame-lab", nickname = "anonymous")

    fun getUser(principal: Principal?): User {
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