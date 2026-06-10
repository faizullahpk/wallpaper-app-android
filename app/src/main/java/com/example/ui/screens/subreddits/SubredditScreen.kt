package com.example.ui.screens.subreddits

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SubredditEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditScreen(
    viewModel: SubredditViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val subreddits by viewModel.subreddits.collectAsState()

    var newSubName by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCsvText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subreddit feeds manager", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        importCsvText = viewModel.exportToCsv()
                        showImportDialog = true
                    }) {
                        Icon(Icons.Default.ImportExport, contentDescription = "Import/Export")
                    }
                    IconButton(onClick = { viewModel.resetDefaults() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Defaults")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Text box for custom entries
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newSubName,
                    onValueChange = { newSubName = it },
                    placeholder = { Text("Add custom r/subreddit...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (newSubName.isNotBlank()) {
                            viewModel.addSubreddit(newSubName)
                            newSubName = ""
                            Toast.makeText(context, "Community registered!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subreddit listing
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(subreddits, key = { _, item -> item.name }) { index, item ->
                    SubredditRow(
                        item = item,
                        onToggle = { viewModel.toggleSubreddit(item) },
                        onDelete = { viewModel.removeSubreddit(item.name) },
                        onMoveUp = { viewModel.moveSubredditUp(index) },
                        onMoveDown = { viewModel.moveSubredditDown(index) },
                        isFirst = index == 0,
                        isLast = index == subreddits.size - 1
                    )
                }
            }
        }
    }

    // Bulk Import/Export Dialog helper
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Bulk Import / Export communities") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Paste a list of subreddits separated by commas, or copy the active list to export:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = importCsvText,
                        onValueChange = { importCsvText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("e.g. wallpapers, earthporn, amoledbackgrounds") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.bulkImport(importCsvText)
                    showImportDialog = false
                    Toast.makeText(context, "CSV list merged!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Apply Names")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(importCsvText))
                    Toast.makeText(context, "List copied to Clipboard!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Copy Code")
                }
            }
        )
    }
}

@Composable
fun SubredditRow(
    item: SubredditEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("r/${item.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = if (item.isCustom) "Custom Community" else "Default Platform Seed",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Positional controllers
                IconButton(onClick = onMoveUp, enabled = !isFirst) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (isFirst) Color.LightGray else Color.Gray)
                }
                IconButton(onClick = onMoveDown, enabled = !isLast) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (isLast) Color.LightGray else Color.Gray)
                }

                // Enabled Toggle Switch
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                }
            }
        }
    }
}
