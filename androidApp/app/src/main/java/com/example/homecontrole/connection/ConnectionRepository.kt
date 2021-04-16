package com.example.homecontrole.connection

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ConnectionRepository constructor(
    private val dao: ConnectionDao
)  {

    val allConnections: Flow<List<ConnectionEntity>> = dao.getAll()

    fun insert(connection: ConnectionEntity)
    {
        GlobalScope.launch{
            if ( !dao.exists(connection.url, connection.port) )
                dao.insert(connection)
        }
    }

    fun remove(connection: ConnectionEntity)
    {
        GlobalScope.launch {
            dao.delete(connection)
        }
    }
}