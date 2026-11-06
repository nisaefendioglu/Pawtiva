package com.nisaefendioglu.pawtiva.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nisaefendioglu.pawtiva.R
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.feature.habits.HabitEditorFeature
import com.nisaefendioglu.pawtiva.feature.habits.HabitsFeature
import com.nisaefendioglu.pawtiva.feature.home.HomeFeature
import com.nisaefendioglu.pawtiva.feature.settings.SettingsFeature
import com.nisaefendioglu.pawtiva.feature.shop.ShopFeature
import com.nisaefendioglu.pawtiva.feature.statistics.StatisticsFeature
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object HabitsRoute
@Serializable data object ShopRoute
@Serializable data object StatsRoute
@Serializable data object SettingsRoute

/** [habitId] 0 creates a new habit. The editor's ViewModel reads it from SavedStateHandle("habitId"). */
@Serializable data class HabitEditorRoute(val habitId: Long = 0L)

enum class TopLevelDestination(
    val route: Any,
    @param:StringRes val labelRes: Int,
) {
    HOME(HomeRoute, R.string.nav_home),
    HABITS(HabitsRoute, R.string.nav_habits),
    SHOP(ShopRoute, R.string.nav_shop),
    STATS(StatsRoute, R.string.nav_stats),
    ;

    fun matches(destination: NavDestination): Boolean = when (this) {
        HOME -> destination.hasRoute(HomeRoute::class)
        HABITS -> destination.hasRoute(HabitsRoute::class)
        SHOP -> destination.hasRoute(ShopRoute::class)
        STATS -> destination.hasRoute(StatsRoute::class)
    }

    val icon: ImageVector
        get() = when (this) {
            HOME -> PawtivaIcons.Home
            HABITS -> PawtivaIcons.Habits
            SHOP -> PawtivaIcons.Shop
            STATS -> PawtivaIcons.Stats
        }
}

@Composable
fun PawtivaApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination == null ||
        TopLevelDestination.entries.any { dest -> currentDestination.hierarchy.any { dest.matches(it) } }
    val fadeMillis = if (PawtivaTheme.reduceMotion) 0 else 220

    val openEditor: (Long) -> Unit = { id -> navController.navigate(HabitEditorRoute(habitId = id)) }
    val openSettings: () -> Unit = { navController.navigate(SettingsRoute) { launchSingleTop = true } }

    Scaffold(
        modifier = modifier,
        containerColor = PawtivaTheme.colors.background,
        bottomBar = {
            if (showBottomBar) {
                PawtivaBottomBar(
                    selected = TopLevelDestination.entries.firstOrNull { dest ->
                        currentDestination?.hierarchy?.any { dest.matches(it) } == true
                    },
                    onSelect = { navController.navigateToTopLevel(it) },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            // Screens with their own Scaffold must not add the system-bar insets a second time.
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
            enterTransition = { fadeIn(tween(fadeMillis)) },
            exitTransition = { fadeOut(tween(fadeMillis)) },
        ) {
            composable<HomeRoute> {
                HomeFeature(
                    onOpenSettings = openSettings,
                    onAddHabit = { openEditor(0L) },
                    onEditHabit = openEditor,
                )
            }
            composable<HabitsRoute> {
                HabitsFeature(onAddHabit = { openEditor(0L) }, onEditHabit = openEditor)
            }
            composable<ShopRoute> { ShopFeature() }
            composable<StatsRoute> { StatisticsFeature() }
            composable<HabitEditorRoute> { HabitEditorFeature(onDone = { navController.popBackStack() }) }
            composable<SettingsRoute> { SettingsFeature(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
internal fun PawtivaBottomBar(
    selected: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
) {
    val colors = PawtivaTheme.colors
    NavigationBar(containerColor = colors.surface) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    selectedTextColor = colors.textPrimary,
                    indicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
            )
        }
    }
}

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
