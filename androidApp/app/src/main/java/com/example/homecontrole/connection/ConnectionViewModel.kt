package com.example.homecontrole.connection

import androidx.lifecycle.*
import javax.inject.Inject

class ConnectionViewModel @Inject constructor(
    private val repo: ConnectionRepository
) : ViewModel()
{
    val connections: LiveData<List<ConnectionEntity>> = repo.allConnections.asLiveData()

    fun remove(connectionEntity: ConnectionEntity) { repo.remove(connectionEntity) }

    fun insert(connectionEntity: ConnectionEntity) { repo.insert(connectionEntity) }
}

class ConnectionViewModelFactory(private val repository: ConnectionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}