package com.nakaharadev.controller

import com.nakaharadev.database.entity.UserEntity
import com.nakaharadev.database.service.UserService
import com.nakaharadev.model.request.AuthRequest
import com.nakaharadev.model.response.AuthResponse
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = [ "http://192.168.0.114:3000", "http://localhost:3000" ])
@RestController
@RequestMapping(value = ["/app"], produces = ["application/json"])
class AppController(val userService: UserService) {
    @PostMapping("/auth/sign_in")
    fun signIn(@RequestBody body: AuthRequest.SignInRequest): AuthResponse? {
        val entity = userService.findByLogin(body.login) ?: return AuthResponse(404, "User not found", "", "")

        return if (body.password.hashCode().toString() == entity.password) {
            AuthResponse(200, "Success", entity.nickname, entity.uuid)
        } else {
            AuthResponse(506, "Invalid password", "", "")
        }
    }

    @PostMapping("/auth/sign_up")
    fun singUp(@RequestBody body: AuthRequest.SignUpRequest): AuthResponse? {

        for (elem: UserEntity in userService.getAll()) {
            if (body.login == elem.login) {
                return AuthResponse(506, "User already exists", "", "")
            }
        }

        var entity = UserEntity(
            "",
            "",
            body.nickname,
            body.login,
            body.password.hashCode().toString(),
            "",
            ""
        )

        userService.save(entity)

        entity = userService.findByLogin(body.login)!!

        return AuthResponse(200, "Success", entity.nickname, entity.uuid)
    }
}