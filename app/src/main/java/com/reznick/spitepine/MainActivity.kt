package com.reznick.spitepine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reznick.spitepine.ui.auth.AuthViewModel
import com.reznick.spitepine.ui.auth.SignInScreen
import com.reznick.spitepine.ui.home.HomeScreen
import com.reznick.spitepine.ui.settings.SettingsScreen
import com.reznick.spitepine.ui.theme.SpitePineTheme
import com.reznick.spitepine.ui.tree.AddTreeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpitePineTheme {
                AppRoot(activity = this@MainActivity)
            }
        }
    }
}

@Composable
private fun AppRoot(activity: ComponentActivity) {
    val app = LocalContext.current.app
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.factory(app))
    val user by authVm.user.collectAsStateWithLifecycle()
    val signingIn by authVm.signingIn.collectAsStateWithLifecycle()
    val error by authVm.error.collectAsStateWithLifecycle()

    if (user == null) {
        SignInScreen(
            onSignIn = { authVm.signIn(activity) },
            signingIn = signingIn,
            error = error,
        )
    } else {
        SpitePineRoot()
    }
}

@Composable
fun SpitePineRoot() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { dest ->
                item(
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) },
                    selected = dest == currentDestination,
                    onClick = { currentDestination = dest }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> HomeNavGraph()
            AppDestinations.REMINDERS -> DestinationStub(AppDestinations.REMINDERS)
            AppDestinations.ROUTES -> DestinationStub(AppDestinations.ROUTES)
            AppDestinations.SETTINGS -> SettingsScreen()
        }
    }
}

@Composable
private fun HomeNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = HomeRoute) {
        composable(HomeRoute) {
            HomeScreen(onAddClick = { nav.navigate(AddTreeRoute) })
        }
        composable(AddTreeRoute) {
            AddTreeScreen(
                onSaved = { nav.popBackStack() },
                onCancel = { nav.popBackStack() },
            )
        }
    }
}

private const val HomeRoute = "home"
private const val AddTreeRoute = "tree/add"

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Place),
    REMINDERS("Reminders", Icons.Default.Notifications),
    ROUTES("Routes", Icons.AutoMirrored.Filled.List),
    SETTINGS("Settings", Icons.Default.Settings),
}

@Composable
private fun DestinationStub(destination: AppDestinations, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destination.label,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
