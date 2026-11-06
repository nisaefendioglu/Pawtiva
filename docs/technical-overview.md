# Pawtiva — technical overview

Version 1.0.0 (versionCode 1) · applicationId `com.nisaefendioglu.pawtiva.app` · 2026-11-05

## Build setup

| Item | Value |
| --- | --- |
| Android Gradle Plugin | 8.12.0 (highest supported by Android Studio Narwhal Feature Drop 2025.1.2) |
| Gradle | 8.13 (wrapper) |
| Kotlin | 2.2.10, KSP 2.2.10-2.0.2 |
| JDK | 17+ to run Gradle (Android Studio's bundled JBR 21 works); bytecode target 17 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 26 |
| UI | Jetpack Compose (BOM 2025.08.00), Material 3, Navigation Compose (type-safe routes) |
| DI | Hilt 2.57.1 |
| Storage | Room 2.7.2 (structured data), Preferences DataStore 1.1.7 (simple settings) |
| Versions | Gradle version catalog `gradle/libs.versions.toml` |

Upgrade path: after updating Android Studio, move to AGP 9.x (built-in Kotlin), Gradle 9.x and current AndroidX releases. The comment at the top of the version catalog records why the current versions were chosen.

## Architecture

MVVM with a one-way dependency direction: `feature → core:data → core:database / core:datastore`, with business rules in `core:domain`.

| Module | Responsibility |
| --- | --- |
| `app` | `MainActivity` (single activity, AppCompat for per-app language), Hilt application, navigation shell, reminder alarm and receivers |
| `core:model` | Plain Kotlin models: `Habit`, `HabitCompletion`, `Progress`, `ShopItem`, `AccessoryId`, `AchievementId`, `UserPreferences`, … |
| `core:domain` | Pure Kotlin rules: `RewardPolicy`, `LevelCalculator`, `EditableDays`, `HabitValidator`, `PetMoodCalculator`, `ShopCatalog`, achievements and statistics calculations, `PawtivaClock` |
| `core:database` | Room database, entities, DAOs, exported schemas |
| `core:datastore` | Preferences DataStore data source |
| `core:data` | Repository interfaces and implementations (`HabitRepository`, `ProgressRepository`, `ShopRepository`, `AchievementRepository`, `DataManagementRepository`, `UserPreferencesRepository`), language controller |
| `core:designsystem` | Theme tokens (light/dark/dynamic), typography, spacing, icons, components (`PawtivaButton`, `PawtivaCard`, `XPProgressBar`, `DailyProgressCard`, `StatCard`, `SectionHeader`, `EmptyState`, `LoadingState`, `ErrorState`, …), code-drawn pet (`PetDisplay`) and accessories |
| `feature:onboarding` | First-run flow |
| `feature:home` | Daily dashboard: greeting, pet, level/XP, daily progress, today's habits |
| `feature:habits` | Habit list and editor |
| `feature:shop` | Cosmetic shop, inventory, equip/unequip |
| `feature:statistics` | Statistics, charts, calendar, history, achievements |
| `feature:settings` | Settings and profile, notifications, data export/reset, privacy and about |

UI conventions: one immutable `UiState` per screen exposed as `StateFlow`; collected with `collectAsStateWithLifecycle`; one-off events (celebrations, snackbars) separate from state; composables stay in the presentation layer; all user-facing text in string resources.

## Data model

Room tables:

| Table | Key columns | Rules |
| --- | --- | --- |
| `habits` | id, name, description, icon (emoji), color, createdAt, archivedAt | Delete = archive; history kept |
| `habit_completions` | id, habitId, localDate (`yyyy-MM-dd`), completedAt (UTC ms), zoneId, xpAwarded, currencyAwarded | `UNIQUE(habitId, localDate)` blocks duplicate rewards |
| `currency_transactions` | id, amount (±), type (REWARD, REWARD_REVERSAL, PURCHASE), refId, createdAt | Balance = `SUM(amount)` |
| `inventory` | itemId (PK), purchasedAt, equipped | One owned copy per item; one equipped item per slot |
| `achievement_progress` | achievementId (PK), unlockedAt | Unlocked once |

XP is not stored separately: total XP = sum of `xpAwarded`. The shop catalog and achievement definitions live in code, so changing them needs no migration. Schemas are exported to `core/database/schemas/`; destructive migration is not used.

DataStore keys: `pet_type`, `pet_name`, `theme_mode`, `dynamic_color`, `onboarding_completed`, `reminders_enabled`, `reminder_minute_of_day`, `reduce_pet_animations`, `date_format`. The app language is stored by AppCompat's per-app locale API, not duplicated.

## Business rules

- **XP:** 10 per completed habit. **Level** = ⌊totalXp / 100⌋ + 1; progress in level = totalXp mod 100. Pet type never affects progress.
- **PawCoins:** 5 per completion (`RewardPolicy`), separate from XP. No real-money purchase, ads or subscriptions.
- **Completion window:** today and the 6 days before (7 days). Future dates are rejected.
- **Duplicates:** blocked by the unique index and by checks inside one Room transaction; rapid taps cannot double-reward.
- **Undo:** deletes the completion (XP drops automatically) and writes a reversal transaction. If coins were already spent, only the remaining balance is reversed, so the balance never goes negative. Unlocked achievements are not revoked.
- **Shop:** purchase checks item exists, level requirement, not already owned, sufficient balance, in one transaction. Items that do not fit the current pet stay owned and equipped but are not drawn.
- **Pet mood:** derived from today only (never punitive): excited at ≥ 50 % done, happy at ≥ 1, sleepy 22:00–06:00 with none done, otherwise calm; celebrating is a short reaction on level-up or finishing the day.
- **Achievements:** First Step (1 completion), Beginner (10), Consistency (completions on 7 consecutive days), Determined (50), Growing Together (level 5), First Purchase, Perfect Day (all active habits done on a day).
- **Time zones:** each completion stores the local date at the moment it was made; changing time zone cannot create a second completion for the same habit and date.

## Offline and privacy

No network code, accounts, analytics or ad SDKs. All data stays in app-private storage. Android Auto Backup is enabled with explicit rules (database, DataStore, shared preferences). Users can export all data as JSON and reset everything. See `docs/privacy-policy-*.md` and `docs/play-data-safety.md`.

## Localization and accessibility

- English (default) and Turkish resources in every module, with plurals and locale-aware dates and numbers; in-app language switch via `AppCompatDelegate.setApplicationLocales` with a generated locale config for Android 13+.
- A unit test checks that every English string key has a Turkish counterpart.
- Content descriptions for the pet and icons, headings marked for screen readers, 48 dp touch targets, sp text sizes, states shown with icons and text (not only color), WCAG AA contrast enforced by a unit test, reduced motion respected (system animator scale and an in-app option).

## Testing

| Layer | Tooling | Runs on |
| --- | --- | --- |
| Domain rules | JUnit | JVM |
| Repositories, Room, DataStore | JUnit + Robolectric (real Room/DataStore) | JVM |
| ViewModels | JUnit + kotlinx-coroutines-test with fakes | JVM |
| Compose UI flows | Compose UI Test + Robolectric | JVM |
| Localization, color contrast | JUnit | JVM |

Run everything:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew testDebugUnitTest lintDebug
```

Reports: `*/build/reports/tests/testDebugUnitTest/index.html`, `*/build/reports/lint-results-debug.html`.

## Known limitations (1.0.0)

- No instrumented (emulator/device) tests have been run; all automated tests run on the JVM with Robolectric. Room migration tests become meaningful from schema version 2.
- Per-habit reminders are not in v1; there is one optional daily reminder (inexact alarm, may be delayed a few minutes by the system).
- Habits are daily only.
- The pet is a flat 2D vector illustration drawn in code (not a 3D model); the `PetDisplay` entry point allows swapping in animated or 3D art later.
- System sans-serif font (no bundled font yet).
- Store screenshots must be captured from the real app on a device or emulator.
- Release signing requires a local `keystore.properties` (see `docs/play-release-checklist.md`).
