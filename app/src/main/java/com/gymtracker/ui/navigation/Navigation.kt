package com.gymtracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.gymtracker.GymTrackerApp
import com.gymtracker.ui.screens.exercises.ExerciseDetailScreen
import com.gymtracker.ui.screens.exercises.ExerciseLibraryScreen
import com.gymtracker.ui.screens.home.HomeScreen
import com.gymtracker.ui.screens.progress.ProgressScreen
import com.gymtracker.ui.screens.routines.RoutineDetailScreen
import com.gymtracker.ui.screens.routines.RoutinesScreen
import com.gymtracker.ui.screens.settings.SettingsScreen
import com.gymtracker.ui.screens.workout.ActiveWorkoutScreen
import com.gymtracker.ui.screens.workout.WorkoutSetupScreen
import com.gymtracker.ui.screens.bodymap.BodyMapScreen
import com.gymtracker.ui.screens.gymmap.GymMapScreen
import com.gymtracker.ui.screens.onboarding.OnboardingScreen

// ═══════════════════════════════════════════════════════════════
// ROUTE DEFINITIONS
// ═══════════════════════════════════════════════════════════════
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Exercises : Screen("exercises")
    object ExerciseDetail : Screen("exercise/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise/$exerciseId"
    }
    object WorkoutSetup : Screen("workout_setup")
    object ActiveWorkout : Screen("active_workout/{sessionId}") {
        fun createRoute(sessionId: Long) = "active_workout/$sessionId"
    }
    object Progress : Screen("progress")
    object BodyMap : Screen("body_map")
    object Routines : Screen("routines")
    object RoutineDetail : Screen("routine/{routineId}") {
        fun createRoute(routineId: Long) = "routine/$routineId"
    }
    object GymMap : Screen("gym_map")
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}

// ═══════════════════════════════════════════════════════════════
// BOTTOM NAV ITEMS — emoji icons matching design spec
// ═══════════════════════════════════════════════════════════════
data class BottomNavItem(
    val label: String,
    val route: String,
    val emoji: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home",      Screen.Home.route,      "🏠"),
    BottomNavItem("Exercises", Screen.Exercises.route,  "🏋"),
    BottomNavItem("Routines",  Screen.Routines.route,   "📅"),
    BottomNavItem("Progress",  Screen.Progress.route,   "📈"),
    BottomNavItem("GymMap",    Screen.GymMap.route,     "📍"),
    BottomNavItem("Settings",  Screen.Settings.route,   "⚙️"),
)

// ═══════════════════════════════════════════════════════════════
// CUSTOM BOTTOM NAV — matches BottomNav.jsx design spec
// surface bg · 1dp outline top border · pill indicator · emoji icons
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DesignBottomNav(
    currentRoute: String?,
    items: List<BottomNavItem>,
    navController: NavController
) {
    val primary        = MaterialTheme.colorScheme.primary
    val onSurfaceVar   = MaterialTheme.colorScheme.onSurfaceVariant
    val surface        = MaterialTheme.colorScheme.surface
    val outline        = MaterialTheme.colorScheme.outline

    Column {
        HorizontalDivider(color = outline, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface)
                .padding(top = 6.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(min = 44.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 2.dp)
                ) {
                    // Pill indicator
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) primary.copy(alpha = 0.12f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.emoji, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.label,
                        fontSize    = 9.sp,
                        fontWeight  = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color       = if (selected) primary else onSurfaceVar,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// MAIN SCAFFOLD WITH NAV
// ═══════════════════════════════════════════════════════════════
@Composable
fun MainNavigation(startOnboarding: Boolean = false) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route } ||
        currentRoute == Screen.BodyMap.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DesignBottomNav(currentRoute, bottomNavItems, navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (startOnboarding) Screen.Onboarding.route else Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(200)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onStartWorkout = { navController.navigate(Screen.WorkoutSetup.route) },
                    onViewProgress = {
                        navController.navigate(Screen.Progress.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onViewBodyMap = { navController.navigate(Screen.BodyMap.route) },
                    onNavigateToExercises = { navController.navigate(Screen.Exercises.route) },
                    onResumeWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    }
                )
            }

            composable(Screen.Exercises.route) {
                ExerciseLibraryScreen(
                    onExerciseClick = { id ->
                        navController.navigate(Screen.ExerciseDetail.createRoute(id))
                    }
                )
            }

            composable(
                Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: return@composable
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.WorkoutSetup.route) {
                WorkoutSetupScreen(
                    onStartSession = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Screen.ActiveWorkout.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                ActiveWorkoutScreen(
                    sessionId = sessionId,
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onRunInBackground = {
                        navController.popBackStack(Screen.Home.route, false)
                    },
                    app = GymTrackerApp.instance
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(
                    onExerciseClick = { id ->
                        navController.navigate(Screen.ExerciseDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.BodyMap.route) {
                BodyMapScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.GymMap.route) {
                GymMapScreen()
            }

            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onRoutineClick = { id ->
                        navController.navigate(Screen.RoutineDetail.createRoute(id))
                    },
                    onStartWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    }
                )
            }

            composable(
                Screen.RoutineDetail.route,
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: return@composable
                RoutineDetailScreen(
                    routineId = routineId,
                    onBack = { navController.popBackStack() },
                    onStartWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout.createRoute(sessionId))
                    },
                    app = GymTrackerApp.instance
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
