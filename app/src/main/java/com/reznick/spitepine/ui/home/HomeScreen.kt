package com.reznick.spitepine.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.app
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
    val trees by viewModel.trees.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add tree")
            }
        },
    ) { padding ->
        if (trees.isEmpty()) HomeEmpty(padding) else HomeList(trees, padding)
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
private fun HomeList(trees: List<Tree>, padding: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 12.dp,
            bottom = padding.calculateBottomPadding() + 88.dp, // clear FAB
            start = 12.dp,
            end = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(trees, key = { it.id }) { tree -> TreeCard(tree) }
    }
}

@Composable
private fun TreeCard(tree: Tree) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
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
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    SpitePineTheme { HomeEmpty(PaddingValues(0.dp)) }
}
