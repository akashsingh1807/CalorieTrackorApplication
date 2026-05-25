package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(
    currentStreak: Int = 27,
    longestStreak: Int = 27,
    caloriesAboveBudget: Int = 1023,
    averageCalories: Int = 1841,
    currentWeightKg: Int = 78,
    mealRepository: com.calorie.tracker.feature_journal.domain.MealRepository,
    budgetCalorie: Int,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Logged Days Card
            Card(
            modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Logged Days",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$currentStreak",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Current Streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$longestStreak",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Longest Streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Current Week Card
            Card(
            modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Week",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Date row: Last 7 days
                    val streakDays = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<StreakDay>>(emptyList()) }
                    val computedAverageCalories = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
                    val computedAboveBudget = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
                    val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
                    val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(timeZone).date
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val startOf7DaysAgo = today.minus(6, kotlinx.datetime.DateTimeUnit.DAY)
                            .atStartOfDayIn(timeZone).toEpochMilliseconds()
                        val endOfToday = today.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
                            .atStartOfDayIn(timeZone).toEpochMilliseconds()
                            
                        mealRepository.getMealsForDate(startOf7DaysAgo, endOfToday).collect { allMeals ->
                            val daysList = mutableListOf<StreakDay>()
                            var totalCalAllDays = 0
                            var totalDaysWithLogs = 0
                            var totalAboveBudget = 0

                            for (i in 6 downTo 0) {
                                val d = today.minus(i, kotlinx.datetime.DateTimeUnit.DAY)
                                val dStart = d.atStartOfDayIn(timeZone).toEpochMilliseconds()
                                val dEnd = d.plus(1, kotlinx.datetime.DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
                                
                                val mealsForDay = allMeals.filter { it.timestamp in dStart until dEnd }
                                val dayTotal = mealsForDay.sumOf { it.totalCalories }.toInt()
                                val hasMeals = mealsForDay.isNotEmpty()
                                val isOver = hasMeals && dayTotal > budgetCalorie
                                val isUnderOrEq = hasMeals && dayTotal <= budgetCalorie
                                
                                if (hasMeals) {
                                    totalCalAllDays += dayTotal
                                    totalDaysWithLogs++
                                    if (dayTotal > budgetCalorie) {
                                        totalAboveBudget += (dayTotal - budgetCalorie)
                                    }
                                }
                                
                                daysList.add(
                                    StreakDay(
                                        name = d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                                        dayNum = d.dayOfMonth.toString(),
                                        isOverBudget = isOver,
                                        isUnderOrEqualBudget = isUnderOrEq,
                                        isSelected = (i == 0) // today is selected
                                    )
                                )
                            }
                            streakDays.value = daysList
                            computedAverageCalories.value = if (totalDaysWithLogs > 0) (totalCalAllDays / totalDaysWithLogs) else 0
                            computedAboveBudget.value = totalAboveBudget
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (day in streakDays.value) {
                            val bgColor = if (day.isOverBudget) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else if (day.isUnderOrEqualBudget) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                Color.Transparent
                            }
                            val textColor = if (day.isOverBudget) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else if (day.isUnderOrEqualBudget) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                Color.Gray
                            }
                            
                            val borderColor = if (day.isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                            val borderStroke = if (day.isSelected) BorderStroke(1.dp, borderColor) else null

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(52.dp)
                                    .background(color = bgColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .let { modifier ->
                                        if (borderStroke != null) {
                                            modifier.border(borderStroke, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                        } else {
                                            modifier
                                        }
                                    }
                            ) {
                                Text(
                                    text = day.name,
                                    fontSize = 11.sp,
                                    color = textColor
                                )
                                Text(
                                    text = day.dayNum,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.takeUnless { it == Color.Gray } ?: (if (day.isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Above budget / Avg Calories Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${computedAboveBudget.value}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Calories Above\nBudget",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier.height(50.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${computedAverageCalories.value}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Average Calories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Weight
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$currentWeightKg kg",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Current Weight",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // View Weekly Summary Button
                    Row(
                        modifier = Modifier
                            .clickable { /* Simulate Navigation */ }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Weekly Summary",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Weekly Summary",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private data class StreakDay(
    val name: String,
    val dayNum: String,
    val isOverBudget: Boolean,
    val isUnderOrEqualBudget: Boolean,
    val isSelected: Boolean
)
