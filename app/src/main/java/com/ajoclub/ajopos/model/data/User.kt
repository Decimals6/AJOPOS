package com.ajoclub.ajopos.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val username: String,
    val password: String,
    val role: String,
    val created_at: Long,
    val updated_at: Long,
    val last_login: Long
)
