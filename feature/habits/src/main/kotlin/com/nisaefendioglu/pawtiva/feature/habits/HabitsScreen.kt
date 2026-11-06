package com.nisaefendioglu.pawtiva.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.component.EmptyState
import com.nisaefendioglu.pawtiva.core.designsystem.component.ErrorState
import com.nisaefendioglu.pawtiva.core.designsystem.component.LoadingState
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButton
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButtonStyle
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaCard
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.designsystem.theme.habitContainerColor
import com.nisaefendioglu.pawtiva.core.model.Habit

/** Public entry: the "Habits" tab. */
@Composable
fun HabitsFeature(
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HabitsScreen(state = state, onAddHabit = onAddHabit, onEditHabit = onEditHabit, onDelete = viewModel::onDelete)
}

@Composable
fun HabitsScreen(
    state: HabitsUiState,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PawtivaTheme.colors
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }
    val hasHabits = state is HabitsUiState.Loaded && state.habits.isNotEmpty()

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        floatingActionButton = {
            if (hasHabits) {
                ExtendedFloatingActionButton(
                    onClick = onAddHabit,
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    icon = { Icon(PawtivaIcons.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.habits_add)) },
                )
            }
        },
    ) { padding ->
        when (state) {
            HabitsUiState.Loading -> LoadingState(Modifier.padding(padding).fillMaxSize())
            HabitsUiState.Error -> ErrorState(
                message = stringResource(R.string.habits_load_error),
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            is HabitsUiState.Loaded -> if (state.habits.isEmpty()) {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = stringResource(R.string.habits_empty_title),
                        message = stringResource(R.string.habits_empty_message),
                        actionLabel = stringResource(R.string.habits_add),
                        onAction = onAddHabit,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Spacing.md,
                        end = Spacing.md,
                        top = padding.calculateTopPadding() + Spacing.md,
                        bottom = padding.calculateBottomPadding() + 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(key = "title") {
                        Text(
                            text = stringResource(R.string.habits_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    items(state.habits, key = { it.id }) { habit ->
                        HabitRow(
                            habit = habit,
                            onEdit = { onEditHabit(habit.id) },
                            onDelete = { pendingDelete = habit.id },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    val deleting = (state as? HabitsUiState.Loaded)?.habits?.firstOrNull { it.id == pendingDelete }
    if (deleting != null) {
        DeleteHabitDialog(
            habitName = deleting.name,
            onConfirm = {
                onDelete(deleting.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun HabitRow(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PawtivaTheme.colors
    PawtivaCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        containerColor = habitContainerColor(habit.color),
        contentPadding = PaddingValues(start = Spacing.md, top = Spacing.sm, end = Spacing.xs, bottom = Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(habit.icon, style = MaterialTheme.typography.titleLarge)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.sm),
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            // The whole card opens the editor, so only delete needs its own control.
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = PawtivaIcons.Delete,
                    contentDescription = stringResource(R.string.habits_delete_named, habit.name),
                    tint = colors.error,
                )
            }
        }
    }
}

@Composable
internal fun DeleteHabitDialog(habitName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.habits_delete_title, habitName)) },
        text = { Text(stringResource(R.string.habits_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.habits_delete_confirm), color = PawtivaTheme.colors.error)
            }
        },
        dismissButton = {
            PawtivaButton(
                text = stringResource(R.string.habits_cancel),
                onClick = onDismiss,
                style = PawtivaButtonStyle.Text,
            )
        },
        containerColor = PawtivaTheme.colors.surface,
    )
}
