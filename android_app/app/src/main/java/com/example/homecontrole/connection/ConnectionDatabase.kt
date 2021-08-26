package com.example.homecontrole.connection

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
    fun getAll(): Flow<List<ConnectionEntity>>

    @Query("SELECT EXISTS(SELECT * FROM ${ConnectionEntity.TABLE_NAME} where url = :url AND port = :port)")
    fun exists(url: String, port: String): Boolean

    @Insert
    fun insert(vararg connections: ConnectionEntity)

    @Delete
    fun delete(entity: ConnectionEntity)
}

@Database(entities = [ConnectionEntity::class], version = 1)
abstract class ConnectionDatabase : RoomDatabase()
{
    abstract fun connectionDao(): ConnectionDao

    companion object {
        @Volatile
        private var INSTANCE: ConnectionDatabase? = null

        fun getInstance(context: Context): ConnectionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConnectionDatabase::class.java,
                    "word_database"
                ).build()

                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}