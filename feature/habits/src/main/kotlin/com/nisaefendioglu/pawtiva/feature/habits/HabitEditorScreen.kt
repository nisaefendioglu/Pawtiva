package com.nisaefendioglu.pawtiva.feature.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.component.ErrorState
import com.nisaefendioglu.pawtiva.core.designsystem.component.LoadingState
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButton
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButtonStyle
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.designsystem.theme.habitContainerColor
import com.nisaefendioglu.pawtiva.core.domain.HabitValidator
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.HabitIcons

/** Public entry: create (habitId = 0) or edit a habit. Calls [onDone] after save, delete or close. */
@Composable
fun HabitEditorFeature(
    onDone: () -> Unit,
    viewModel: HabitEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentOnDone by rememberUpdatedState(onDone)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HabitEditorEvent.Saved, HabitEditorEvent.Deleted -> currentOnDone()
                HabitEditorEvent.SaveFailed ->
                    snackbarHostState.showSnackbar(context.getString(R.string.habit_editor_save_failed))
            }
        }
    }
    HabitEditorScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onClose = onDone,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onIconSelected = viewModel::onIconSelected,
        onColorSelected = viewModel::onColorSelected,
        onSave = viewModel::onSave,
        onDelete = viewModel::onDelete,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitEditorScreen(
    state: HabitEditorUiState,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onColorSelected: (HabitColor) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val colors = PawtivaTheme.colors
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(PawtivaIcons.Close, contentDescription = stringResource(R.string.habit_editor_close), tint = colors.textPrimary)
                }
                Text(
                    text = stringResource(if (state.isEditing) R.string.habit_editor_title_edit else R.string.habit_editor_title_new),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            }

            when {
                state.isLoading -> LoadingState(Modifier.fillMaxSize())
                state.notFound -> ErrorState(message = stringResource(R.string.habit_editor_not_found), onRetry = null)
                else -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    PreviewBubble(icon = state.icon, color = state.color)

                    val error = state.visibleNameError
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.habit_editor_name)) },
                        singleLine = true,
                        isError = error != null,
                        supportingText = {
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    text = when (error) {
                                        HabitValidator.NameError.BLANK -> stringResource(R.string.habit_editor_name_blank)
                                        HabitValidator.NameError.TOO_LONG ->
                                            stringResource(R.string.habit_editor_name_too_long, HabitValidator.NAME_MAX_LENGTH)
                                        null -> ""
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                Text("${state.name.trim().length}/${HabitValidator.NAME_MAX_LENGTH}")
                            }
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = state.description,
                        onValueChange = onDescriptionChange,
                        label = { Text(stringResource(R.string.habit_editor_description)) },
                        placeholder = { Text(stringResource(R.string.habit_editor_description_hint)) },
                        supportingText = {
                            Text(
                                text = "${state.description.length}/${HabitValidator.DESCRIPTION_MAX_LENGTH}",
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    SectionLabel(stringResource(R.string.habit_editor_icon))
                    val iconLabel = stringResource(R.string.habit_editor_icon_option)
                    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }
                    // A device emoji chosen earlier stays visible as its own tile.
                    val iconChoices = if (state.icon in HabitIcons.all) HabitIcons.all else HabitIcons.all + state.icon
                    FlowRow(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        iconChoices.forEach { icon ->
                            val selected = icon == state.icon
                            Surface(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onIconSelected(icon) })
                                    .semantics { contentDescription = "$iconLabel $icon" },
                                shape = MaterialTheme.shapes.medium,
                                color = if (selected) colors.primaryContainer else colors.surface,
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.border),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(icon, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                        // Last tile: pick any emoji from the device keyboard.
                        Surface(
                            onClick = { showEmojiPicker = true },
                            modifier = Modifier.size(52.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = colors.surfaceMuted,
                            border = BorderStroke(1.dp, colors.primary),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PawtivaIcons.Add,
                                    contentDescription = stringResource(R.string.habit_editor_custom_icon),
                                    tint = colors.primary,
                                )
                            }
                        }
                    }
                    if (showEmojiPicker) {
                        CustomEmojiDialog(
                            onConfirm = { emoji ->
                                onIconSelected(emoji)
                                showEmojiPicker = false
                            },
                            onDismiss = { showEmojiPicker = false },
                        )
                    }

                    SectionLabel(stringResource(R.string.habit_editor_color))
                    FlowRow(
                        modifier = Modifier.selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        HabitColor.entries.forEach { color ->
                            val selected = color == state.color
                            val label = stringResource(colorLabel(color))
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onColorSelected(color) }),
                                shape = CircleShape,
                                color = habitContainerColor(color),
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.border),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (selected) {
                                        Icon(PawtivaIcons.Check, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.size(Spacing.xxs))
                                    }
                                    Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.md))
                }
            }

            if (!state.isLoading && !state.notFound) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    PawtivaButton(
                        text = stringResource(R.string.habit_editor_save),
                        onClick = onSave,
                        enabled = state.canSave,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.isEditing) {
                        PawtivaButton(
                            text = stringResource(R.string.habits_delete),
                            onClick = { confirmDelete = true },
                            style = PawtivaButtonStyle.Text,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (confirmDelete) {
        DeleteHabitDialog(
            habitName = state.name.trim(),
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun PreviewBubble(icon: String, color: HabitColor) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = habitContainerColor(color),
            border = BorderStroke(1.dp, PawtivaTheme.colors.border),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, style = MaterialTheme.typography.displaySmall)
            }
        }
    }
}

/** Lets the user type one emoji with the system keyboard's emoji tab; anything else is rejected. */
@Composable
private fun CustomEmojiDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = PawtivaTheme.colors
    var input by rememberSaveable { mutableStateOf("") }
    val emoji = HabitIcons.normalizeCustom(input)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(stringResource(R.string.habit_editor_custom_icon_title), color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(R.string.habit_editor_custom_icon_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(CUSTOM_ICON_MAX_CHARS) },
                    label = { Text(stringResource(R.string.habit_editor_custom_icon_label)) },
                    singleLine = true,
                    isError = input.isNotBlank() && emoji == null,
                    supportingText = {
                        if (input.isNotBlank() && emoji == null) {
                            Text(stringResource(R.string.habit_editor_custom_icon_error), color = colors.error)
                        }
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { emoji?.let(onConfirm) }),
                    colors = fieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
        },
        confirmButton = {
            PawtivaButton(
                text = stringResource(R.string.habit_editor_custom_icon_use),
                onClick = { emoji?.let(onConfirm) },
                enabled = emoji != null,
            )
        },
        dismissButton = {
            PawtivaButton(
                text = stringResource(R.string.habits_cancel),
                onClick = onDismiss,
                style = PawtivaButtonStyle.Text,
            )
        },
    )
}

/** Room for one emoji made of several code points (skin tone, joined emoji). */
private const val CUSTOM_ICON_MAX_CHARS = 16

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = PawtivaTheme.colors.textPrimary,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PawtivaTheme.colors.primary,
    unfocusedBorderColor = PawtivaTheme.colors.border,
    focusedLabelColor = PawtivaTheme.colors.primary,
    cursorColor = PawtivaTheme.colors.primary,
)

internal fun colorLabel(color: HabitColor): Int = when (color) {
    HabitColor.LAVENDER -> R.string.habit_color_lavender
    HabitColor.MINT -> R.string.habit_color_mint
    HabitColor.PEACH -> R.string.habit_color_peach
    HabitColor.PINK -> R.string.habit_color_pink
    HabitColor.SKY -> R.string.habit_color_sky
    HabitColor.SUN -> R.string.habit_color_sun
}
