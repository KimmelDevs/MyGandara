package com.pikacheat.mygandara.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.pikacheat.mygandara.model.sampleAnnouncements
import com.pikacheat.mygandara.ui.components.AnnouncementCard
import com.pikacheat.mygandara.ui.theme.MyGandaraTheme

private enum class HomeTab { ANNOUNCEMENTS, MY_REPORTS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(HomeTab.ANNOUNCEMENTS) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MyGandara") },
                actions = {
                    IconButton(onClick = { /* no backend yet */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onReportClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Report a problem") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == HomeTab.ANNOUNCEMENTS,
                        onClick = { selectedTab = HomeTab.ANNOUNCEMENTS },
                        label = { Text("Announcements") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = selectedTab == HomeTab.MY_REPORTS,
                        onClick = { selectedTab = HomeTab.MY_REPORTS },
                        label = { Text("My reports") }
                    )
                }
            }

            if (selectedTab == HomeTab.ANNOUNCEMENTS) {
                items(sampleAnnouncements) { announcement ->
                    AnnouncementCard(announcement = announcement)
                }
            } else {
                item {
                    Text(
                        text = "Your submitted reports will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MyGandaraTheme {
        HomeScreen()
    }
}
