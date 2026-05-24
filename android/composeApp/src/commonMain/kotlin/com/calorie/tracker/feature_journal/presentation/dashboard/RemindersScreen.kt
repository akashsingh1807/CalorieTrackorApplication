package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBackClick: () -> Unit
) {
    var breakfastEnabled by remember { mutableStateOf(true) }
    var lunchEnabled by remember { mutableStateOf(false) }
    var dinnerEnabled by remember { mutableStateOf(true) }
    var waterEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.SemiBold) },
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ReminderItem(
                        title = "Breakfast (9:00 AM)",
                        description = "Log your morning meal",
                        checked = breakfastEnabled,
                        onCheckedChange = { breakfastEnabled = it }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    ReminderItem(
                        title = "Lunch (1:00 PM)",
                        description = "Log your midday meal",
                        checked = lunchEnabled,
                        onCheckedChange = { lunchEnabled = it }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    ReminderItem(
                        title = "Dinner (7:00 PM)",
                        description = "Log your evening meal",
                        checked = dinnerEnabled,
                        onCheckedChange = { dinnerEnabled = it }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    ReminderItem(
                        title = "Hydration Reminders",
                        description = "Remind me to drink water every 2 hours",
                        checked = waterEnabled,
                        onCheckedChange = { waterEnabled = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(text = description, color = Color.Gray, fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}
