package com.nakaharadev.database.repository

import com.nakaharadev.database.entity.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface IUserRepository : JpaRepository<UserEntity, String> {
    @Query("SELECT u FROM UserEntity u WHERE u.login = :login")
    fun findByLogin(@Param("login") login: String): UserEntity?

    /*
    @Transactional
    @Modifying
    @Query("UPDATE UserEntity u SET u.avatar = :name WHERE u.id = :id")
    fun saveAvatar(@Param("id") id: String, @Param("name") name: String)

    @Transactional
    @Modifying
    @Query("UPDATE UserEntity u SET u.nickname = :value WHERE u.id = :id")
    fun updateNickname(@Param("id") id: String, @Param("value") name: String)

    @Transactional
    @Modifying
    @Query("UPDATE UserEntity u SET u.characters = :value WHERE u.id = :id")
    fun setCharacters(@Param("id") id: String, @Param("value") name: String)


    @Transactional
    @Modifying
    @Query("UPDATE UserEntity c SET c.showId = :showId WHERE c.id = :id")
    fun updateShowingId(@Param("id") id: String, @Param("showId") showId: String)*/
}