package com.nisaefendioglu.pawtiva.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserPreferencesRepository()
    private val language = FakeAppLanguageController()
    private val habits = FakeHabitRepository()
    private val reminders = FakeReminderScheduler()

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        OnboardingViewModel(repository, language, habits, reminders, handle)

    private fun OnboardingViewModel.goTo(step: OnboardingStep) {
        while (uiState.value.step != step) {
            when (uiState.value.step) {
                OnboardingStep.PET -> if (uiState.value.petType == null) onSelectPet(PetType.CAT)
                OnboardingStep.NOTIFICATIONS -> {
                    onNotificationsDecided(granted = false)
                    continue
                }
                OnboardingStep.FIRST_HABIT -> {
                    onSkipFirstHabit()
                    continue
                }
                else -> Unit
            }
            onNext()
        }
    }

    @Test
    fun startsAtWelcome_andWalksForward() {
        val vm = viewModel()
        assertEquals(OnboardingStep.WELCOME, vm.uiState.value.step)
        vm.onNext()
        assertEquals(OnboardingStep.INTRO, vm.uiState.value.step)
        vm.onBack()
        assertEquals(OnboardingStep.WELCOME, vm.uiState.value.step)
    }

    @Test
    fun petStep_blocksContinueUntilAPetIsChosen() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.INTRO)
        vm.onNext()
        assertEquals(OnboardingStep.PET, vm.uiState.value.step)

        vm.onNext()
        assertEquals(OnboardingStep.PET, vm.uiState.value.step)
        assertFalse(vm.uiState.value.canContinue)

        vm.onSelectPet(PetType.DOG)
        assertEquals(PetType.DOG, repository.state.value.petType)
        vm.onNext()
        assertEquals(OnboardingStep.NAME, vm.uiState.value.step)
    }

    @Test
    fun petName_isLimitedAndSavedOnContinue() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.NAME)
        vm.onPetNameChange("A very very long pet name indeed")
        assertEquals(UserPreferences.PET_NAME_MAX_LENGTH, vm.uiState.value.petName.length)

        vm.onPetNameChange("Pamuk")
        vm.onNext()
        assertEquals("Pamuk", repository.state.value.petName)
        assertEquals(OnboardingStep.LANGUAGE, vm.uiState.value.step)
    }

    @Test
    fun skippingName_clearsItAndMovesOn() {
        repository.state.value = UserPreferences(petName = "Old")
        val vm = viewModel()
        vm.goTo(OnboardingStep.NAME)
        vm.onSkipName()
        assertNull(repository.state.value.petName)
        assertEquals(OnboardingStep.LANGUAGE, vm.uiState.value.step)
    }

    @Test
    fun languageAndTheme_areAppliedImmediately() {
        val vm = viewModel()
        vm.onSelectLanguage(AppLanguage.TURKISH)
        vm.onSelectTheme(ThemeMode.DARK)
        assertEquals(AppLanguage.TURKISH, language.language)
        assertEquals(ThemeMode.DARK, repository.state.value.themeMode)
    }

    @Test
    fun notifications_declinedOrSkipped_staysOff() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.NOTIFICATIONS)
        vm.onNotificationsDecided(granted = false)
        assertFalse(repository.state.value.remindersEnabled)
        assertNull(reminders.scheduled)
        assertEquals(OnboardingStep.FIRST_HABIT, vm.uiState.value.step)
    }

    @Test
    fun finishing_marksOnboardingComplete() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.NOTIFICATIONS)
        vm.onNotificationsDecided(granted = true)
        assertTrue(repository.state.value.remindersEnabled)
        assertEquals(UserPreferences.DEFAULT_REMINDER_TIME, reminders.scheduled)
        vm.onSkipFirstHabit()
        assertEquals(OnboardingStep.DONE, vm.uiState.value.step)
        vm.onFinish()
        assertTrue(repository.state.value.onboardingCompleted)
    }

    @Test
    fun step_isRestoredAfterProcessDeath() {
        val handle = SavedStateHandle()
        viewModel(handle).goTo(OnboardingStep.THEME)
        val restored = viewModel(handle)
        assertEquals(OnboardingStep.THEME, restored.uiState.value.step)
    }

    @Test
    fun existingChoices_arePrefilled() {
        repository.state.value = UserPreferences(petType = PetType.DOG, petName = "Waffle", themeMode = ThemeMode.LIGHT)
        val vm = viewModel()
        assertEquals(PetType.DOG, vm.uiState.value.petType)
        assertEquals("Waffle", vm.uiState.value.petName)
        assertEquals(ThemeMode.LIGHT, vm.uiState.value.themeMode)
    }

    @Test
    fun firstHabit_blankNameCannotBeCreated() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.FIRST_HABIT)
        assertFalse(vm.uiState.value.canContinue)
        vm.onCreateFirstHabit()
        assertEquals(0, habits.createCalls)
        assertEquals(OnboardingStep.FIRST_HABIT, vm.uiState.value.step)
    }

    @Test
    fun firstHabit_suggestionCreatesHabitOnce_evenWhenRevisited() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.FIRST_HABIT)
        vm.onFirstHabitSuggestion("Drink water", "💧")
        vm.onNext()
        assertEquals(OnboardingStep.DONE, vm.uiState.value.step)
        assertEquals(1, habits.createCalls)
        assertEquals("💧", habits.habits.value.single().icon)

        vm.onBack()
        vm.onFirstHabitNameChange("Drink two glasses of water")
        vm.onCreateFirstHabit()
        assertEquals(1, habits.createCalls)
        assertEquals("Drink two glasses of water", habits.habits.value.single().name)
    }

    @Test
    fun firstHabit_failureKeepsUserOnStep() {
        val vm = viewModel()
        vm.goTo(OnboardingStep.FIRST_HABIT)
        vm.onFirstHabitNameChange("Walk")
        habits.failNext = true
        vm.onCreateFirstHabit()
        assertTrue(vm.uiState.value.firstHabitFailed)
        assertEquals(OnboardingStep.FIRST_HABIT, vm.uiState.value.step)
    }
}
