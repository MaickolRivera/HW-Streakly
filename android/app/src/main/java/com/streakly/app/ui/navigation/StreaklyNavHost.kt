package com.streakly.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.streakly.app.ui.theme.StreaklyColors
import com.streakly.app.ui.today.TodayScreen

object Routes {
    const val TODAY = "today"
    const val STREAKS = "streaks"
    const val GOALS = "goals"
    const val PROFILE = "profile"
    const val NEW_HABIT = "habits/new"
    const val NEW_GOAL = "goals/new"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.TODAY, "Hoy", Icons.Outlined.Home),
    Tab(Routes.STREAKS, "Rachas", Icons.Outlined.BarChart),
    Tab(Routes.GOALS, "Goals", Icons.Outlined.Flag),
    Tab(Routes.PROFILE, "Perfil", Icons.Outlined.Person),
)

@Composable
fun StreaklyNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = StreaklyColors.Background,
        bottomBar = { if (showBottomBar) BottomBar(navController, currentRoute) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .statusBarsPadding(),
        ) {
            composable(Routes.TODAY) { TodayScreen(onNewHabit = { navController.navigate(Routes.NEW_HABIT) }) }
            composable(Routes.STREAKS) { Placeholder("Rachas") }
            composable(Routes.GOALS) { Placeholder("Goals") }
            composable(Routes.PROFILE) { Placeholder("Perfil") }
        }
    }
}

@Composable
private fun Placeholder(title: String) {
    Box(Modifier.fillMaxSize().background(StreaklyColors.Background)) {
        Column(Modifier.padding(22.dp)) { Text(title.uppercase(), style = MaterialTheme.typography.headlineLarge) }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = StreaklyColors.Surface, tonalElevation = 0.dp) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StreaklyColors.Green,
                    selectedTextColor = StreaklyColors.TextPrimary,
                    indicatorColor = StreaklyColors.GreenTint,
                    unselectedIconColor = StreaklyColors.TextSecondary,
                    unselectedTextColor = StreaklyColors.TextSecondary,
                ),
            )
        }
    }
}
