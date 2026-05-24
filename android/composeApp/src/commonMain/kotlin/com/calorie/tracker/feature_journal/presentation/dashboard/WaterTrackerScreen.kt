package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    waterRepository: com.calorie.tracker.feature_journal.domain.WaterRepository? = null,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val todayStr = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
    
    val waterLog by produceState<com.calorie.tracker.feature_journal.domain.WaterEntry?>(initialValue = null, waterRepository) {
        waterRepository?.getWaterLogByDate(todayStr)?.collect {
            value = it
        }
    }
    
    val waterGlasses = waterLog?.glasses ?: 0
    val targetGlasses = 8
    
    val progress by animateFloatAsState(
        targetValue = (waterGlasses.toFloat() / targetGlasses.toFloat()).coerceIn(0f, 1f),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Tracker", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF2196F3).copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF2196F3),
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$waterGlasses / $targetGlasses",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Glasses",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { 
                        if (waterGlasses > 0) {
                            scope.launch {
                                waterRepository?.addGlasses(
                                    glasses = -1,
                                    dateStr = todayStr,
                                    timestamp = Clock.System.now().toEpochMilliseconds()
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove Water")
                }
                
                FilledIconButton(
                    onClick = { 
                        scope.launch {
                            waterRepository?.addGlasses(
                                glasses = 1,
                                dateStr = todayStr,
                                timestamp = Clock.System.now().toEpochMilliseconds()
                            )
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Water")
                }
            }
        }
    }
}
