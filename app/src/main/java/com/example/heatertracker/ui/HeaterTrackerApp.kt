package com.example.heatertracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.heatertracker.data.TournamentType
import androidx.compose.material3.menuAnchor
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun HeaterTrackerApp(viewModel: HeaterTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Heater Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_input_add),
                    contentDescription = "Add Tournament"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryRow(uiState)
            ProfitChart(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(180.dp),
                points = uiState.cumulativePoints
            )
            TournamentList(
                modifier = Modifier.weight(1f),
                uiState = uiState
            )
        }
    }

    if (showAddDialog) {
        AddTournamentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { date, buyIn, payout, type, notes ->
                scope.launch {
                    viewModel.addTournament(date, buyIn, payout, type, notes)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun SummaryRow(uiState: HeaterTrackerUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Profit",
            value = formatCurrency(uiState.totalProfit),
            valueColor = if (uiState.totalProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Total Buy-ins",
            value = formatCurrency(uiState.totalBuyIns),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "ITM %",
            value = "${String.format("%.1f", uiState.itmPercentage)}%",
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun TournamentList(
    modifier: Modifier = Modifier,
    uiState: HeaterTrackerUiState
) {
    if (uiState.entries.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No tournaments recorded yet. Tap the + button to add one!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(uiState.entries) { entry ->
            TournamentRow(entry = entry)
        }
    }
}

@Composable
private fun TournamentRow(entry: TournamentEntryUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = entry.type.displayName, style = MaterialTheme.typography.titleMedium)
                Text(text = entry.dateLabel, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = formatCurrency(entry.profit),
                color = if (entry.profit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Buy-in: ${formatCurrency(entry.buyIn)}")
            Text(text = "Payout: ${formatCurrency(entry.payout)}")
        }
        if (entry.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.notes,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfitChart(modifier: Modifier = Modifier, points: List<ProfitPoint>) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Add results to see your bankroll trajectory", textAlign = TextAlign.Center)
        }
        return
    }

    val minValue = points.minOf { it.value }
    val maxValue = points.maxOf { it.value }
    val range = (maxValue - minValue).coerceAtLeast(1.0)

    Canvas(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val stepX = if (points.size == 1) chartWidth else chartWidth / (points.size - 1)

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = stepX * index
            val normalized = (point.value - minValue) / range
            val y = chartHeight - (normalized * chartHeight)
            if (index == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }
        }

        drawPath(
            path = path,
            color = Color(0xFF2E7D32),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        points.forEachIndexed { index, point ->
            val x = stepX * index
            val normalized = (point.value - minValue) / range
            val y = chartHeight - (normalized * chartHeight)
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = 8f,
                center = Offset(x, y.toFloat())
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTournamentDialog(
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double, Double, TournamentType, String) -> Unit
) {
    var dateInput by remember { mutableStateOf(LocalDate.now().toString()) }
    var buyInInput by remember { mutableStateOf("") }
    var payoutInput by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TournamentType.MTT) }
    var notesInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Tournament") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Date (YYYY-MM-DD)") }
                )
                OutlinedTextField(
                    value = buyInInput,
                    onValueChange = { buyInInput = it },
                    label = { Text("Buy-in") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = payoutInput,
                    onValueChange = { payoutInput = it },
                    label = { Text("Payout") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tournament Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        TournamentType.values().forEach { type ->
                            TextButton(onClick = {
                                selectedType = type
                                typeExpanded = false
                            }) {
                                Text(text = type.displayName)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes") },
                    maxLines = 4
                )
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val date = LocalDate.parse(dateInput)
                    val buyIn = buyInInput.toDoubleOrNull()
                    val payout = payoutInput.toDoubleOrNull()
                    if (buyIn == null || payout == null) {
                        errorMessage = "Enter valid numeric values for buy-in and payout"
                        return@TextButton
                    }
                    onSave(date, buyIn, payout, selectedType, notesInput)
                } catch (e: DateTimeParseException) {
                    errorMessage = "Invalid date format"
                }
            }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

private fun formatCurrency(value: Double): String =
    if (value < 0) "-$" + String.format("%.2f", kotlin.math.abs(value))
    else "$" + String.format("%.2f", value)
