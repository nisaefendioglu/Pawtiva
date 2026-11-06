package com.nisaefendioglu.pawtiva.feature.settings

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetDisplay
import com.nisaefendioglu.pawtiva.core.designsystem.pet.defaultName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.displayName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.petContentDescription
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import java.time.LocalTime

@Composable
internal fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { (value, label) ->
                    RadioRow(label = label, selected = value == selected, onClick = { onSelect(value) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
        },
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = PawtivaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

@Composable
internal fun PetChangeDialog(current: PetType, onConfirm: (PetType) -> Unit, onDismiss: () -> Unit) {
    var choice by rememberSaveable { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_change_pet)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    PetType.entries.forEach { type ->
                        val isSelected = type == choice
                        Column(
                            modifier = Modifier
                                .selectable(selected = isSelected, role = Role.RadioButton, onClick = { choice = type })
                                .padding(Spacing.xs),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val mood = if (isSelected) PetMood.HAPPY else PetMood.NORMAL
                            PetDisplay(
                                appearance = PetAppearance(type, mood),
                                contentDescription = petContentDescription(type, type.defaultName(), mood),
                                modifier = Modifier.size(84.dp),
                                animate = false,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isSelected, onClick = null)
                                Text(type.displayName(), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.settings_pet_change_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PawtivaTheme.colors.textSecondary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(choice) }, enabled = choice != current) {
                Text(stringResource(R.string.settings_pet_change_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        },
    )
}

@Composable
internal fun RenameDialog(initial: String, defaultName: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    val max = UserPreferences.PET_NAME_MAX_LENGTH
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_rename_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.replace("\n", "").take(max) },
                singleLine = true,
                label = { Text(stringResource(R.string.settings_pet_name)) },
                placeholder = { Text(defaultName) },
                supportingText = {
                    Column {
                        Text(stringResource(R.string.settings_rename_hint, defaultName))
                        Text(
                            text = stringResource(R.string.settings_rename_counter, name.length, max),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }) { Text(stringResource(R.string.settings_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderTimeDialog(initial: LocalTime, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_time_picker_title)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.settings_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        },
    )
}

@Composable
internal fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (destructive) PawtivaTheme.colors.error else PawtivaTheme.colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        },
    )
}

@Composable
internal fun InfoDialog(title: String, body: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(body, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.size(Spacing.xxs))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_close)) }
        },
    )
}
