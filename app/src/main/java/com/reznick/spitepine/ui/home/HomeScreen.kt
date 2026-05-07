package com.reznick.spitepine.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reznick.spitepine.app
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.ui.components.StatusBadge
import com.reznick.spitepine.ui.theme.SpitePineTheme

private enum class HomeMode { MAP, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onTreeClick: (Tree) -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.app
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    val trees by viewModel.trees.collectAsStateWithLifecycle()

    var mode by rememberSaveable { mutableStateOf(HomeMode.MAP) }
    var pendingDelete by remember { mutableStateOf<Tree?>(null) }

    // When there are no trees yet, falling back to the list-with-empty-state
    // gives a friendlier experience than a blank world map.
    val effectiveMode = if (trees.isEmpty()) HomeMode.LIST else mode

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("SpitePine") },
                actions = {
                    if (trees.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = {
                                mode = if (mode == HomeMode.MAP) HomeMode.LIST else HomeMode.MAP
                            },
                            modifier = Modifier.padding(end = 12.dp),
                        ) {
                            if (mode == HomeMode.MAP) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("List")
                            } else {
                                Icon(Icons.Default.Place, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Map")
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add tree")
            }
        },
    ) { padding ->
        when (effectiveMode) {
            HomeMode.LIST -> {
                if (trees.isEmpty()) {
                    HomeEmpty(padding)
                } else {
                    HomeList(
                        trees = trees,
                        padding = padding,
                        onDeleteClick = { pendingDelete = it },
                        onTreeClick = onTreeClick,
                    )
                }
            }
            HomeMode.MAP -> {
                TreeMapView(
                    trees = trees,
                    padding = padding,
                    onTreeClick = onTreeClick,
                )
            }
        }
    }

    pendingDelete?.let { tree ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this tree?") },
            text = {
                Column {
                    Text(
                        text = tree.address.ifBlank { "Tree ${tree.id.takeLast(6)}" },
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (tree.notes.isNotBlank()) {
                        Text(
                            text = tree.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(tree.id)
                    pendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun HomeEmpty(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No trees yet",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Tap + to add one you spotted.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeList(
    trees: List<Tree>,
    padding: PaddingValues,
    onDeleteClick: (Tree) -> Unit,
    onTreeClick: (Tree) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 12.dp,
            bottom = padding.calculateBottomPadding() + 88.dp, // clear FAB
            start = 12.dp,
            end = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(trees, key = { it.id }) { tree ->
            TreeCard(
                tree = tree,
                onDeleteClick = { onDeleteClick(tree) },
                onClick = { onTreeClick(tree) },
            )
        }
    }
}

@Composable
private fun TreeCard(tree: Tree, onDeleteClick: () -> Unit, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 4.dp, top = 4.dp)) {
                Text(
                    text = tree.address.ifBlank { "Tree ${tree.id.takeLast(6)}" },
                    style = MaterialTheme.typography.titleMedium,
                )
                if (tree.notes.isNotBlank()) {
                    Text(
                        text = tree.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                StatusBadge(
                    status = tree.status,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete tree",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    SpitePineTheme { HomeEmpty(PaddingValues(0.dp)) }
}
