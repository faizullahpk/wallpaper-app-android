package com.example.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DownloadEntity
import com.example.domain.repository.WallpaperRepository
import com.example.download.AndroidDownloadManagerOrchestrator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(
    private val repository: WallpaperRepository,
    private val orchestrator: AndroidDownloadManagerOrchestrator
) : ViewModel() {

    val downloads: StateFlow<List<DownloadEntity>> = repository.getDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startDownload(postId: String, mediaUrl: String, title: String, subreddit: String) {
        orchestrator.downloadMedia(postId, mediaUrl, title, subreddit)
    }

    fun removeDownload(downloadId: Long) {
        viewModelScope.launch {
            repository.deleteDownload(downloadId)
        }
    }

    class Factory(
        private val repository: WallpaperRepository,
        private val orchestrator: AndroidDownloadManagerOrchestrator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DownloadsViewModel(repository, orchestrator) as T
        }
    }
}
