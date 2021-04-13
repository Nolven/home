package com.example.homecontrole

import androidx.room.*

@Entity(primaryKeys = ["url", "port"], tableName = "connections")
data class ConnectionEntity(
    val url: String,
    val port: String)
{
    companion object {
        const val TABLE_NAME = "connections"
    }
}

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM ${ConnectionEntity.TABLE_NAME}")
    fun getAll(): List<ConnectionEntity>

    @Query("SELECT EXISTS(SELECT * FROM ${ConnectionEntity.TABLE_NAME} where url = :url AND port = :port)")
    fun exists(url: String, port: String): Boolean

    @Insert
    fun insert(vararg connections: ConnectionEntity)

    @Delete
    fun delete(entity: ConnectionEntity)
}

@Database(entities = [ConnectionEntity::class], version = 1)
abstract class ConnectionDatabase : RoomDatabase(){
    abstract fun connectionDao(): ConnectionDao
}