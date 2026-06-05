package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- State Models ---

sealed interface ChatScreenState {
    data object Loading : ChatScreenState // Initial screen load
    
    data class Success(
        val dailyMacros: DailyMacroSummary,
        val messages: List<FoodMessage>,
        val isTyping: Boolean = false, // Controls the 3-dot typing indicator
        val inputText: String = ""
    ) : ChatScreenState
    
    data class Error(val message: String) : ChatScreenState
}

data class DailyMacroSummary(
    val currentCalories: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val targetProtein: Int,
    val carbsGrams: Int,
    val targetCarbs: Int,
    val fatsGrams: Int,
    val targetFats: Int
)

sealed interface FoodMessage {
    val id: String
    val timestamp: Long
    
    data class UserEntry(
        override val id: String,
        override val timestamp: Long,
        val rawText: String,
        val isSynced: Boolean // Used to show a pending indicator if offline
    ) : FoodMessage
    
    data class SystemNutrition(
        override val id: String,
        override val timestamp: Long,
        val foodName: String,
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fats: Int
    ) : FoodMessage
}

// --- Intent/Event Model ---

sealed interface ChatIntent {
    data class UpdateInputText(val text: String) : ChatIntent
    data class SubmitMessage(val text: String) : ChatIntent
    data class DeleteMessage(val messageId: String) : ChatIntent
    // Provide new macros for editing
    data class EditMessage(val messageId: String, val newCalories: Int) : ChatIntent 
}

// --- Composables ---

@Composable
fun DailyDashboardScreen(
    state: ChatScreenState.Success,
    onIntent: (ChatIntent) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size + (if (state.isTyping) 1 else 0))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ChatInputBar(
                inputText = state.inputText,
                onTextChange = { onIntent(ChatIntent.UpdateInputText(it)) },
                onSubmit = { onIntent(ChatIntent.SubmitMessage(it)) }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Essential for smooth keyboard handling
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sticky Macro Dashboard Header
            StickyMacroHeader(state.dailyMacros)

            // Infinite Chat Timeline
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
                
                if (state.isTyping) {
                    item {
                        TypingIndicatorBubble()
                    }
                }
            }
        }
    }
}

@Composable
fun StickyMacroHeader(macros: DailyMacroSummary) {
    // Spring physics for macro animations
    val animSpec = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    
    val calorieProgress by animateFloatAsState(
        targetValue = macros.currentCalories.toFloat() / macros.targetCalories.toFloat(),
        animationSpec = animSpec, label = "calProgress"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Calorie Ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                CircularProgressIndicator(
                    progress = { calorieProgress.coerceIn(0f, 1f) },
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${macros.currentCalories}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("kcal", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            // Linear Progress Bars for P/C/F
            Column(modifier = Modifier.weight(1f).padding(start = 24.dp)) {
                MacroProgressBar("Protein", macros.proteinGrams, macros.targetProtein, Color(0xFFE57373), animSpec)
                Spacer(modifier = Modifier.height(8.dp))
                MacroProgressBar("Carbs", macros.carbsGrams, macros.targetCarbs, Color(0xFF64B5F6), animSpec)
                Spacer(modifier = Modifier.height(8.dp))
                MacroProgressBar("Fats", macros.fatsGrams, macros.targetFats, Color(0xFFFFD54F), animSpec)
            }
        }
    }
}

@Composable
fun MacroProgressBar(label: String, current: Int, target: Int, color: Color, animSpec: AnimationSpec<Float>) {
    val progress by animateFloatAsState(
        targetValue = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f),
        animationSpec = animSpec, label = "$label-progress"
    )
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, modifier = Modifier.width(48.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text("$current/$target", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun MessageBubble(message: FoodMessage) {
    val isUser = message is FoodMessage.UserEntry
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            when (message) {
                is FoodMessage.UserEntry -> {
                    Text(message.rawText, color = MaterialTheme.colorScheme.onPrimary)
                }
                is FoodMessage.SystemNutrition -> {
                    Column {
                        Text(message.foodName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${message.calories} kcal • ${message.protein}g P • ${message.carbs}g C • ${message.fats}g F",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(animation = tween(300), repeatMode = RepeatMode.Reverse), label = "dot1"
    )
    val dotOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(animation = tween(300, delayMillis = 150), repeatMode = RepeatMode.Reverse), label = "dot2"
    )
    val dotOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(animation = tween(300, delayMillis = 300), repeatMode = RepeatMode.Reverse), label = "dot3"
    )

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).offset(y = dotOffset1.dp).background(Color.Gray, CircleShape))
        Box(modifier = Modifier.size(6.dp).offset(y = dotOffset2.dp).background(Color.Gray, CircleShape))
        Box(modifier = Modifier.size(6.dp).offset(y = dotOffset3.dp).background(Color.Gray, CircleShape))
    }
}

@Composable
fun ChatInputBar(inputText: String, onTextChange: (String) -> Unit, onSubmit: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChange,
                placeholder = { Text("I ate 2 eggs and a bagel...") },
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { 
                    if(inputText.isNotBlank()) {
                        onSubmit(inputText)
                        onTextChange("")
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    if(inputText.isNotBlank()) {
                        onSubmit(inputText)
                        onTextChange("")
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
