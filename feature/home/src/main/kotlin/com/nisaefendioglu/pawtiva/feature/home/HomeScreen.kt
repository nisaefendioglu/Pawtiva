package com.nisaefendioglu.pawtiva.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.component.DailyProgressCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.EmptyState
import com.nisaefendioglu.pawtiva.core.designsystem.component.ErrorState
import com.nisaefendioglu.pawtiva.core.designsystem.component.HabitCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.LoadingState
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButton
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.SectionHeader
import com.nisaefendioglu.pawtiva.core.designsystem.component.XPProgressBar
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetDisplay
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetReaction
import com.nisaefendioglu.pawtiva.core.designsystem.pet.defaultName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.displayName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.petContentDescription
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.designsystem.theme.currentLocale
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatDate
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatNumber
import com.nisaefendioglu.pawtiva.core.designsystem.theme.habitContainerColor
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import java.time.LocalDate
import java.time.format.TextStyle

/** Public entry used by the app's navigation. */
@Composable
fun HomeFeature(
    onOpenSettings: () -> Unit,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var reaction by remember { mutableStateOf(PetReaction.HOP) }
    var reactionKey by rememberSaveable { mutableIntStateOf(0) }
    var levelUp by rememberSaveable { mutableStateOf<Int?>(null) }
    val petName = state.petName ?: state.petType.defaultName()
    val currentPetName by rememberUpdatedState(petName)
    val currentDateFormat by rememberUpdatedState(state.dateFormat)
    val locale = currentLocale

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.Rewarded -> {
                    reaction = PetReaction.HOP
                    reactionKey++
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        if (event.coins > 0) {
                            context.getString(R.string.home_screen_reward_snackbar, event.xp, event.coins)
                        } else {
                            // No coins when this habit-day already paid out before an undo.
                            context.getString(R.string.home_screen_reward_xp_only, event.xp)
                        },
                    )
                }
                is HomeEvent.DayCompleted -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    if (event.isToday) {
                        // The pet's mood follows today, so it only celebrates today's full set.
                        reaction = PetReaction.CELEBRATE
                        reactionKey++
                        snackbarHostState.showSnackbar(context.getString(R.string.home_screen_day_done, currentPetName))
                    } else {
                        val day = formatDate(event.date, currentDateFormat, locale)
                        snackbarHostState.showSnackbar(context.getString(R.string.home_screen_past_day_done, day))
                    }
                }
                is HomeEvent.LeveledUp -> {
                    reaction = PetReaction.CELEBRATE
                    reactionKey++
                    levelUp = event.level
                }
                HomeEvent.Undone -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(context.getString(R.string.home_screen_undone_snackbar))
                }
                HomeEvent.UpdateFailed -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(context.getString(R.string.home_screen_error_generic))
                }
            }
        }
    }

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        reaction = reaction,
        reactionKey = reactionKey.takeIf { it > 0 },
        onPetTapped = {
            reaction = PetReaction.HOP
            reactionKey++
        },
        onSelectDate = viewModel::onSelectDate,
        onToggleHabit = viewModel::onToggleHabit,
        onOpenSettings = onOpenSettings,
        onAddHabit = onAddHabit,
        onEditHabit = onEditHabit,
    )

    levelUp?.let { level ->
        LevelUpDialog(
            level = level,
            appearance = PetAppearance(state.petType, PetMood.CELEBRATING, state.equipped),
            petName = petName,
            onDismiss = { levelUp = null },
        )
    }
}

/** Stateless home screen, used directly by tests. */
@Composable
fun HomeScreen(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onSelectDate: (LocalDate) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    reaction: PetReaction = PetReaction.HOP,
    reactionKey: Any? = null,
    onPetTapped: () -> Unit = {},
) {
    val colors = PawtivaTheme.colors
    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.isLoading && !state.loadFailed && state.habits.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddHabit,
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    icon = { Icon(PawtivaIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.home_screen_add_habit)) },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding).fillMaxSize())
            state.loadFailed -> ErrorState(
                message = stringResource(R.string.home_screen_load_error),
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            else -> HomeContent(
                state = state,
                contentPadding = padding,
                reaction = reaction,
                reactionKey = reactionKey,
                onPetTapped = onPetTapped,
                onSelectDate = onSelectDate,
                onToggleHabit = onToggleHabit,
                onOpenSettings = onOpenSettings,
                onAddHabit = onAddHabit,
                onEditHabit = onEditHabit,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    contentPadding: PaddingValues,
    reaction: PetReaction,
    reactionKey: Any?,
    onPetTapped: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
) {
    val locale = currentLocale
    val completedText = stringResource(R.string.home_screen_completed)
    val notCompletedText = stringResource(R.string.home_screen_not_completed)
    val editLabel = stringResource(R.string.home_screen_edit_habit)
    LazyColumn(
        contentPadding = PaddingValues(
            start = Spacing.md,
            end = Spacing.md,
            top = contentPadding.calculateTopPadding() + Spacing.md,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            Header(state = state, onOpenSettings = onOpenSettings)
        }
        item(key = "pet") {
            PetCard(state = state, reaction = reaction, reactionKey = reactionKey, onPetTapped = onPetTapped)
        }
        item(key = "days") {
            DayStrip(state = state, onSelectDate = onSelectDate)
        }
        if (state.habits.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    title = stringResource(R.string.home_screen_empty_title),
                    message = stringResource(R.string.home_screen_empty_message),
                    actionLabel = stringResource(R.string.home_screen_add_habit),
                    onAction = onAddHabit,
                )
            }
        } else {
            item(key = "progress") {
                DailyProgressCard(completed = state.completedCount, total = state.totalCount)
            }
            item(key = "section") {
                val title = if (state.isToday) {
                    stringResource(R.string.home_screen_habits_today)
                } else {
                    stringResource(R.string.home_screen_habits_for_date, formatDate(state.selectedDate, state.dateFormat, locale))
                }
                Column {
                    SectionHeader(title = title)
                    if (!state.isToday) {
                        Text(
                            text = stringResource(R.string.home_screen_past_day_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = PawtivaTheme.colors.textSecondary,
                        )
                    }
                }
            }
            items(state.habits, key = { it.habit.id }) { daily ->
                HabitCard(
                    icon = daily.habit.icon,
                    name = daily.habit.name,
                    description = daily.habit.description.ifBlank { null },
                    completed = daily.completed,
                    rewardLabel = stringResource(
                        if (daily.completed) R.string.home_screen_reward_earned else R.string.home_screen_reward_chip,
                        state.xpPerCompletion,
                    ),
                    stateDescription = if (daily.completed) completedText else notCompletedText,
                    containerColor = habitContainerColor(daily.habit.color),
                    onToggle = { onToggleHabit(daily.habit.id) },
                    enabled = daily.habit.id !in state.inFlight,
                    modifier = Modifier
                        .animateItem()
                        .semantics {
                            customActions = listOf(
                                CustomAccessibilityAction(editLabel) {
                                    onEditHabit(daily.habit.id)
                                    true
                                },
                            )
                        },
                )
            }
        }
    }
}

@Composable
private fun Header(state: HomeUiState, onOpenSettings: () -> Unit) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            val greeting = when (greetingFor(state.hourOfDay)) {
                Greeting.MORNING -> R.string.home_screen_greeting_morning
                Greeting.AFTERNOON -> R.string.home_screen_greeting_afternoon
                Greeting.EVENING -> R.string.home_screen_greeting_evening
                Greeting.NIGHT -> R.string.home_screen_greeting_night
            }
            Text(
                text = stringResource(greeting),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = formatDate(state.today, state.dateFormat, locale),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
        // One clear entry point: profile, reminders and every preference live in Settings.
        FilledTonalIconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
            ),
        ) {
            Icon(
                imageVector = PawtivaIcons.Settings,
                contentDescription = stringResource(R.string.home_screen_settings),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun PetCard(state: HomeUiState, reaction: PetReaction, reactionKey: Any?, onPetTapped: () -> Unit) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val petName = state.petName ?: state.petType.defaultName()
    val progress = state.progress
    PawtivaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetDisplay(
                appearance = PetAppearance(state.petType, state.mood, state.equipped),
                contentDescription = petContentDescription(state.petType, petName, state.mood),
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = stringResource(R.string.home_screen_pet_tap_label), onClick = onPetTapped),
                animate = !state.reducePetAnimations,
                reaction = reaction,
                reactionKey = reactionKey,
            )
            Spacer(Modifier.size(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(petName, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text(
                    text = stringResource(R.string.home_screen_mood, state.mood.displayName()),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = stringResource(R.string.home_screen_level, progress.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(Spacing.xxs))
                XPProgressBar(
                    progress = progress.levelFraction,
                    contentDescription = stringResource(
                        R.string.home_screen_xp_description,
                        progress.level,
                        progress.xpIntoLevel,
                        progress.xpPerLevel,
                    ),
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = stringResource(R.string.home_screen_xp, progress.xpIntoLevel, progress.xpPerLevel),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(PawtivaIcons.Coin, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(Spacing.xxs))
                    Text(
                        text = pluralStringResource(
                            R.plurals.home_screen_coins,
                            progress.balance,
                            formatNumber(progress.balance, locale),
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayStrip(state: HomeUiState, onSelectDate: (LocalDate) -> Unit) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val todayLabel = stringResource(R.string.home_screen_today)
    Column {
        SectionHeader(title = stringResource(R.string.home_screen_days_title))
        Spacer(Modifier.height(Spacing.xs))
        // Cells share the width but never shrink below a 48dp touch target; on narrow screens or
        // with large fonts the strip scrolls instead of squeezing the labels.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val count = state.days.size.coerceAtLeast(1)
        val cellWidth = maxOf(48.dp, (maxWidth - Spacing.xxs * (count - 1)) / count)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            state.days.forEach { date ->
                val selected = date == state.selectedDate
                val isToday = date == state.today
                val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                val fullLabel = formatDate(date, state.dateFormat, locale) + if (isToday) ", $todayLabel" else ""
                Surface(
                    modifier = Modifier
                        .width(cellWidth)
                        .clip(MaterialTheme.shapes.medium)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelectDate(date) })
                        .semantics { contentDescription = fullLabel },
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) colors.primaryContainer else colors.surface,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.border),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = Spacing.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = weekday,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) colors.onPrimaryContainer else colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) colors.onPrimaryContainer else colors.textPrimary,
                        )
                        // A dot marks today, independent of the selection color.
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary),
                            )
                        } else {
                            Spacer(Modifier.padding(top = 2.dp).size(5.dp))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun LevelUpDialog(level: Int, appearance: PetAppearance, petName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PawtivaButton(text = stringResource(R.string.home_screen_level_up_button), onClick = onDismiss)
        },
        title = {
            Text(
                text = stringResource(R.string.home_screen_level_up_title, level),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                PetDisplay(
                    appearance = appearance,
                    contentDescription = petContentDescription(appearance.type, petName, PetMood.CELEBRATING),
                    modifier = Modifier.size(140.dp),
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(R.string.home_screen_level_up_message, petName),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        containerColor = PawtivaTheme.colors.surface,
    )
}
