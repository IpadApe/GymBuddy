package com.gymtracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}

// ═══════════════════════════════════════════════════════════════
// BOTTOM NAV ITEMS
// ═══════════════════════════════════════════════════════════════
data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Exercises", Screen.Exercises.route, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    BottomNavItem("Routines", Screen.Routines.route, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomNavItem("Progress", Screen.Progress.route, Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
    BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings, Icons.Outlined.Settings)
)

// ═══════════════════════════════════════════════════════════════
// MAIN SCAFFOLD WITH NAV
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(startOnboarding: Boolean = false) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route } || currentRoute == Screen.BodyMap.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
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
