package com.example.ui.screens.subreddits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SubredditEntity
import com.example.domain.usecase.SubredditUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubredditViewModel(
    private val subredditUseCase: SubredditUseCase
) : ViewModel() {

    val subreddits: StateFlow<List<SubredditEntity>> = subredditUseCase.getSubreddits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubreddit(name: String) {
        viewModelScope.launch {
            subredditUseCase.addSubreddit(name)
        }
    }

    fun removeSubreddit(name: String) {
        viewModelScope.launch {
            subredditUseCase.removeSubreddit(name)
        }
    }

    fun toggleSubreddit(sub: SubredditEntity) {
        viewModelScope.launch {
            subredditUseCase.toggleSubreddit(sub)
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            subredditUseCase.resetDefaults()
        }
    }

    fun bulkImport(csv: String) {
        viewModelScope.launch {
            csv.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { name ->
                    subredditUseCase.addSubreddit(name, isCustom = true)
                }
        }
    }

    fun exportToCsv(): String {
        return subreddits.value.joinToString(",") { it.name }
    }

    fun moveSubredditUp(index: Int) {
        if (index <= 0) return
        viewModelScope.launch {
            val list = subreddits.value.toMutableList()
            val temp = list[index]
            list[index] = list[index - 1]
            list[index - 1] = temp
            subredditUseCase.reorderSubreddits(list)
        }
    }

    fun moveSubredditDown(index: Int) {
        if (index >= subreddits.value.size - 1) return
        viewModelScope.launch {
            val list = subreddits.value.toMutableList()
            val temp = list[index]
            list[index] = list[index + 1]
            list[index + 1] = temp
            subredditUseCase.reorderSubreddits(list)
        }
    }

    class Factory(
        private val subredditUseCase: SubredditUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubredditViewModel(subredditUseCase) as T
        }
    }
}
