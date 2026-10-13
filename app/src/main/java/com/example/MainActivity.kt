package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BizPilotBottomNavBar
import com.example.ui.components.BizPilotTopBar
import com.example.ui.dialogs.AppInstallationSetupWizard
import com.example.ui.screens.*
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BizPilotTheme
import com.example.ui.viewmodel.BizPilotViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: BizPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val businessName by viewModel.businessName.collectAsState()
            val userRole by viewModel.userRole.collectAsState()
            val currency by viewModel.currency.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val pendingSyncCount by viewModel.pendingSyncItemsCount.collectAsState()
            val isSyncing by viewModel.isOfflineSyncActive.collectAsState()

            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val hasCompletedSetup by viewModel.hasCompletedSetup.collectAsState()
            var showSetupWizard by remember { mutableStateOf(!hasCompletedSetup) }

            val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            BizPilotTheme(themeMode = themeMode) {
                var activeAppMode by remember { mutableStateOf(if (isLoggedIn) "BUSINESS" else "SOCIAL") }
                var socialTabRoute by remember { mutableStateOf("social_feed") }
                var showSearchEngineDialog by remember { mutableStateOf(false) }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                if (showSetupWizard) {
                    AppInstallationSetupWizard(
                        viewModel = viewModel,
                        onSetupFinished = { mode ->
                            activeAppMode = mode
                            showSetupWizard = false
                        }
                    )
                }

                Scaffold(
                    snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
                    topBar = {
                        BizPilotTopBar(
                            businessName = if (isLoggedIn) businessName else "Guest Explorer",
                            userRole = if (isLoggedIn) userRole else "Public Guest",
                            currentCurrency = currency,
                            themeMode = themeMode,
                            searchQuery = searchQuery,
                            pendingSyncCount = pendingSyncCount,
                            isSyncing = isSyncing,
                            activeAppMode = activeAppMode,
                            isLoggedIn = isLoggedIn,
                            onAppModeChange = { activeAppMode = it },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onCurrencyClick = {
                                val nextCurr = when (currency) {
                                    "KES" -> "USD"
                                    "USD" -> "NGN"
                                    "NGN" -> "GHS"
                                    "GHS" -> "UGX"
                                    else -> "KES"
                                }
                                viewModel.setCurrency(nextCurr)
                            },
                            onRoleClick = {
                                val nextRole = when (userRole) {
                                    "Owner" -> "Manager"
                                    "Manager" -> "Cashier"
                                    "Cashier" -> "Accountant"
                                    else -> "Owner"
                                }
                                viewModel.setUserRole(nextRole)
                            },
                            onTelegramModeClick = {
                                val nextMode = if (themeMode == AppThemeMode.TELEGRAM_MINI_APP) AppThemeMode.DARK else AppThemeMode.TELEGRAM_MINI_APP
                                viewModel.setThemeMode(nextMode)
                            },
                            onThemeToggleClick = { newTheme ->
                                viewModel.setThemeMode(newTheme)
                            },
                            onOpenSetupWizard = {
                                showSetupWizard = true
                            },
                            onOpenSearchEngine = {
                                showSearchEngineDialog = true
                            },
                            onSyncClick = {
                                viewModel.syncNow { msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            },
                            onLogoutClick = {
                                viewModel.logout()
                                activeAppMode = "SOCIAL"
                            }
                        )
                    },
                    bottomBar = {
                        BizPilotBottomNavBar(
                            activeAppMode = activeAppMode,
                            currentRoute = if (activeAppMode == "SOCIAL") socialTabRoute else currentRoute,
                            onNavigate = { route ->
                                if (activeAppMode == "SOCIAL") {
                                    socialTabRoute = route
                                } else {
                                    navController.navigate(route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onOpenSettings = {
                                if (activeAppMode == "SOCIAL") {
                                    socialTabRoute = "social_settings"
                                } else {
                                    showSetupWizard = true
                                }
                            },
                            onSwitchAppMode = { newMode ->
                                activeAppMode = newMode
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (activeAppMode == "SOCIAL") {
                            // Public Biz Social Hub - ACCESSIBLE BY EVERYONE
                            SocialFeedScreen(
                                viewModel = viewModel,
                                activeTabMode = socialTabRoute,
                                showSearchEngineOnLaunch = showSearchEngineDialog,
                                onDismissSearchEngine = { showSearchEngineDialog = false },
                                onTabModeChange = { socialTabRoute = it },
                                onOpenSetupWizard = { showSetupWizard = true },
                                onSwitchAppMode = { activeAppMode = it }
                            )
                        } else {
                            // Business OS - AUTHORIZED PERSONS ONLY
                            if (isLoggedIn) {
                                NavHost(
                                    navController = navController,
                                    startDestination = "dashboard"
                                ) {
                                    composable("dashboard") {
                                        DashboardScreen(
                                            viewModel = viewModel,
                                            onNavigate = { dest -> navController.navigate(dest) }
                                        )
                                    }
                                    composable("pos") {
                                        PosScreen(viewModel = viewModel)
                                    }
                                    composable("ai_chat") {
                                        AiAssistantScreen(viewModel = viewModel)
                                    }
                                    composable("inventory") {
                                        InventoryScreen(viewModel = viewModel)
                                    }
                                    composable("finance") {
                                        FinanceScreen(viewModel = viewModel)
                                    }
                                    composable("more") {
                                        MoreModulesScreen(viewModel = viewModel)
                                    }
                                }
                            } else {
                                // Security Gate for Unauthorized Users attempting to access Business OS
                                LoginScreen(
                                    viewModel = viewModel,
                                    onContinueAsGuest = {
                                        activeAppMode = "SOCIAL"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

