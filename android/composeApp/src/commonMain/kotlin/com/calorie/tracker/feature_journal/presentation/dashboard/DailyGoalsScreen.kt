package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGoalsScreen(
    initialCalories: Int = 1500,
    initialCarbsPct: Int = 45,
    initialProteinPct: Int = 43,
    initialFatPct: Int = 12,
    onBackClick: () -> Unit,
    onGoalsSaved: (Int, Int, Int, Int) -> Unit,
    onCalculatorClick: () -> Unit
) {
    var caloriesText by remember { mutableStateOf(initialCalories.toString()) }
    var carbsPctText by remember { mutableStateOf(initialCarbsPct.toString()) }
    var proteinPctText by remember { mutableStateOf(initialProteinPct.toString()) }
    var fatPctText by remember { mutableStateOf(initialFatPct.toString()) }

    val calories = caloriesText.toIntOrNull() ?: 0
    val carbsPct = carbsPctText.toIntOrNull() ?: 0
    val proteinPct = proteinPctText.toIntOrNull() ?: 0
    val fatPct = fatPctText.toIntOrNull() ?: 0

    val carbsGrams = (calories * (carbsPct / 100.0) / 4.0).roundToInt()
    val proteinGrams = (calories * (proteinPct / 100.0) / 4.0).roundToInt()
    val fatGrams = (calories * (fatPct / 100.0) / 9.0).roundToInt()

    val totalPct = carbsPct + proteinPct + fatPct

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Goals", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        onGoalsSaved(calories, carbsPct, proteinPct, fatPct)
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Calories Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Calories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                UnderlineInputField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculator Link Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCalculatorClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Calculator",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Use daily calorie goal calculator",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Carbohydrates Pct Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Carbohydrates ${carbsGrams}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                UnderlineInputField(
                    value = carbsPctText,
                    onValueChange = { carbsPctText = it },
                    suffix = "%"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Protein Pct Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Protein ${proteinGrams}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                UnderlineInputField(
                    value = proteinPctText,
                    onValueChange = { proteinPctText = it },
                    suffix = "%"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fat Pct Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Fat ${fatGrams}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                UnderlineInputField(
                    value = fatPctText,
                    onValueChange = { fatPctText = it },
                    suffix = "%"
                )
            }

            if (totalPct != 100) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Warning: Total percentage is $totalPct% (must be 100%)",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun UnderlineInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = suffix,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }
}
