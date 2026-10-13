package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class NavDestination(val route: String, val label: String, val icon: ImageVector) {
    // Business OS Destinations
    object Dashboard : NavDestination("dashboard", "Dashboard", Icons.Default.GridView)
    object Pos : NavDestination("pos", "POS", Icons.Default.PointOfSale)
    object AiChat : NavDestination("ai_chat", "AI Copilot", Icons.Default.SmartToy)
    object Inventory : NavDestination("inventory", "Inventory", Icons.Default.Inventory2)
    object Finance : NavDestination("finance", "Finance", Icons.Default.AccountBalanceWallet)
    object More : NavDestination("more", "Modules", Icons.Default.Apps)

    // Social Hub Destinations (Minimized Bottom Nav Bar)
    object SocialFeed : NavDestination("social_feed", "Home", Icons.Default.Home)
    object SocialReels : NavDestination("social_reels", "Reels", Icons.Default.OndemandVideo)
    object SocialPost : NavDestination("social_post", "Post", Icons.Default.AddCircle)
    object SocialMap : NavDestination("social_map", "Store Map", Icons.Default.Map)
    object SocialSettings : NavDestination("social_settings", "Settings", Icons.Default.Settings)
    object BusinessOsSwitch : NavDestination("switch_business", "Business OS", Icons.Default.Store)
}

val mainNavDestinations = listOf(
    NavDestination.Dashboard,
    NavDestination.Pos,
    NavDestination.AiChat,
    NavDestination.Inventory,
    NavDestination.Finance,
    NavDestination.More
)

val socialNavDestinations = listOf(
    NavDestination.SocialFeed,
    NavDestination.SocialReels,
    NavDestination.SocialPost,
    NavDestination.SocialMap,
    NavDestination.SocialSettings
)

@Composable
fun BizPilotBottomNavBar(
    activeAppMode: String = "BUSINESS",
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit = {},
    onSwitchAppMode: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val destinations = if (activeAppMode == "SOCIAL") socialNavDestinations else mainNavDestinations

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        destinations.forEach { dest ->
            val selected = (currentRoute == dest.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    when (dest.route) {
                        "social_settings" -> onOpenSettings()
                        "switch_business" -> onSwitchAppMode("BUSINESS")
                        else -> onNavigate(dest.route)
                    }
                },
                icon = {
                    if (dest.route == "social_post") {
                        androidx.compose.material3.Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = com.example.ui.theme.MpesaGreen,
                            modifier = Modifier.size(28.dp)
                        ) {
                            androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = dest.label,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

