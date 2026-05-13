package com.gymtracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
// BOTTOM NAV ITEMS — vector icons
// ═══════════════════════════════════════════════════════════════
data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home",      Screen.Home.route,      Icons.Filled.Home),
    BottomNavItem("Exercises", Screen.Exercises.route,  Icons.Filled.FitnessCenter),
    BottomNavItem("Routines",  Screen.Routines.route,   Icons.Filled.CalendarToday),
    BottomNavItem("Progress",  Screen.Progress.route,   Icons.AutoMirrored.Filled.ShowChart),
    BottomNavItem("GymMap",    Screen.GymMap.route,     Icons.Filled.Map),
    BottomNavItem("Settings",  Screen.Settings.route,   Icons.Filled.Settings),
)

// ═══════════════════════════════════════════════════════════════
// CUSTOM BOTTOM NAV — vector icons with animated color transition
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DesignBottomNav(
    currentRoute: String?,
    items: List<BottomNavItem>,
    navController: NavController
) {
    val primary      = MaterialTheme.colorScheme.primary
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val surface      = MaterialTheme.colorScheme.surface
    val outline      = MaterialTheme.colorScheme.outline

    Column {
        HorizontalDivider(color = outline.copy(alpha = 0.5f), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface.copy(alpha = 0.92f))
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val iconColor by animateColorAsState(
                    targetValue = if (selected) primary else onSurfaceVar,
                    animationSpec = tween(200),
                    label = "navIconColor"
                )
                val labelColor by animateColorAsState(
                    targetValue = if (selected) primary else onSurfaceVar,
                    animationSpec = tween(200),
                    label = "navLabelColor"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
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
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) primary.copy(alpha = 0.1f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(if (selected) 24.dp else 22.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.label,
                        fontSize      = 10.sp,
                        fontWeight    = if (selected) FontWeight.Black else FontWeight.Medium,
                        color         = labelColor,
                        letterSpacing = 0.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// GLASS TOP APP BAR
// ═══════════════════════════════════════════════════════════════
@Composable
private fun GymBuddyTopBar(navController: NavController) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("🏋", fontSize = 18.sp) }
                    Text(
                        "GymBuddy",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate(Screen.Settings.route) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)
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
        topBar = {
            if (showBottomBar) {
                GymBuddyTopBar(navController = navController)
            }
        },
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
