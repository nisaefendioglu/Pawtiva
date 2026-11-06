package com.nisaefendioglu.pawtiva.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButton
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaButtonStyle
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode

/** Entry point used by the app: wires the ViewModel and the notification permission request. */
@Composable
fun OnboardingRoute(viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onNotificationsDecided(granted) }

    val requestNotifications: () -> Unit = {
        val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsRuntimePermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onNotificationsDecided(true)
        }
    }

    BackHandler(enabled = state.step.previous() != null && state.step != OnboardingStep.DONE) {
        viewModel.onBack()
    }

    OnboardingContent(
        state = state,
        actions = OnboardingActions(
            onNext = viewModel::onNext,
            onBack = viewModel::onBack,
            onSelectPet = viewModel::onSelectPet,
            onPetNameChange = viewModel::onPetNameChange,
            onSkipName = viewModel::onSkipName,
            onSelectLanguage = viewModel::onSelectLanguage,
            onSelectTheme = viewModel::onSelectTheme,
            onAllowNotifications = requestNotifications,
            onSkipNotifications = { viewModel.onNotificationsDecided(false) },
            onFirstHabitNameChange = viewModel::onFirstHabitNameChange,
            onFirstHabitSuggestion = viewModel::onFirstHabitSuggestion,
            onCreateFirstHabit = viewModel::onCreateFirstHabit,
            onSkipFirstHabit = viewModel::onSkipFirstHabit,
            onFinish = viewModel::onFinish,
        ),
    )
}

data class OnboardingActions(
    val onNext: () -> Unit = {},
    val onBack: () -> Unit = {},
    val onSelectPet: (PetType) -> Unit = {},
    val onPetNameChange: (String) -> Unit = {},
    val onSkipName: () -> Unit = {},
    val onSelectLanguage: (AppLanguage) -> Unit = {},
    val onSelectTheme: (ThemeMode) -> Unit = {},
    val onAllowNotifications: () -> Unit = {},
    val onSkipNotifications: () -> Unit = {},
    val onFirstHabitNameChange: (String) -> Unit = {},
    val onFirstHabitSuggestion: (name: String, icon: String) -> Unit = { _, _ -> },
    val onCreateFirstHabit: () -> Unit = {},
    val onSkipFirstHabit: () -> Unit = {},
    val onFinish: () -> Unit = {},
)

/** Stateless onboarding UI, used directly by tests and previews. */
@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = PawtivaTheme.reduceMotion
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawtivaTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        TopBar(state = state, onBack = actions.onBack)

        AnimatedContent(
            targetState = state.step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(snap()) togetherWith fadeOut(snap())
                } else {
                    val forward = targetState.ordinal > initialState.ordinal
                    val direction = if (forward) 1 else -1
                    (slideInHorizontally(tween(300)) { direction * it / 5 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(250)) { -direction * it / 5 } + fadeOut(tween(200)))
                }
            },
            label = "onboardingStep",
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.INTRO -> IntroStep()
                    OnboardingStep.PET -> PetStep(selected = state.petType, onSelect = actions.onSelectPet)
                    OnboardingStep.NAME -> NameStep(
                        petType = state.petType ?: PetType.CAT,
                        name = state.petName,
                        onNameChange = actions.onPetNameChange,
                        onDone = actions.onNext,
                    )
                    OnboardingStep.LANGUAGE -> LanguageStep(selected = state.language, onSelect = actions.onSelectLanguage)
                    OnboardingStep.THEME -> ThemeStep(selected = state.themeMode, onSelect = actions.onSelectTheme)
                    OnboardingStep.NOTIFICATIONS -> NotificationsStep()
                    OnboardingStep.FIRST_HABIT -> FirstHabitStep(
                        name = state.firstHabitName,
                        icon = state.firstHabitIcon,
                        failed = state.firstHabitFailed,
                        onNameChange = actions.onFirstHabitNameChange,
                        onSuggestion = actions.onFirstHabitSuggestion,
                        onDone = actions.onCreateFirstHabit,
                    )
                    OnboardingStep.DONE -> DoneStep(petType = state.petType ?: PetType.CAT, petName = state.petName)
                }
            }
        }

        BottomActions(state = state, actions = actions)
    }
}

@Composable
private fun TopBar(state: OnboardingUiState, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(Spacing.touchTarget)) {
            val canGoBack = state.step != OnboardingStep.WELCOME && state.step != OnboardingStep.DONE
            if (canGoBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = PawtivaIcons.Back,
                        contentDescription = stringResource(R.string.onboarding_back),
                        tint = PawtivaTheme.colors.textPrimary,
                    )
                }
            }
        }
        StepDots(
            current = state.step.ordinal,
            count = state.stepCount,
            description = stringResource(R.string.onboarding_progress, state.stepNumber, state.stepCount),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(Spacing.touchTarget))
    }
}

@Composable
private fun StepDots(current: Int, count: Int, description: String, modifier: Modifier = Modifier) {
    val colors = PawtivaTheme.colors
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val width by animateDpAsState(
                targetValue = if (index == current) 22.dp else 8.dp,
                animationSpec = if (PawtivaTheme.reduceMotion) snap() else tween(250),
                label = "stepDot",
            )
            Box(
                Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(if (index <= current) colors.primary else colors.progressTrack),
            )
        }
    }
}

@Composable
private fun BottomActions(state: OnboardingUiState, actions: OnboardingActions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        val (label, onClick) = when (state.step) {
            OnboardingStep.WELCOME -> stringResource(R.string.onboarding_start) to actions.onNext
            OnboardingStep.NOTIFICATIONS -> stringResource(R.string.onboarding_notifications_allow) to actions.onAllowNotifications
            OnboardingStep.FIRST_HABIT -> stringResource(R.string.onboarding_first_habit_create) to actions.onCreateFirstHabit
            OnboardingStep.DONE -> stringResource(R.string.onboarding_done_button) to actions.onFinish
            else -> stringResource(R.string.onboarding_next) to actions.onNext
        }
        PawtivaButton(
            text = label,
            onClick = onClick,
            enabled = state.canContinue && !state.isFinishing,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.step.isOptional) {
            PawtivaButton(
                text = stringResource(
                    if (state.step == OnboardingStep.NOTIFICATIONS) R.string.onboarding_not_now else R.string.onboarding_skip,
                ),
                onClick = when (state.step) {
                    OnboardingStep.NOTIFICATIONS -> actions.onSkipNotifications
                    OnboardingStep.FIRST_HABIT -> actions.onSkipFirstHabit
                    else -> actions.onSkipName
                },
                style = PawtivaButtonStyle.Text,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
