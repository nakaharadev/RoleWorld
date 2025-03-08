package com.nakaharadev.database.entity

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @Column(name = "id")
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    var uuid: String,

    @Column(name = "showId")
    var showId: String = "",

    @Column(name = "nickname")
    var nickname: String,

    @Column(name = "email")
    var login: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "avatar")
    val avatar: String,

    @Column(name = "characters")
    val characters: String = ""
) {
    constructor() : this("", "", "", "", "", "", "")

    override fun toString(): String {
        return "UserEntity {\n" +
                "\tuuid='$uuid'\n" +
                "\tshowId='$uuid'\n" +
                "\tnickname='$nickname'\n" +
                "\tlogin='$login'\n" +
                "\tpassword='$password'\n" +
                "\tavatar='$avatar'\n" +
                "\tcharacters=$characters\n" +
                "}"
    }
}