package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.PostDto
import com.pikacheat.mygandara.data.model.PostType
import com.pikacheat.mygandara.data.remote.Tables
import com.pikacheat.mygandara.data.repository.PostRepository
import com.pikacheat.mygandara.data.repository.RealtimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BulletinViewModel(
    private val postRepository: PostRepository,
    realtimeRepository: RealtimeRepository
) : ViewModel() {

    val posts = Loadable(viewModelScope) { postRepository.getPosts() }

    /** null = all types. */
    private val _filter = MutableStateFlow<PostType?>(null)
    val filter: StateFlow<PostType?> = _filter.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        posts.refresh()
        realtimeRepository.tableChanges(Tables.POSTS)
            .onEach { posts.refresh(showIndicator = false) }
            .catch { /* live updates are best-effort; pull to refresh still works */ }
            .launchIn(viewModelScope)
    }

    fun setFilter(type: PostType?) {
        _filter.value = type
    }

    fun attachmentUrl(post: PostDto): String? = post.attachmentPath?.let(postRepository::attachmentUrl)

    fun deletePost(post: PostDto) {
        viewModelScope.launch {
            _message.value = runAction { postRepository.deletePost(post.id) } ?: "Post deleted"
            posts.refresh(showIndicator = false)
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        val Factory = appViewModelFactory { BulletinViewModel(it.postRepository, it.realtimeRepository) }
    }
}
