package com.nisaefendioglu.pawtiva.feature.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.component.EmptyState
import com.nisaefendioglu.pawtiva.core.designsystem.component.LoadingState
import com.nisaefendioglu.pawtiva.core.designsystem.component.PawtivaCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.ProgressRing
import com.nisaefendioglu.pawtiva.core.designsystem.component.SectionHeader
import com.nisaefendioglu.pawtiva.core.designsystem.component.StatCard
import com.nisaefendioglu.pawtiva.core.designsystem.component.XPProgressBar
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import com.nisaefendioglu.pawtiva.core.designsystem.theme.currentLocale
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatDate
import com.nisaefendioglu.pawtiva.core.designsystem.theme.formatNumber
import com.nisaefendioglu.pawtiva.core.domain.DayCount
import com.nisaefendioglu.pawtiva.core.domain.StatisticsSummary
import com.nisaefendioglu.pawtiva.core.model.AchievementStatus
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** Statistics tab entry point. */
@Composable
fun StatisticsFeature(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsContent(state = state)
}

/** Stateless statistics UI, used by tests. */
@Composable
fun StatisticsContent(state: StatisticsUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PawtivaTheme.colors.background),
    ) {
        val summary = state.summary
        if (state.loading || summary == null) {
            LoadingState(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        val today = state.today ?: LocalDate.now()
        LazyColumn(
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "header") { Header() }
            item(key = "stats") { StatGrid(state) }
            if (state.isEmpty) {
                item(key = "empty") {
                    PawtivaCard(Modifier.fillMaxWidth()) {
                        EmptyState(
                            title = stringResource(R.string.stats_empty_title),
                            message = stringResource(R.string.stats_empty_message),
                            icon = PawtivaIcons.Stats,
                        )
                    }
                }
            } else {
                item(key = "today") { TodayCard(summary) }
                item(key = "week") { WeekCard(summary, today) }
                item(key = "month") { MonthCard(summary, today) }
                item(key = "history") { HistoryCard(summary.recent, state.dateFormat) }
            }
            item(key = "achievementsHeader") {
                SectionHeader(
                    title = stringResource(R.string.stats_achievements_title, state.unlockedCount, state.achievements.size),
                )
            }
            items(state.achievements, key = { it.id.storageId }) { achievement ->
                AchievementRow(achievement, state.dateFormat)
            }
            item(key = "bottom") { Spacer(Modifier.height(Spacing.lg)) }
        }
    }
}

@Composable
private fun Header() {
    val colors = PawtivaTheme.colors
    Column {
        Text(
            text = stringResource(R.string.stats_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.stats_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun StatGrid(state: StatisticsUiState) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val total = state.summary?.totalCompletions ?: 0
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            StatCard(
                label = stringResource(R.string.stats_total_completions),
                value = formatNumber(total, locale),
                icon = PawtivaIcons.Check,
                modifier = Modifier.weight(1f),
                accent = colors.successContainer,
                onAccent = colors.onSuccessContainer,
            )
            StatCard(
                label = stringResource(R.string.stats_total_xp),
                value = formatNumber(state.progress.totalXp, locale),
                icon = PawtivaIcons.Stats,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            StatCard(
                label = stringResource(R.string.stats_level),
                value = formatNumber(state.progress.level, locale),
                icon = PawtivaIcons.Paw,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.stats_total_coins),
                value = formatNumber(state.progress.totalCoinsEarned, locale),
                icon = PawtivaIcons.Coin,
                modifier = Modifier.weight(1f),
                accent = colors.secondaryContainer,
                onAccent = colors.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun TodayCard(summary: StatisticsSummary) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val percent = remember(summary.todayRate, locale) { NumberFormat.getPercentInstance(locale).format(summary.todayRate) }
    PawtivaCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ProgressRing(fraction = summary.todayRate, centerText = percent)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.stats_today_title), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(
                    text = if (summary.activeHabits == 0) {
                        stringResource(R.string.stats_today_no_habits)
                    } else {
                        pluralStringResource(
                            R.plurals.stats_today_summary,
                            summary.activeHabits,
                            summary.completedToday.coerceAtMost(summary.activeHabits),
                            summary.activeHabits,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = pluralStringResource(R.plurals.stats_current_streak, summary.currentStreak, summary.currentStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                )
                Text(
                    text = pluralStringResource(R.plurals.stats_longest_streak, summary.longestStreak, summary.longestStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun WeekCard(summary: StatisticsSummary, today: LocalDate) {
    val colors = PawtivaTheme.colors
    PawtivaCard(Modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.stats_week_title))
        Spacer(Modifier.height(Spacing.sm))
        WeeklyBarChart(days = summary.last7Days, today = today)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = pluralStringResource(R.plurals.stats_last_30, summary.last30DaysTotal, summary.last30DaysTotal),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

/** Seven labelled bars; every value is printed above its bar, so color is never the only cue. */
@Composable
private fun WeeklyBarChart(days: List<DayCount>, today: LocalDate) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val max = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val todayMarker = stringResource(R.string.stats_today_marker)
    val description = days.map { day ->
        val label = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale) + if (day.date == today) " ($todayMarker)" else ""
        pluralStringResource(R.plurals.stats_day_description, day.count, label, day.count)
    }.joinToString(", ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val isToday = day.date == today
            val fraction by animateFloatAsState(
                targetValue = day.count.toFloat() / max,
                animationSpec = if (PawtivaTheme.reduceMotion) snap() else tween(600),
                label = "weekBar",
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = day.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(Spacing.xxs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height((90 * fraction).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                        .background(if (day.count > 0) colors.primary else colors.progressTrack),
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun MonthCard(summary: StatisticsSummary, today: LocalDate) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val monthName = remember(summary.month, locale) {
        summary.month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
    PawtivaCard(Modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.stats_month_title))
        Text(text = monthName, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Spacer(Modifier.height(Spacing.sm))
        MonthCalendar(summary, today, locale)
    }
}

@Composable
private fun MonthCalendar(summary: StatisticsSummary, today: LocalDate, locale: Locale) {
    val colors = PawtivaTheme.colors
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val weekdays = (0L until 7L).map { firstDayOfWeek.plus(it) }
    val firstOfMonth = summary.month.atDay(1)
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
    val cells: List<Int?> = List(leadingBlanks) { null } + (1..summary.month.lengthOfMonth()).toList()
    val weeks = cells.chunked(7).map { week -> week + List(7 - week.size) { null } }
    val todayMarker = stringResource(R.string.stats_today_marker)

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics { }) {
            weekdays.forEach { day: DayOfWeek ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { dayOfMonth ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dayOfMonth != null) {
                            val date = summary.month.atDay(dayOfMonth)
                            val count = summary.monthCounts[dayOfMonth] ?: 0
                            val label = date.format(DateTimeFormatter.ofPattern("d MMMM", locale)) +
                                if (date == today) " ($todayMarker)" else ""
                            CalendarDay(
                                day = dayOfMonth,
                                count = count,
                                isToday = date == today,
                                description = pluralStringResource(R.plurals.stats_day_description, count, label, count),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(day: Int, count: Int, isToday: Boolean, description: String) {
    val colors = PawtivaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (count > 0) colors.primaryContainer else colors.surface)
            .then(if (isToday) Modifier.border(BorderStroke(1.5.dp, colors.primary), CircleShape) else Modifier)
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (count > 0) colors.onPrimaryContainer else colors.textPrimary,
        )
        if (count > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
                if (count > 1) {
                    Spacer(Modifier.width(1.dp))
                    Text(text = count.toString(), style = MaterialTheme.typography.labelSmall, color = colors.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(records: List<CompletionRecord>, dateFormat: DateFormatPreference) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    PawtivaCard(Modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.stats_history_title))
        Spacer(Modifier.height(Spacing.xs))
        if (records.isEmpty()) {
            Text(stringResource(R.string.stats_history_empty), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        records.forEach { record ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs)
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceMuted)
                        .clearAndSetSemantics { },
                    contentAlignment = Alignment.Center,
                ) { Text(record.habitIcon, style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = record.habitName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = formatDate(record.date, dateFormat, locale),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: AchievementStatus, dateFormat: DateFormatPreference) {
    val colors = PawtivaTheme.colors
    val locale = currentLocale
    val unlockedAt = achievement.unlockedAtEpochMillis
    PawtivaCard(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (achievement.unlocked) colors.successContainer else colors.surfaceMuted),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (achievement.unlocked) PawtivaIcons.Check else PawtivaIcons.Paw,
                    contentDescription = null,
                    tint = if (achievement.unlocked) colors.onSuccessContainer else colors.textSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(achievement.id.titleRes()),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(achievement.id.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.xxs))
                if (unlockedAt != null) {
                    val date = Instant.ofEpochMilli(unlockedAt).atZone(ZoneId.systemDefault()).toLocalDate()
                    Text(
                        text = stringResource(R.string.stats_achievement_unlocked_on, formatDate(date, dateFormat, locale)),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.success,
                    )
                } else {
                    val progress = achievement.progress.coerceIn(0f, 1f)
                    val percent = remember(progress, locale) { NumberFormat.getPercentInstance(locale).format(progress) }
                    val progressText = stringResource(R.string.stats_achievement_progress, percent)
                    XPProgressBar(progress = progress, contentDescription = progressText, height = 8.dp)
                    Text(text = progressText, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }
            }
        }
    }
}
