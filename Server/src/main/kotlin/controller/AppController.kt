package com.nakaharadev.controller

import com.nakaharadev.model.request.AuthRequest
import com.nakaharadev.model.response.AuthResponse
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = [ "http://192.168.0.114:3000", "http://localhost:3000" ])
@RestController
@RequestMapping(value = ["/app"], produces = ["application/json"])
class AppController {
    @PostMapping("/auth/sign_in")
    fun signIn(@RequestBody body: AuthRequest.SignInRequest): AuthResponse? {
        /*
        val entity = userService.findByEmail(body.email) ?: return AuthResponse(404, "", "", "", "")

        return if (body.password.hashCode().toString() == entity.password) {
            AuthResponse(200, entity.id, entity.showId, entity.nickname, entity.characters)
        } else {
            AuthResponse(506, "", "", "", "")
        }*/

        return AuthResponse("404", "Not found", "")
    }

    @PostMapping("/auth/sign_up")
    fun singUp(@RequestBody body: AuthRequest.SignUpRequest): AuthResponse? {
        /*
        for (elem: UserEntity in userService.getAll()) {
            if (body.email == elem.email) {
                return AuthResponse(506, "", "", "", "")
            }
        }

        var entity = UserEntity(
            "",
            "",
            body.nickname,
            body.email,
            body.password.hashCode().toString(),
            "",
            ""
        )

        userService.save(entity)

        entity = userService.findByEmail(body.email)!!

        return AuthResponse(200, entity.id, entity.showId, entity.nickname, entity.characters)*/

        return AuthResponse("200", "Success", "1")
    }
}