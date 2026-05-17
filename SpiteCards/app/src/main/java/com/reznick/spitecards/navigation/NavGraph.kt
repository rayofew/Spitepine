package com.reznick.spitecards.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reznick.spitecards.integration.spitescore.GameSetupPayload
import com.reznick.spitecards.ui.about.AboutScreen
import com.reznick.spitecards.ui.game.GameScreen
import com.reznick.spitecards.ui.history.GameHistoryScreen
import com.reznick.spitecards.ui.home.HomeScreen
import com.reznick.spitecards.ui.host.LobbyScreen
import com.reznick.spitecards.ui.host.NewSessionWizardScreen
import com.reznick.spitecards.ui.player.DiscoverJoinScreen
import com.reznick.spitecards.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NewSessionWizard : Screen("wizard")
    object Lobby : Screen("lobby")
    object DiscoverJoin : Screen("discover")
    object Game : Screen("game/{sessionId}") {
        fun route(sessionId: String) = "game/$sessionId"
    }
    object GameHistory : Screen("history")
    object Settings : Screen("settings")
    object About : Screen("about")
}

@Composable
fun SpiteCardsNavHost(incomingSetup: GameSetupPayload? = null) {
    val navController = rememberNavController()
    val startDestination = Screen.Home.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Home.route) {
            HomeScreen(
                onHostGame = { navController.navigate(Screen.NewSessionWizard.route) },
                onJoinGame = { navController.navigate(Screen.DiscoverJoin.route) },
                onHistory = { navController.navigate(Screen.GameHistory.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onAbout = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.NewSessionWizard.route) {
            NewSessionWizardScreen(
                preloadedSetup = incomingSetup,
                onSessionCreated = { sessionId -> navController.navigate(Screen.Lobby.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Lobby.route) {
            LobbyScreen(
                onGameStart = { sessionId -> navController.navigate(Screen.Game.route(sessionId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DiscoverJoin.route) {
            DiscoverJoinScreen(
                onJoined = { sessionId -> navController.navigate(Screen.Game.route(sessionId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStack ->
            val sessionId = backStack.arguments?.getString("sessionId") ?: return@composable
            GameScreen(
                sessionId = sessionId,
                onGameEnd = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
            )
        }
        composable(Screen.GameHistory.route) {
            GameHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() }, onAbout = { navController.navigate(Screen.About.route) })
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
