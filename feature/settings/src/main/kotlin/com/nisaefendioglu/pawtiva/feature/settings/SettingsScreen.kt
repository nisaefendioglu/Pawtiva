package com.nisaefendioglu.pawtiva.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.XPProgressBar
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetDisplay
import com.nisaefendioglu.pawtiva.core.designsystem.pet.defaultName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.displayName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.petContentDescription
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.designsystem.theme.currentLocale
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatDate
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatNumber
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Callbacks from the settings UI. Defaults make previews and tests short. */
data class SettingsActions(
    val onLanguage: (AppLanguage) -> Unit = {},
    val onTheme: (ThemeMode) -> Unit = {},
    val onDynamicColor: (Boolean) -> Unit = {},
    val onDateFormat: (DateFormatPreference) -> Unit = {},
    val onChangePet: (PetType) -> Unit = {},
    /** Returns false when the name is rejected (too long). */
    val onRenamePet: (String) -> Boolean = { true },
    val onReduceAnimations: (Boolean) -> Unit = {},
    val onRemindersToggle: (Boolean) -> Unit = {},
    val onReminderTime: (LocalTime) -> Unit = {},
    val onExport: () -> Unit = {},
    val onReset: () -> Unit = {},
    val onFeedback: () -> Unit = {},
)

internal enum class SettingsDialog { LANGUAGE, THEME, DATE_FORMAT, PET, RENAME, TIME, RESET, RESET_FINAL, PRIVACY, OFFLINE }

/** Sample date used to preview each date format. */
private val SampleDate: LocalDate = LocalDate.of(2026, 12, 31)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    actions: SettingsActions,
    onBack: () -> Unit,
    versionName: String,
    dynamicColorSupported: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val colors = PawtivaTheme.colors
    var dialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }
    val prefs = state.preferences
    val locale = currentLocale
    val petType = prefs.petType ?: PetType.CAT
    val petName = prefs.petName ?: petType.defaultName()

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PawtivaIcons.Back, contentDescription = stringResource(R.string.settings_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            ProfileCard(state = state, petType = petType, petName = petName, animate = !prefs.reducePetAnimations)

            SettingsSection(stringResource(R.string.settings_section_general)) {
                ClickRow(stringResource(R.string.settings_language), languageLabel(state.language)) { dialog = SettingsDialog.LANGUAGE }
                RowDivider()
                ClickRow(stringResource(R.string.settings_theme), themeLabel(prefs.themeMode)) { dialog = SettingsDialog.THEME }
                if (dynamicColorSupported) {
                    RowDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_dynamic_color),
                        subtitle = stringResource(R.string.settings_dynamic_color_description),
                        checked = prefs.dynamicColor,
                        onCheckedChange = actions.onDynamicColor,
                    )
                }
                RowDivider()
                ClickRow(
                    stringResource(R.string.settings_date_format),
                    dateFormatLabel(prefs.dateFormat),
                ) { dialog = SettingsDialog.DATE_FORMAT }
            }

            SettingsSection(stringResource(R.string.settings_section_pet)) {
                ClickRow(stringResource(R.string.settings_change_pet), petType.displayName()) { dialog = SettingsDialog.PET }
                RowDivider()
                ClickRow(stringResource(R.string.settings_pet_name), petName) { dialog = SettingsDialog.RENAME }
                RowDivider()
                SwitchRow(
                    title = stringResource(R.string.settings_reduce_animations),
                    subtitle = stringResource(R.string.settings_reduce_animations_description),
                    checked = prefs.reducePetAnimations,
                    onCheckedChange = actions.onReduceAnimations,
                )
            }

            SettingsSection(stringResource(R.string.settings_section_notifications)) {
                SwitchRow(
                    title = stringResource(R.string.settings_daily_reminder),
                    subtitle = stringResource(R.string.settings_daily_reminder_description),
                    checked = prefs.remindersEnabled,
                    onCheckedChange = actions.onRemindersToggle,
                )
                RowDivider()
                ClickRow(
                    title = stringResource(R.string.settings_reminder_time),
                    value = prefs.reminderTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)),
                    enabled = prefs.remindersEnabled,
                ) { dialog = SettingsDialog.TIME }
            }

            SettingsSection(stringResource(R.string.settings_section_data)) {
                ClickRow(stringResource(R.string.settings_export), stringResource(R.string.settings_export_description), onClick = actions.onExport)
                RowDivider()
                ClickRow(stringResource(R.string.settings_privacy)) { dialog = SettingsDialog.PRIVACY }
                RowDivider()
                ClickRow(stringResource(R.string.settings_offline)) { dialog = SettingsDialog.OFFLINE }
                RowDivider()
                ClickRow(
                    title = stringResource(R.string.settings_reset),
                    value = stringResource(R.string.settings_reset_description),
                    destructive = true,
                ) { dialog = SettingsDialog.RESET }
            }

            SettingsSection(stringResource(R.string.settings_section_about)) {
                Text(
                    text = stringResource(R.string.settings_about_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                )
                RowDivider()
                InfoRow(stringResource(R.string.settings_version), versionName)
                RowDivider()
                InfoRow(stringResource(R.string.settings_developer), stringResource(R.string.settings_developer_name))
                RowDivider()
                ClickRow(stringResource(R.string.settings_feedback), onClick = actions.onFeedback)
            }
            Spacer(Modifier.size(Spacing.lg))
        }
    }

    val close = { dialog = null }
    when (dialog) {
        SettingsDialog.LANGUAGE -> ChoiceDialog(
            title = stringResource(R.string.settings_language),
            options = AppLanguage.entries.map { it to languageLabel(it) },
            selected = state.language,
            onSelect = { actions.onLanguage(it); close() },
            onDismiss = close,
        )
        SettingsDialog.THEME -> ChoiceDialog(
            title = stringResource(R.string.settings_theme),
            options = ThemeMode.entries.map { it to themeLabel(it) },
            selected = prefs.themeMode,
            onSelect = { actions.onTheme(it); close() },
            onDismiss = close,
        )
        SettingsDialog.DATE_FORMAT -> ChoiceDialog(
            title = stringResource(R.string.settings_date_format),
            options = DateFormatPreference.entries.map { it to dateFormatLabel(it) },
            selected = prefs.dateFormat,
            onSelect = { actions.onDateFormat(it); close() },
            onDismiss = close,
        )
        SettingsDialog.PET -> PetChangeDialog(
            current = petType,
            onConfirm = { actions.onChangePet(it); close() },
            onDismiss = close,
        )
        SettingsDialog.RENAME -> RenameDialog(
            initial = prefs.petName.orEmpty(),
            defaultName = petType.defaultName(),
            onSave = { name -> if (actions.onRenamePet(name)) close() },
            onDismiss = close,
        )
        SettingsDialog.TIME -> ReminderTimeDialog(
            initial = prefs.reminderTime,
            onConfirm = { actions.onReminderTime(it); close() },
            onDismiss = close,
        )
        SettingsDialog.RESET -> ConfirmDialog(
            title = stringResource(R.string.settings_reset_title),
            body = stringResource(R.string.settings_reset_body),
            confirmLabel = stringResource(R.string.settings_continue),
            destructive = false,
            onConfirm = { dialog = SettingsDialog.RESET_FINAL },
            onDismiss = close,
        )
        SettingsDialog.RESET_FINAL -> ConfirmDialog(
            title = stringResource(R.string.settings_reset_final_title),
            body = stringResource(R.string.settings_reset_final_body),
            confirmLabel = stringResource(R.string.settings_reset_confirm),
            destructive = true,
            onConfirm = { close(); actions.onReset() },
            onDismiss = close,
        )
        SettingsDialog.PRIVACY -> InfoDialog(
            title = stringResource(R.string.settings_privacy),
            body = stringResource(R.string.settings_privacy_body),
            onDismiss = close,
        )
        SettingsDialog.OFFLINE -> InfoDialog(
            title = stringResource(R.string.settings_offline),
            body = stringResource(R.string.settings_offline_body),
            onDismiss = close,
        )
        null -> Unit
    }
}

@Composable
private fun ProfileCard(state: SettingsUiState, petType: PetType, petName: String, animate: Boolean) {
    val colors = PawtivaTheme.colors
    val progress = state.progress
    val locale = currentLocale
    PawtivaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetDisplay(
                appearance = PetAppearance(type = petType, mood = PetMood.HAPPY),
                contentDescription = petContentDescription(petType, petName, PetMood.HAPPY),
                modifier = Modifier.size(80.dp),
                animate = animate,
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.settings_profile_level, progress.level),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.primary,
                )
                Spacer(Modifier.size(Spacing.xs))
                XPProgressBar(
                    progress = progress.levelFraction,
                    contentDescription = stringResource(
                        R.string.settings_profile_xp_description,
                        progress.level,
                        progress.xpIntoLevel,
                        progress.xpPerLevel,
                    ),
                )
                Spacer(Modifier.size(Spacing.xxs))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(R.string.settings_profile_xp, progress.xpIntoLevel, progress.xpPerLevel),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = stringResource(R.string.settings_profile_coins, formatNumber(progress.balance, locale)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.size(Spacing.sm))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = PawtivaTheme.colors.textSecondary,
        modifier = Modifier
            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs)
            .semantics { heading() },
    )
    PawtivaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        content()
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = PawtivaTheme.colors.border, modifier = Modifier.padding(horizontal = Spacing.md))
}

@Composable
private fun ClickRow(
    title: String,
    value: String? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = PawtivaTheme.colors
    val titleColor = when {
        !enabled -> colors.onDisabled
        destructive -> colors.error
        else -> colors.textPrimary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) colors.textSecondary else colors.onDisabled,
            )
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    val colors = PawtivaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .clearAndSetSemantics { contentDescription = "$title, $value" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = PawtivaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.width(Spacing.sm))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceMuted,
                uncheckedBorderColor = colors.textSecondary,
            ),
        )
    }
}

@Composable
private fun languageLabel(language: AppLanguage): String = stringResource(
    when (language) {
        AppLanguage.SYSTEM -> R.string.settings_language_system
        AppLanguage.TURKISH -> R.string.settings_language_turkish
        AppLanguage.ENGLISH -> R.string.settings_language_english
    },
)

@Composable
private fun themeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    },
)

@Composable
private fun dateFormatLabel(format: DateFormatPreference): String {
    val sample = formatDate(SampleDate, format, currentLocale)
    return if (format == DateFormatPreference.SYSTEM) stringResource(R.string.settings_date_format_system, sample) else sample
}
