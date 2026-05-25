package com.calorie.tracker.feature_auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorie.tracker.ui.components.Flip7Button
import com.calorie.tracker.ui.components.Flip7ButtonVariant
import com.calorie.tracker.ui.components.Flip7Card
import com.calorie.tracker.ui.components.Flip7CardVariant
import com.calorie.tracker.ui.components.Flip7TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (age: Int, height: Double, weight: Double, lifestyle: String, goal: String) -> Unit
) {
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    
    var expandedLifestyle by remember { mutableStateOf(false) }
    var lifestyle by remember { mutableStateOf("SEDENTARY") }
    val lifestyles = listOf("SEDENTARY", "LIGHTLY_ACTIVE", "MODERATELY_ACTIVE", "VERY_ACTIVE", "EXTRA_ACTIVE")

    var expandedGoal by remember { mutableStateOf(false) }
    var goal by remember { mutableStateOf("MAINTENANCE") }
    val goals = listOf("FAT_LOSS", "MAINTENANCE", "MUSCLE_GAIN")

    var calculatedCalories by remember { mutableStateOf<Int?>(null) }
    var calculatedCarbsGrams by remember { mutableStateOf<Int?>(null) }
    var calculatedProteinGrams by remember { mutableStateOf<Int?>(null) }
    var calculatedFatGrams by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Let's Get Started",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tell us a bit about yourself so we can calculate your personalized goals.",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Flip7Card(variant = Flip7CardVariant.MONOCHROME) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Flip7TextField(
                        value = age,
                        onValueChange = { age = it.filter { char -> char.isDigit() } },
                        label = "Age (Years)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Flip7TextField(
                        value = height,
                        onValueChange = { height = it },
                        label = "Height (cm)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    Flip7TextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = "Weight (kg)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Lifestyle Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedLifestyle,
                        onExpandedChange = { expandedLifestyle = !expandedLifestyle }
                    ) {
                        OutlinedTextField(
                            value = lifestyle,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Lifestyle") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLifestyle) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLifestyle,
                            onDismissRequest = { expandedLifestyle = false }
                        ) {
                            lifestyles.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        lifestyle = selectionOption
                                        expandedLifestyle = false
                                    }
                                )
                            }
                        }
                    }

                    // Goal Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedGoal,
                        onExpandedChange = { expandedGoal = !expandedGoal }
                    ) {
                        OutlinedTextField(
                            value = goal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Goal") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGoal,
                            onDismissRequest = { expandedGoal = false }
                        ) {
                            goals.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        goal = selectionOption
                                        expandedGoal = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Flip7Button(
                        text = "Calculate Goals",
                        variant = Flip7ButtonVariant.MONOCHROME,
                        onClick = {
                            val ageInt = age.toIntOrNull() ?: 25
                            val heightDouble = height.toDoubleOrNull() ?: 170.0
                            val weightDouble = weight.toDoubleOrNull() ?: 70.0
                            
                            val bmr = 10 * weightDouble + 6.25 * heightDouble - 5 * ageInt + 5
                            val lifestyleMultiplier = when (lifestyle) {
                                "SEDENTARY" -> 1.2
                                "LIGHTLY_ACTIVE" -> 1.375
                                "MODERATELY_ACTIVE" -> 1.55
                                "VERY_ACTIVE" -> 1.725
                                "EXTRA_ACTIVE" -> 1.9
                                else -> 1.2
                            }
                            val tdee = bmr * lifestyleMultiplier
                            val targetCalories = when (goal) {
                                "FAT_LOSS" -> tdee - 500
                                "MUSCLE_GAIN" -> tdee + 500
                                else -> tdee
                            }.toInt()
                            
                            calculatedCalories = targetCalories
                            calculatedCarbsGrams = (targetCalories * 0.50 / 4.0).toInt()
                            calculatedProteinGrams = (targetCalories * 0.30 / 4.0).toInt()
                            calculatedFatGrams = (targetCalories * 0.20 / 9.0).toInt()
                        }
                    )

                    calculatedCalories?.let { calories ->
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Calculated Daily Targets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Calories", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$calories kcal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Carbs (50%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${calculatedCarbsGrams}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Protein (30%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${calculatedProteinGrams}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("Fat (20%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${calculatedFatGrams}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Flip7Button(
                            text = "Save to Daily Goals ✓",
                            variant = Flip7ButtonVariant.MONOCHROME,
                            onClick = {
                                val ageInt = age.toIntOrNull() ?: 25
                                val heightDouble = height.toDoubleOrNull() ?: 170.0
                                val weightDouble = weight.toDoubleOrNull() ?: 70.0
                                onComplete(ageInt, heightDouble, weightDouble, lifestyle, goal)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
