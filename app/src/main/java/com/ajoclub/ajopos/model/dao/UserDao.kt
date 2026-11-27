package com.ajoclub.ajopos.model.dao

import androidx.room.*
import com.ajoclub.ajopos.model.data.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM user WHERE username = :username AND password = :password LIMIT 1")
    fun login(username: String, password: String): User?

    @Query("SELECT * FROM user")
    fun getAllUsers(): List<User>

    @Update
    fun updateUser(user: User)

    @Query("DELETE FROM user")
    fun clearUsers()
}
