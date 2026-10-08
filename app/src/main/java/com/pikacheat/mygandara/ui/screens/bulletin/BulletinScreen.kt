package com.pikacheat.mygandara.ui.screens.bulletin

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pikacheat.mygandara.data.model.PostDto
import com.pikacheat.mygandara.data.model.PostType
import com.pikacheat.mygandara.ui.components.EmptyListText
import com.pikacheat.mygandara.ui.components.PostCard
import com.pikacheat.mygandara.ui.components.RefreshOnResume
import com.pikacheat.mygandara.ui.components.UiStateContent
import com.pikacheat.mygandara.ui.viewmodel.BulletinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulletinScreen(
    canPost: Boolean,
    onNewPostClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BulletinViewModel = viewModel(factory = BulletinViewModel.Factory)
) {
    val state by viewModel.posts.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.posts.isRefreshing.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<PostDto?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    RefreshOnResume { viewModel.posts.refreshIfLoaded() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Gandara bulletin") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canPost) {
                ExtendedFloatingActionButton(
                    onClick = onNewPostClick,
                    icon = { Icon(Icons.Filled.Campaign, contentDescription = null) },
                    text = { Text("New post") }
                )
            }
        }
    ) { innerPadding ->
        UiStateContent(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.posts.refresh() },
            modifier = Modifier.padding(innerPadding)
        ) { posts ->
            val visible = if (filter == null) posts else posts.filter { it.type == filter }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp)
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = filter == null,
                                onClick = { viewModel.setFilter(null) },
                                label = { Text("All") }
                            )
                        }
                        items(PostType.entries) { type ->
                            FilterChip(
                                selected = filter == type,
                                onClick = { viewModel.setFilter(type) },
                                label = { Text(type.label) }
                            )
                        }
                    }
                }
                if (visible.isEmpty()) {
                    item { EmptyListText("No posts yet.") }
                }
                items(visible, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        attachmentUrl = viewModel.attachmentUrl(post),
                        onOpenAttachment = { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        },
                        onDelete = if (canPost) ({ pendingDelete = post }) else null
                    )
                }
            }
        }
    }

    pendingDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete post?") },
            text = { Text("\"${post.title}\" will be removed from the bulletin for everyone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePost(post)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}
