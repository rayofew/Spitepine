package com.reznick.spitepine.ui.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.reznick.spitepine.app
import com.reznick.spitepine.data.model.HarvestEntry
import com.reznick.spitepine.data.model.PermissionEvent
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.model.TreeStatus
import com.reznick.spitepine.ui.components.StatusBadge
import com.reznick.spitepine.ui.components.label
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeDetailScreen(
    treeId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.app
    val viewModel: TreeDetailViewModel = viewModel(factory = TreeDetailViewModel.factory(app, treeId))
    val tree by viewModel.tree.collectAsStateWithLifecycle()

    var showStatusSheet by remember { mutableStateOf(false) }
    var showHarvestSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tree?.address?.ifBlank { null }
                            ?: tree?.id?.takeLast(6)?.let { "Tree $it" }
                            ?: "Tree",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit tree",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete tree",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val current = tree
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tree not found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            DetailBody(
                tree = current,
                padding = padding,
                onChangeStatus = { showStatusSheet = true },
                onLogHarvest = { showHarvestSheet = true },
                onDirections = { viewModel.openDirections(context, current) },
            )
        }
    }

    if (showStatusSheet && tree != null) {
        StatusChangeSheet(
            currentStatus = tree!!.status,
            onDismiss = { showStatusSheet = false },
            onSave = { newStatus, spokeWith, notes ->
                viewModel.changeStatus(newStatus, spokeWith, notes)
                showStatusSheet = false
            },
        )
    }

    if (showHarvestSheet) {
        HarvestLogSheet(
            onDismiss = { showHarvestSheet = false },
            onSave = { yield, notes ->
                viewModel.logHarvest(yield, notes)
                showHarvestSheet = false
            },
        )
    }

    if (showDeleteDialog && tree != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this tree?") },
            text = {
                Text(
                    text = tree!!.address.ifBlank { "Tree ${tree!!.id.takeLast(6)}" },
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(onBack)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DetailBody(
    tree: Tree,
    padding: PaddingValues,
    onChangeStatus: () -> Unit,
    onLogHarvest: () -> Unit,
    onDirections: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Address + GPS header
        Column {
            Text(
                text = tree.address.ifBlank { "Tree ${tree.id.takeLast(6)}" },
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "%.5f, %.5f".format(tree.location.latitude, tree.location.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Status section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Status", style = MaterialTheme.typography.labelMedium)
                StatusBadge(status = tree.status)
                FilledTonalButton(
                    onClick = onChangeStatus,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Change status")
                }
            }
        }

        // Notes
        if (tree.notes.isNotBlank()) {
            Column {
                Text("Notes", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(tree.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Permission history
        if (tree.permissionHistory.isNotEmpty()) {
            Column {
                Text("Permission history", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Card(colors = CardDefaults.outlinedCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        tree.permissionHistory
                            .sortedByDescending { it.timestamp?.toDate() }
                            .forEachIndexed { i, event ->
                                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                PermissionEventRow(event)
                            }
                    }
                }
            }
        }

        // Harvest log
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Harvest log",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(onClick = onLogHarvest) { Text("+ Log harvest") }
            }
            Spacer(Modifier.height(4.dp))
            if (tree.harvestLog.isEmpty()) {
                Text(
                    "No harvests yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Card(colors = CardDefaults.outlinedCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        tree.harvestLog
                            .sortedByDescending { it.timestamp?.toDate() }
                            .forEachIndexed { i, entry ->
                                if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                HarvestEntryRow(entry)
                            }
                    }
                }
            }
        }

        // Action buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = onDirections,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Get directions")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionEventRow(event: PermissionEvent) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = event.timestamp.format(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            StatusBadge(status = event.status)
            if (event.spokeWith.isNotBlank()) {
                Text(
                    text = "Talked to ${event.spokeWith}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (event.agreementNotes.isNotBlank()) {
                Text(
                    text = event.agreementNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HarvestEntryRow(entry: HarvestEntry) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = entry.timestamp.format(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            if (entry.yieldEstimate.isNotBlank()) {
                Text(
                    text = "Yield: ${entry.yieldEstimate}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (entry.notes.isNotBlank()) {
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusChangeSheet(
    currentStatus: TreeStatus,
    onDismiss: () -> Unit,
    onSave: (TreeStatus, String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var spokeWith by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Change status", style = MaterialTheme.typography.titleMedium)
            // Status chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TreeStatus.entries.toList().chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { status ->
                            FilterChip(
                                selected = status == selectedStatus,
                                onClick = { selectedStatus = status },
                                label = { Text(status.label()) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            OutlinedTextField(
                value = spokeWith,
                onValueChange = { spokeWith = it },
                label = { Text("Talked to (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("What was agreed (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 3,
            )
            FilledTonalButton(
                onClick = {
                    onSave(selectedStatus, spokeWith, notes)
                    scope.launch { sheetState.hide() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HarvestLogSheet(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var yieldEstimate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Log a harvest", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Yield (optional)", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("small", "medium", "large").forEach { y ->
                    FilterChip(
                        selected = yieldEstimate == y,
                        onClick = {
                            yieldEstimate = if (yieldEstimate == y) "" else y
                        },
                        label = { Text(y.replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 3,
            )
            FilledTonalButton(
                onClick = {
                    onSave(yieldEstimate, notes)
                    scope.launch { sheetState.hide() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Save")
            }
        }
    }
}

private fun Timestamp?.format(): String =
    this?.toDate()?.let { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it) } ?: "—"
