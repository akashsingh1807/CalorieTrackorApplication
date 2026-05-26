package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.calorie.tracker.model.BookmarkedMeal
import com.calorie.tracker.model.FoodItemDto
import com.calorie.tracker.model.Meal
import com.calorie.tracker.ui.components.Flip7Card
import com.calorie.tracker.ui.components.Flip7CardVariant
import com.calorie.tracker.ui.components.Flip7Button
import com.calorie.tracker.ui.components.Flip7ButtonVariant
import com.calorie.tracker.ui.components.Flip7SectionTitle
import com.calorie.tracker.ui.theme.CoralPrimary
import com.calorie.tracker.ui.theme.PrimaryTeal
import com.calorie.tracker.ui.theme.AccentGold
import com.calorie.tracker.ui.theme.SkyBlue
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.min
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    calorieGoal: Int,
    carbsGoalPct: Int,
    proteinGoalPct: Int,
    fatGoalPct: Int,
    onMenuClick: () -> Unit,
    onStreakClick: () -> Unit,
    onLogout: () -> Unit
) {
    val meals by viewModel.meals.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val currentStreak by viewModel.streak.collectAsState()

    val focusManager = LocalFocusManager.current
    var queryText by remember { mutableStateOf("") }
    var showBookmarkSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show snackbar when ViewModel posts a feedback message
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearFeedback()
        }
    }

    // Static dates matching the screenshot (May 14 - May 20, 2026)
    val dates = listOf(
        LocalDate(2026, 5, 14),
        LocalDate(2026, 5, 15),
        LocalDate(2026, 5, 16),
        LocalDate(2026, 5, 17),
        LocalDate(2026, 5, 18),
        LocalDate(2026, 5, 19),
        LocalDate(2026, 5, 20)
    )

    // Interactive Water Trackers
    var waterCupsToday by remember { mutableStateOf(0) }
    var waterCupsYesterday by remember { mutableStateOf(0) }

    val currentWaterCups = if (selectedDate == LocalDate(2026, 5, 20)) waterCupsToday else waterCupsYesterday
    val setWaterCups: (Int) -> Unit = { newCups ->
        if (selectedDate == LocalDate(2026, 5, 20)) {
            waterCupsToday = newCups.coerceIn(0, 16)
        } else {
            waterCupsYesterday = newCups.coerceIn(0, 16)
        }
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    // Gram targets based on percentages
    val carbsGoalGrams = (calorieGoal * (carbsGoalPct / 100.0) / 4.0).roundToInt()
    val proteinGoalGrams = (calorieGoal * (proteinGoalPct / 100.0) / 4.0).roundToInt()
    val fatGoalGrams = (calorieGoal * (fatGoalPct / 100.0) / 9.0).roundToInt()

    // Helper to analyze and log meal — delegates to AI via ViewModel
    val submitMealText: (String) -> Unit = { query ->
        if (query.isNotBlank()) {
            viewModel.analyzeAndLogMeal(query)
            queryText = ""
            focusManager.clearFocus()
        }
    }

    // ── Bookmark Bottom Sheet ─────────────────────────────────
    if (showBookmarkSheet) {
        BookmarkBottomSheet(
            bookmarks = bookmarks,
            onDismiss = { showBookmarkSheet = false },
            onLogBookmark = { bookmark ->
                viewModel.logBookmark(bookmark)
                showBookmarkSheet = false
            },
            onDeleteBookmark = { id -> viewModel.deleteBookmark(id) }
        )
    }

    val imagePicker = com.calorie.tracker.core.ui.rememberImagePicker { bytes ->
        if (bytes != null) {
            viewModel.analyzeAndLogMealFromImage(bytes)
        }
    }

    val speechRecognizer = com.calorie.tracker.core.ui.rememberSpeechRecognizer(
        onResult = { text -> submitMealText(text) },
        onError = { errorMsg -> 
            scope.launch {
                snackbarHostState.showSnackbar(errorMsg)
            }
        }
    )


    // ── AI Analysis Dialogs ──────────────────────────────────
    when (val state = analysisState) {
        is MealAnalysisState.Analyzing -> {
            Dialog(onDismissRequest = {}) {
                Flip7Card() {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.Black)
                        Text(
                            text = "Analyzing your food...",
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Getting accurate nutrition data",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        is MealAnalysisState.PendingConfirmation -> {
            FoodConfirmationDialog(
                originalText = state.originalText,
                initialFoodItems = state.foodItems,
                onConfirm = { items, saveBookmark, bookmarkName ->
                    viewModel.confirmAndLogMeals(items, state.originalText, saveBookmark, bookmarkName)
                },
                onDismiss = { viewModel.dismissAnalysis() }
            )
        }
        is MealAnalysisState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAnalysis() },
                title = { Text("Could not analyze") },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissAnalysis() }) { Text("OK") }
                }
            )
        }
        else -> {}
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { dropdownExpanded = true }
                    ) {
                        Text(
                            text = if (selectedDate == LocalDate(2026, 5, 20)) "Today" else if (selectedDate == LocalDate(2026, 5, 19)) "Yesterday" else "${selectedDate.dayOfMonth} ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Date dropdown",
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Today") },
                                onClick = {
                                    viewModel.selectDate(LocalDate(2026, 5, 20))
                                    dropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Yesterday") },
                                onClick = {
                                    viewModel.selectDate(LocalDate(2026, 5, 19))
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Groups",
                            tint = Color.DarkGray
                        )
                    }

                    // Streak lightning bolt item
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onStreakClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$currentStreak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Bottom chat input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("What did you eat or exercise?", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp, max = 120.dp),
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            submitMealText(queryText)
                        }
                    ),
                    trailingIcon = {
                        if (queryText.isNotBlank()) {
                            IconButton(onClick = { submitMealText(queryText) }) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showBookmarkSheet = true }) {
                        Icon(
                            imageVector = if (bookmarks.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Saved Meals",
                            tint = if (bookmarks.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { imagePicker.pickImage() }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gallery",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { imagePicker.takePhoto() }) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Camera",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { speechRecognizer.startListening() }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Horizontal calendar day selector row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (date in dates) {
                        val isSelected = selectedDate == date
                        val isLogged = date <= LocalDate(2026, 5, 19)
                        val dayName = when (date.dayOfWeek.name) {
                            "MONDAY" -> "Mon"
                            "TUESDAY" -> "Tue"
                            "WEDNESDAY" -> "Wed"
                            "THURSDAY" -> "Thu"
                            "FRIDAY" -> "Fri"
                            "SATURDAY" -> "Sat"
                            else -> "Sun"
                        }
                        val dayNum = date.dayOfMonth.toString()

                        val bgColor = if (isLogged) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                        val borderColor = if (isSelected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            Color.Transparent
                        }
                        val borderStroke = if (isSelected) BorderStroke(1.dp, borderColor) else null

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(42.dp)
                                .height(52.dp)
                                .background(color = bgColor, )
                                .let {
                                    if (borderStroke != null) {
                                        it.border(borderStroke, androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                                    } else {
                                        it
                                    }
                                }
                                .clickable {
                                    viewModel.selectDate(date)
                                }
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                color = if (isLogged) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayNum,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLogged) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Calorie and Macro Cards row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Calories Card
                    Card(
                        modifier = Modifier.weight(1f).wrapContentHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🔥", fontSize = 16.sp)
                                Text(
                                    text = "Calories",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    val foodVal = meals.sumOf { it.totalCalories }.roundToInt()
                                    Text(
                                        text = "$foodVal",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("Food", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "0",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("Exercise", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.Start) {
                                    val foodVal = meals.sumOf { it.totalCalories }.roundToInt()
                                    val remaining = calorieGoal - foodVal
                                    Text(
                                        text = "$remaining",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("Remaining", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Macros Card
                    Card(
                        modifier = Modifier.weight(1f).wrapContentHeight(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(16.dp)) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawArc(
                                            color = PrimaryTeal,
                                            startAngle = 0f,
                                            sweepAngle = 120f,
                                            useCenter = false,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                        drawArc(
                                            color = CoralPrimary,
                                            startAngle = 120f,
                                            sweepAngle = 120f,
                                            useCenter = false,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                        drawArc(
                                            color = AccentGold,
                                            startAngle = 240f,
                                            sweepAngle = 120f,
                                            useCenter = false,
                                            style = Stroke(width = 3.dp.toPx())
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Macros",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    val carbsVal = meals.sumOf { it.totalCarbs }.roundToInt()
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)) {
                                                append("$carbsVal")
                                            }
                                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)) {
                                                append("/$carbsGoalGrams")
                                            }
                                        }
                                    )
                                    Text("Carbs (g)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.Start) {
                                    val proteinVal = meals.sumOf { it.totalProtein }.roundToInt()
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)) {
                                                append("$proteinVal")
                                            }
                                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)) {
                                                append("/$proteinGoalGrams")
                                            }
                                        }
                                    )
                                    Text("Protein (g)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.Start) {
                                    val fatVal = meals.sumOf { it.totalFat }.roundToInt()
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)) {
                                                append("$fatVal")
                                            }
                                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)) {
                                                append("/$fatGoalGrams")
                                            }
                                        }
                                    )
                                    Text("Fat (g)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // ── Micronutrients card (daily totals) ────────────────────────────
            item {
                MicronutrientsCard(meals = meals)
            }

            // Water Tracker Card
            item {
                Flip7Card(modifier = Modifier.fillMaxWidth(),) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp)) {
                        Text(
                            text = "Water: ${currentWaterCups * 0.25}L",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(CoralPrimary, RoundedCornerShape(8.dp))
                                    .clickable { setWaterCups(currentWaterCups - 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Remove water",
                                    tint = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentWaterCups Cups",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${16 - currentWaterCups} Cups Remaining",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryTeal, RoundedCornerShape(8.dp))
                                    .clickable { setWaterCups(currentWaterCups + 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add water",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Logged items or simulated Yesterday details
            if (selectedDate == LocalDate(2026, 5, 19)) {
                // Yesterday meals simulation from screenshot 2
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card 1: Daal ke Farae (100 g)
                        Flip7Card(modifier = Modifier.fillMaxWidth(),) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Daal ke Farae (100 g)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Calories", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("150", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { 0.10f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("10%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Carbs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("20g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { 0.12f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("12%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Protein", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("8g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { 0.05f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("5%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Fat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("4g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { 0.20f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("20%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("11:53 am", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Card 2: AI parsed list
                        Flip7Card(modifier = Modifier.fillMaxWidth(),) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "120 gm dahi 200 gm daal cooked and 200 gm brown rice cooked",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                FoodItemDetailRow(
                                    name = "Dahi (120 g)",
                                    calories = "72",
                                    carbs = "4g",
                                    protein = "6g",
                                    fat = "4g"
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                FoodItemDetailRow(
                                    name = "Cooked Daal (200 g)",
                                    calories = "230",
                                    carbs = "35g",
                                    protein = "14g",
                                    fat = "2g"
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                FoodItemDetailRow(
                                    name = "Cooked Brown Rice (200 g)",
                                    calories = "220",
                                    carbs = "45g",
                                    protein = "5g",
                                    fat = "2g"
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TotalsColumnItem(title = "Calories", value = "522")
                                    TotalsColumnItem(title = "Carbs", value = "84g")
                                    TotalsColumnItem(title = "Protein", value = "25g")
                                    TotalsColumnItem(title = "Fat", value = "8g")
                                }
                            }
                        }
                    }
                }
            } else {
                // Today's list of meals
                if (meals.isNotEmpty()) {
                    items(meals) { meal ->
                        Flip7Card(modifier = Modifier.fillMaxWidth(),) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (meal.rawTextInput != null) {
                                            Text(
                                                text = meal.rawTextInput,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Text(
                                            text = meal.mealType,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Carbs: ${meal.totalCarbs.roundToInt()}g  Protein: ${meal.totalProtein.roundToInt()}g  Fat: ${meal.totalFat.roundToInt()}g",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${meal.totalCalories.roundToInt()} kcal",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        // Bookmark icon — tap to save this meal as a bookmark
                                        IconButton(
                                            onClick = { viewModel.saveLoggedMealAsBookmark(meal) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkBorder,
                                                contentDescription = "Save meal",
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog that shows detected food items with quantities and lets user
 * review/adjust quantities before confirming the log.
 */
@Composable
private fun FoodConfirmationDialog(
    originalText: String,
    initialFoodItems: List<FoodItemDto>,
    onConfirm: (List<FoodItemDto>, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var items by remember { mutableStateOf(initialFoodItems) }
    var saveAsBookmark by remember { mutableStateOf(false) }
    var bookmarkName by remember {
        mutableStateOf(
            initialFoodItems.firstOrNull()?.name?.take(25) ?: originalText.take(25)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Text(
                    text = "Food Analysis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$originalText\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Food items list
                items.forEachIndexed { index, item ->
                    FoodItemConfirmRow(
                        index = index,
                        item = item,
                        onItemChange = { updatedItem ->
                            items = items.toMutableList().also { list ->
                                list[index] = updatedItem
                            }
                        }
                    )
                    if (index < items.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Totals row
                val totalCal = items.sumOf { it.calories }.roundToInt()
                val totalProt = items.sumOf { it.protein }.roundToInt()
                val totalCarbs = items.sumOf { it.carbs }.roundToInt()
                val totalFat = items.sumOf { it.fat }.roundToInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TotalsColumnItem("Total Cal", "${totalCal} kcal")
                    TotalsColumnItem("Carbs", "${totalCarbs}g")
                    TotalsColumnItem("Protein", "${totalProt}g")
                    TotalsColumnItem("Fat", "${totalFat}g")
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // ── Micronutrients collapsible section ─────────────────────
                var showMicronutrients by remember { mutableStateOf(false) }
                val totalFiber     = items.sumOf { it.fiber }
                val totalSugar     = items.sumOf { it.sugar }
                val totalSodium    = items.sumOf { it.sodium }
                val totalPotassium = items.sumOf { it.potassium }
                val totalCalcium   = items.sumOf { it.calcium }
                val totalIron      = items.sumOf { it.iron }
                val totalVitaminC  = items.sumOf { it.vitaminC }
                val totalVitaminD  = items.sumOf { it.vitaminD }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMicronutrients = !showMicronutrients }
                        .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Micronutrients",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showMicronutrients) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showMicronutrients,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        val microRows = listOf(
                            Triple("Fiber",      "${totalFiber.roundToInt()}g",     PrimaryTeal),
                            Triple("Sugar",      "${totalSugar.roundToInt()}g",     CoralPrimary),
                            Triple("Sodium",     "${totalSodium.roundToInt()}mg",   AccentGold),
                            Triple("Potassium",  "${totalPotassium.roundToInt()}mg",SkyBlue),
                            Triple("Calcium",    "${totalCalcium.roundToInt()}mg",  PrimaryTeal),
                            Triple("Iron",       "${totalIron.roundToInt()}mg",     CoralPrimary),
                            Triple("Vitamin C",  "${totalVitaminC.roundToInt()}mg", AccentGold),
                            Triple("Vitamin D",  "${totalVitaminD.roundToInt()}µg", SkyBlue)
                        )
                        microRows.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { (label, value, color) ->
                                    Row(
                                        modifier = Modifier.weight(1f)
                                            .background(color.copy(alpha = 0.05f), androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(6.dp)
                                                .background(color, androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                                        }
                                    }
                                }
                                // pad if odd number
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { saveAsBookmark = !saveAsBookmark }
                        .background(
                            if (saveAsBookmark) Color(0xFFEEEEEE) else Color.Transparent,
                            androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (saveAsBookmark) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (saveAsBookmark) Color.Black else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save as bookmark",
                            fontSize = 13.sp,
                            color = if (saveAsBookmark) Color.Black else Color.Gray,
                            fontWeight = if (saveAsBookmark) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    Switch(
                        checked = saveAsBookmark,
                        onCheckedChange = { saveAsBookmark = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black
                        )
                    )
                }

                // Bookmark name field — shown when toggle is on
                if (saveAsBookmark) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bookmarkName,
                        onValueChange = { bookmarkName = it },
                        label = { Text("Bookmark name", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Flip7Button(
                        text = "Cancel",
                        variant = Flip7ButtonVariant.TEAL,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    Flip7Button(
                        text = "Log It ✓",
                        variant = Flip7ButtonVariant.PRIMARY_GOLD,
                        onClick = { onConfirm(items, saveAsBookmark, bookmarkName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet showing all saved/bookmarked meals for quick re-logging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkBottomSheet(
    bookmarks: List<BookmarkedMeal>,
    onDismiss: () -> Unit,
    onLogBookmark: (BookmarkedMeal) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
            ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Meals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap \"Log Now\" to instantly add to today",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            if (bookmarks.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = Color(0xFFBBBBBB),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved meals yet",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Log a meal and tap 🔖 to save it as a bookmark for quick daily reuse.",
                        fontSize = 13.sp,
                        color = Color(0xFFAAAAAA),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                bookmarks.forEach { bookmark ->
                    BookmarkCard(
                        bookmark = bookmark,
                        onLog = { onLogBookmark(bookmark) },
                        onDelete = { onDeleteBookmark(bookmark.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: BookmarkedMeal,
    onLog: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
                        modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bookmark.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${bookmark.totalCalories.roundToInt()} kcal  ·  C: ${bookmark.totalCarbs.roundToInt()}g  P: ${bookmark.totalProtein.roundToInt()}g  F: ${bookmark.totalFat.roundToInt()}g",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Log Now button
                Flip7Button(
                    text = "Log Now",
                    variant = Flip7ButtonVariant.PRIMARY_GOLD,
                    onClick = onLog,
                    modifier = Modifier.width(120.dp)
                )
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove bookmark",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodItemConfirmRow(
    index: Int,
    item: FoodItemDto,
    onItemChange: (FoodItemDto) -> Unit
) {
    // Parse quantity and unit
    val servingSizeClean = remember(index) { item.servingSize.trim() }
    val match = remember(index) { Regex("^([0-9.]+)\\s*(.*)$").matchEntire(servingSizeClean) }
    val qtyString = remember(index) { match?.groupValues?.get(1) ?: "1" }
    val unitString = remember(index) { match?.groupValues?.get(2)?.trim() ?: "piece" }

    var name by remember(index) { mutableStateOf(item.name) }
    var quantity by remember(index) { mutableStateOf(qtyString) }
    var unit by remember(index) { mutableStateOf(unitString) }
    var calories by remember(index) { mutableStateOf(item.calories.toString()) }
    var carbs by remember(index) { mutableStateOf(item.carbs.toString()) }
    var protein by remember(index) { mutableStateOf(item.protein.toString()) }
    var fat by remember(index) { mutableStateOf(item.fat.toString()) }

    // Baseline values for scaling (retains original values per unit)
    val qtyVal = qtyString.toDoubleOrNull() ?: 1.0
    val divisor = if (qtyVal > 0) qtyVal else 1.0
    var baseCaloriesPerUnit by remember(index) { mutableStateOf(item.calories / divisor) }
    var baseCarbsPerUnit by remember(index) { mutableStateOf(item.carbs / divisor) }
    var baseProteinPerUnit by remember(index) { mutableStateOf(item.protein / divisor) }
    var baseFatPerUnit by remember(index) { mutableStateOf(item.fat / divisor) }
    var baseFiberPerUnit by remember(index) { mutableStateOf(item.fiber / divisor) }
    var baseSugarPerUnit by remember(index) { mutableStateOf(item.sugar / divisor) }
    var baseSodiumPerUnit by remember(index) { mutableStateOf(item.sodium / divisor) }
    var basePotassiumPerUnit by remember(index) { mutableStateOf(item.potassium / divisor) }
    var baseCalciumPerUnit by remember(index) { mutableStateOf(item.calcium / divisor) }
    var baseIronPerUnit by remember(index) { mutableStateOf(item.iron / divisor) }
    var baseVitaminCPerUnit by remember(index) { mutableStateOf(item.vitaminC / divisor) }
    var baseVitaminDPerUnit by remember(index) { mutableStateOf(item.vitaminD / divisor) }

    var unitMenuExpanded by remember { mutableStateOf(false) }
    val units = listOf("g", "ml", "piece", "bowl", "cup", "tbsp", "tsp", "plate", "glass", "serving")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onItemChange(item.copy(name = it))
            },
            label = { Text("Food Name", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Qty, Unit, Calories Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quantity
            OutlinedTextField(
                value = quantity,
                onValueChange = { newValue ->
                    val sanitized = newValue.filter { it.isDigit() || it == '.' }
                    quantity = sanitized
                    val qtyDouble = sanitized.toDoubleOrNull() ?: 0.0
                    
                    val updatedCalories = baseCaloriesPerUnit * qtyDouble
                    val updatedCarbs = baseCarbsPerUnit * qtyDouble
                    val updatedProtein = baseProteinPerUnit * qtyDouble
                    val updatedFat = baseFatPerUnit * qtyDouble
                    
                    calories = if (qtyDouble > 0) "%.1f".format(updatedCalories) else ""
                    carbs = if (qtyDouble > 0) "%.1f".format(updatedCarbs) else ""
                    protein = if (qtyDouble > 0) "%.1f".format(updatedProtein) else ""
                    fat = if (qtyDouble > 0) "%.1f".format(updatedFat) else ""
                    
                    onItemChange(item.copy(
                        servingSize = "$sanitized $unit",
                        calories = updatedCalories,
                        carbs = updatedCarbs,
                        protein = updatedProtein,
                        fat = updatedFat,
                        fiber = baseFiberPerUnit * qtyDouble,
                        sugar = baseSugarPerUnit * qtyDouble,
                        sodium = baseSodiumPerUnit * qtyDouble,
                        potassium = basePotassiumPerUnit * qtyDouble,
                        calcium = baseCalciumPerUnit * qtyDouble,
                        iron = baseIronPerUnit * qtyDouble,
                        vitaminC = baseVitaminCPerUnit * qtyDouble,
                        vitaminD = baseVitaminDPerUnit * qtyDouble
                    ))
                },
                label = { Text("Qty", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Unit Dropdown
            Box(modifier = Modifier.weight(1.5f)) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().clickable { unitMenuExpanded = true },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    trailingIcon = {
                        IconButton(onClick = { unitMenuExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    units.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                unit = u
                                unitMenuExpanded = false
                                onItemChange(item.copy(servingSize = "$quantity $u"))
                            }
                        )
                    }
                }
            }

            // Calories
            OutlinedTextField(
                value = calories,
                onValueChange = { newValue ->
                    val sanitized = newValue.filter { c -> c.isDigit() || c == '.' }
                    calories = sanitized
                    val newCalDouble = sanitized.toDoubleOrNull() ?: 0.0
                    val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
                    val divisor = if (qtyDouble > 0) qtyDouble else 1.0
                    baseCaloriesPerUnit = newCalDouble / divisor
                    onItemChange(item.copy(calories = newCalDouble))
                },
                label = { Text("kcal", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1.2f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Macros Row (Carbs, Protein, Fat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Carbs
            OutlinedTextField(
                value = carbs,
                onValueChange = { newValue ->
                    val sanitized = newValue.filter { c -> c.isDigit() || c == '.' }
                    carbs = sanitized
                    val newCarbsDouble = sanitized.toDoubleOrNull() ?: 0.0
                    val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
                    val divisor = if (qtyDouble > 0) qtyDouble else 1.0
                    baseCarbsPerUnit = newCarbsDouble / divisor
                    onItemChange(item.copy(carbs = newCarbsDouble))
                },
                label = { Text("Carbs (g)", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Protein
            OutlinedTextField(
                value = protein,
                onValueChange = { newValue ->
                    val sanitized = newValue.filter { c -> c.isDigit() || c == '.' }
                    protein = sanitized
                    val newProteinDouble = sanitized.toDoubleOrNull() ?: 0.0
                    val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
                    val divisor = if (qtyDouble > 0) qtyDouble else 1.0
                    baseProteinPerUnit = newProteinDouble / divisor
                    onItemChange(item.copy(protein = newProteinDouble))
                },
                label = { Text("Prot (g)", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Fat
            OutlinedTextField(
                value = fat,
                onValueChange = { newValue ->
                    val sanitized = newValue.filter { c -> c.isDigit() || c == '.' }
                    fat = sanitized
                    val newFatDouble = sanitized.toDoubleOrNull() ?: 0.0
                    val qtyDouble = quantity.toDoubleOrNull() ?: 1.0
                    val divisor = if (qtyDouble > 0) qtyDouble else 1.0
                    baseFatPerUnit = newFatDouble / divisor
                    onItemChange(item.copy(fat = newFatDouble))
                },
                label = { Text("Fat (g)", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
private fun FoodItemDetailRow(
    name: String,
    calories: String,
    carbs: String,
    protein: String,
    fat: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MacroBadge(label = "Calories", value = calories)
            MacroBadge(label = "Carbs", value = carbs)
            MacroBadge(label = "Protein", value = protein)
            MacroBadge(label = "Fat", value = fat)
        }
    }
}

@Composable
private fun MacroBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label: $value",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TotalsColumnItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Micronutrients Dashboard Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MicronutrientsCard(meals: List<Meal>) {
    val fiber     = meals.sumOf { it.totalFiber }
    val sugar     = meals.sumOf { it.totalSugar }
    val sodium    = meals.sumOf { it.totalSodium }
    val potassium = meals.sumOf { it.totalPotassium }
    val calcium   = meals.sumOf { it.totalCalcium }
    val iron      = meals.sumOf { it.totalIron }
    val vitaminC  = meals.sumOf { it.totalVitaminC }
    val vitaminD  = meals.sumOf { it.totalVitaminD }

    var expanded by remember { mutableStateOf(true) }

    Card(
                        modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // Header row — tap to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Micronutrients",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val textColor = MaterialTheme.colorScheme.onSurface
                Column(modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
                    data class MicroItem(
                        val label: String,
                        val value: Double,
                        val rdi: Double,
                        val unit: String,
                        val color: Color
                    )

                    val microItems = listOf(
                        MicroItem("Fiber",     fiber,     25.0,   "g",  textColor),
                        MicroItem("Sugar",     sugar,     25.0,   "g",  textColor),
                        MicroItem("Sodium",    sodium,    2300.0, "mg", textColor),
                        MicroItem("Potassium", potassium, 3500.0, "mg", textColor),
                        MicroItem("Calcium",   calcium,   1000.0, "mg", textColor),
                        MicroItem("Iron",      iron,      18.0,   "mg", textColor),
                        MicroItem("Vitamin C", vitaminC,  90.0,   "mg", textColor),
                        MicroItem("Vitamin D", vitaminD,  20.0,   "µg", textColor)
                    )

                    microItems.forEachIndexed { idx, item ->
                        val progress = min(1f, (item.value / item.rdi).toFloat())
                        val pct = (progress * 100).roundToInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(item.color, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${"%.1f".format(item.value)}${item.unit}  •  $pct% RDI",
                                        fontSize = 12.sp,
                                        color = item.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(androidx.compose.foundation.shape.CircleShape),
                                    color = item.color,
                                    trackColor = item.color.copy(alpha = 0.1f),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }

                        if (idx < microItems.lastIndex) {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }
}
