package com.nisaefendioglu.pawtiva.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.pet.PetIllustration
import com.nisaefendioglu.pawtiva.core.designsystem.pet.defaultName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.displayName
import com.nisaefendioglu.pawtiva.core.designsystem.pet.petContentDescription
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences

@Composable
private fun StepTitle(title: String, subtitle: String? = null) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = PawtivaTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() },
    )
    if (subtitle != null) {
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = PawtivaTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(Spacing.lg))
}

@Composable
internal fun WelcomeStep() {
    Spacer(Modifier.height(Spacing.lg))
    Row(verticalAlignment = Alignment.Bottom) {
        val catName = PetType.CAT.defaultName()
        val dogName = PetType.DOG.defaultName()
        PetIllustration(
            type = PetType.CAT,
            mood = PetMood.HAPPY,
            contentDescription = petContentDescription(PetType.CAT, catName, PetMood.HAPPY),
            modifier = Modifier.size(150.dp),
        )
        PetIllustration(
            type = PetType.DOG,
            mood = PetMood.EXCITED,
            contentDescription = petContentDescription(PetType.DOG, dogName, PetMood.EXCITED),
            modifier = Modifier.size(150.dp),
        )
    }
    Spacer(Modifier.height(Spacing.xl))
    StepTitle(
        title = stringResource(R.string.onboarding_welcome_title),
        subtitle = stringResource(R.string.onboarding_welcome_tagline),
    )
}

@Composable
internal fun IntroStep() {
    StepTitle(title = stringResource(R.string.onboarding_intro_title))
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        FeatureRow(PawtivaIcons.Habits, R.string.onboarding_intro_habits_title, R.string.onboarding_intro_habits_body)
        FeatureRow(PawtivaIcons.Paw, R.string.onboarding_intro_grow_title, R.string.onboarding_intro_grow_body)
        FeatureRow(PawtivaIcons.Shop, R.string.onboarding_intro_style_title, R.string.onboarding_intro_style_body)
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, @StringRes title: Int, @StringRes body: Int) {
    val colors = PawtivaTheme.colors
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.padding(start = Spacing.md)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(stringResource(body), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

@Composable
internal fun PetStep(selected: PetType?, onSelect: (PetType) -> Unit) {
    StepTitle(
        title = stringResource(R.string.onboarding_pet_title),
        subtitle = stringResource(R.string.onboarding_pet_subtitle),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        PetChoiceCard(PetType.CAT, R.string.onboarding_pet_cat_description, selected == PetType.CAT, onSelect, Modifier.weight(1f))
        PetChoiceCard(PetType.DOG, R.string.onboarding_pet_dog_description, selected == PetType.DOG, onSelect, Modifier.weight(1f))
    }
}

@Composable
private fun PetChoiceCard(
    type: PetType,
    @StringRes description: Int,
    selected: Boolean,
    onSelect: (PetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PawtivaTheme.colors
    val scale by animateFloatAsState(
        targetValue = if (selected && !PawtivaTheme.reduceMotion) 1.03f else 1f,
        label = "petCardScale",
    )
    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(MaterialTheme.shapes.large)
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(type) }),
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.border),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val mood = if (selected) PetMood.HAPPY else PetMood.NORMAL
            PetIllustration(
                type = type,
                mood = mood,
                contentDescription = petContentDescription(type, type.defaultName(), mood),
                modifier = Modifier.size(112.dp),
                animate = selected,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = type.displayName(),
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) colors.onPrimaryContainer else colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) colors.onPrimaryContainer else colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            if (selected) {
                Spacer(Modifier.height(Spacing.xs))
                Icon(
                    imageVector = PawtivaIcons.Check,
                    contentDescription = stringResource(R.string.onboarding_pet_selected),
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
internal fun NameStep(petType: PetType, name: String, onNameChange: (String) -> Unit, onDone: () -> Unit) {
    val colors = PawtivaTheme.colors
    val defaultName = petType.defaultName()
    PetIllustration(
        type = petType,
        mood = PetMood.HAPPY,
        contentDescription = petContentDescription(petType, name.ifBlank { defaultName }, PetMood.HAPPY),
        modifier = Modifier.size(160.dp),
    )
    Spacer(Modifier.height(Spacing.md))
    StepTitle(
        title = stringResource(R.string.onboarding_name_title),
        subtitle = stringResource(R.string.onboarding_name_hint, defaultName),
    )
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.onboarding_name_label)) },
        placeholder = { Text(defaultName) },
        singleLine = true,
        supportingText = {
            Text(
                text = stringResource(R.string.onboarding_name_counter, name.length, UserPreferences.PET_NAME_MAX_LENGTH),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border,
            focusedLabelColor = colors.primary,
            cursorColor = colors.primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun LanguageStep(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    StepTitle(title = stringResource(R.string.onboarding_language_title))
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OptionRow(
            title = stringResource(R.string.onboarding_language_system),
            subtitle = stringResource(R.string.onboarding_language_system_description),
            selected = selected == AppLanguage.SYSTEM,
            onClick = { onSelect(AppLanguage.SYSTEM) },
        )
        OptionRow(
            title = stringResource(R.string.onboarding_language_turkish),
            selected = selected == AppLanguage.TURKISH,
            onClick = { onSelect(AppLanguage.TURKISH) },
        )
        OptionRow(
            title = stringResource(R.string.onboarding_language_english),
            selected = selected == AppLanguage.ENGLISH,
            onClick = { onSelect(AppLanguage.ENGLISH) },
        )
    }
}

@Composable
internal fun ThemeStep(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    StepTitle(
        title = stringResource(R.string.onboarding_theme_title),
        subtitle = stringResource(R.string.onboarding_theme_subtitle),
    )
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OptionRow(stringResource(R.string.onboarding_theme_system), selected = selected == ThemeMode.SYSTEM, onClick = { onSelect(ThemeMode.SYSTEM) })
        OptionRow(stringResource(R.string.onboarding_theme_light), selected = selected == ThemeMode.LIGHT, onClick = { onSelect(ThemeMode.LIGHT) })
        OptionRow(stringResource(R.string.onboarding_theme_dark), selected = selected == ThemeMode.DARK, onClick = { onSelect(ThemeMode.DARK) })
    }
}

@Composable
private fun OptionRow(title: String, selected: Boolean, onClick: () -> Unit, subtitle: String? = null) {
    val colors = PawtivaTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.textSecondary),
            )
            Column(Modifier.padding(start = Spacing.sm)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
internal fun NotificationsStep() {
    val colors = PawtivaTheme.colors
    Spacer(Modifier.height(Spacing.md))
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(colors.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(PawtivaIcons.Bell, contentDescription = null, tint = colors.onSecondaryContainer, modifier = Modifier.size(44.dp))
    }
    Spacer(Modifier.height(Spacing.lg))
    StepTitle(
        title = stringResource(R.string.onboarding_notifications_title),
        subtitle = stringResource(R.string.onboarding_notifications_body),
    )
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceMuted,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.onboarding_notifications_example),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Composable
internal fun DoneStep(petType: PetType, petName: String) {
    val name = petName.ifBlank { petType.defaultName() }
    Spacer(Modifier.height(Spacing.lg))
    PetIllustration(
        type = petType,
        mood = PetMood.CELEBRATING,
        contentDescription = petContentDescription(petType, name, PetMood.CELEBRATING),
        modifier = Modifier.size(200.dp),
    )
    Spacer(Modifier.height(Spacing.lg))
    StepTitle(
        title = stringResource(R.string.onboarding_done_title),
        subtitle = stringResource(R.string.onboarding_done_body, name),
    )
}

private val FirstHabitSuggestions = listOf(
    R.string.onboarding_first_habit_suggestion_water to "💧",
    R.string.onboarding_first_habit_suggestion_walk to "🚶",
    R.string.onboarding_first_habit_suggestion_read to "📚",
    R.string.onboarding_first_habit_suggestion_stretch to "🧘",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FirstHabitStep(
    name: String,
    icon: String,
    failed: Boolean,
    onNameChange: (String) -> Unit,
    onSuggestion: (name: String, icon: String) -> Unit,
    onDone: () -> Unit,
) {
    val colors = PawtivaTheme.colors
    Spacer(Modifier.height(Spacing.md))
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(colors.successContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, style = MaterialTheme.typography.displaySmall)
    }
    Spacer(Modifier.height(Spacing.lg))
    StepTitle(
        title = stringResource(R.string.onboarding_first_habit_title),
        subtitle = stringResource(R.string.onboarding_first_habit_subtitle),
    )
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.onboarding_first_habit_label)) },
        singleLine = true,
        isError = failed,
        supportingText = if (failed) {
            { Text(stringResource(R.string.onboarding_first_habit_failed)) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border,
            focusedLabelColor = colors.primary,
            cursorColor = colors.primary,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.sm))
    Text(
        text = stringResource(R.string.onboarding_first_habit_suggestions),
        style = MaterialTheme.typography.labelLarge,
        color = colors.textSecondary,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.xs))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        FirstHabitSuggestions.forEach { (labelRes, suggestionIcon) ->
            val label = stringResource(labelRes)
            SuggestionChip(
                onClick = { onSuggestion(label, suggestionIcon) },
                label = { Text("$suggestionIcon  $label") },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = colors.surface,
                    labelColor = colors.textPrimary,
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = colors.border),
            )
        }
    }
}
