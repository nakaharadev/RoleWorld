package com.nakaharadev.database.service

import com.nakaharadev.database.entity.UserEntity
import com.nakaharadev.database.repository.IUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {
    @Autowired
    private lateinit var repository: IUserRepository

    fun save(entity: UserEntity) {
        repository.save(entity)
    }

    fun getAll(): List<UserEntity> {
        return repository.findAll()
    }

    fun findByLogin(login: String): UserEntity? {
        return repository.findByLogin(login)
    }
}