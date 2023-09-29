package com.wafflestudio.seminar.spring2023.user.controller

import com.wafflestudio.seminar.spring2023.user.service.SignUpUsernameConflictException
import com.wafflestudio.seminar.spring2023.user.service.User
import com.wafflestudio.seminar.spring2023.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/api/v1/signup")
    fun signup(
        @RequestBody request: SignUpRequest,
    ): ResponseEntity<Unit> {
        try{
            userService.signUp(
                username = request.username,
                password = request.password,
                image = request.image
            )
        } catch(e: SignUpUsernameConflictException){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch(e: RuntimeException){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/signin")
    fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> {
        val user: User
        try{
            user = userService.signIn(
                username = request.username,
                password = request.password
            )
        } catch(e: RuntimeException){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        val response = SignInResponse(user.getAccessToken())
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/v1/users/me")
    fun me(
        @RequestHeader(name = "Authorization", required = false) authorizationHeader: String?,
    ): ResponseEntity<UserMeResponse> {
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val token = authorizationHeader.split(" ")[1]
        val user: User
        try{ user = userService.authenticate(token) }
        catch(e: RuntimeException){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val response = UserMeResponse(username = user.username, image = user.image)
        return ResponseEntity.ok(response)
    }
}

data class UserMeResponse(
    val username: String,
    val image: String,
)

data class SignUpRequest(
    val username: String,
    val password: String,
    val image: String,
)

data class SignInRequest(
    val username: String,
    val password: String,
)

data class SignInResponse(
    val accessToken: String,
)
