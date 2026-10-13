package com.example.ui.screens

import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.WhatsAppHelper

import coil.compose.AsyncImage
import coil.request.ImageRequest

data class BusinessPost(
    val id: String,
    val merchantName: String,
    val merchantCategory: String,
    val isVerified: Boolean = true,
    val timestamp: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val contentText: String,
    val productName: String? = null,
    val productPrice: Double? = null,
    val hashtags: List<String> = emptyList(),
    var likesCount: Int,
    var isLiked: Boolean = false,
    val comments: MutableList<PostComment> = mutableListOf(),
    val sharesCount: Int = 12,
    val mediaUrl: String? = null,
    val mediaType: String = "NONE", // "NONE", "IMAGE", "VIDEO"
    val soundTrack: String = "Original Sound - $merchantName",
    val viewsCount: Int = 1420
)

data class PostComment(
    val id: String,
    val authorName: String,
    val authorBadge: String = "Verified SME",
    val commentText: String,
    val timestamp: String,
    var likesCount: Int = 0,
    var isLiked: Boolean = false,
    val replies: MutableList<PostComment> = mutableListOf()
)

fun countTotalComments(comments: List<PostComment>): Int {
    var count = comments.size
    for (comment in comments) {
        count += countTotalComments(comment.replies)
    }
    return count
}

data class NearbyStore(
    val id: String,
    val name: String,
    val category: String,
    val distanceKm: Double,
    val address: String,
    val phone: String,
    val rating: Double,
    val latOffset: Float,
    val lngOffset: Float,
    val description: String = "Verified SME Partner Store offering direct M-Pesa deals and local store pickup.",
    val isVerified: Boolean = true,
    val latitude: Double = -1.286389,
    val longitude: Double = 36.817223,
    val isOpenNow: Boolean = true,
    val openingHours: String = "Open • Closes 8:00 PM",
    val currentPromotion: String? = null,
    val promoBadge: String? = null,
    val stockStatus: String = "In Stock (High Availability)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    viewModel: BizPilotViewModel,
    activeTabMode: String = "FEED",
    showSearchEngineOnLaunch: Boolean = false,
    onDismissSearchEngine: () -> Unit = {},
    onTabModeChange: (String) -> Unit = {},
    onOpenSetupWizard: () -> Unit = {},
    onSwitchAppMode: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency by viewModel.currency.collectAsState()

    val storeName by viewModel.businessName.collectAsState()
    val merchantName by viewModel.userName.collectAsState()
    val merchantCategory by viewModel.businessCategory.collectAsState()
    val merchantBio by viewModel.businessBio.collectAsState()
    val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsState()

    val currentMode = when (activeTabMode) {
        "social_reels", "REELS" -> "REELS"
        "social_map", "MAP" -> "MAP"
        "social_settings", "SETTINGS" -> "SETTINGS"
        else -> "FEED"
    }

    var selectedReelIndex by remember { mutableIntStateOf(0) }
    var showCreatePostModal by remember { mutableStateOf(false) }
    var showEditProfileModal by remember { mutableStateOf(false) }
    var showSearchEngineModal by remember { mutableStateOf(false) }
    var selectedPostForComments by remember { mutableStateOf<BusinessPost?>(null) }
    var selectedPostForSharing by remember { mutableStateOf<BusinessPost?>(null) }
    var selectedStoreForMapDetail by remember { mutableStateOf<NearbyStore?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    LaunchedEffect(showSearchEngineOnLaunch) {
        if (showSearchEngineOnLaunch) {
            showSearchEngineModal = true
        }
    }

    LaunchedEffect(activeTabMode) {
        if (activeTabMode == "social_post") {
            showCreatePostModal = true
        }
    }

    // Mock initial posts feed
    val posts = remember {
        mutableStateListOf(
            BusinessPost(
                id = "post_1",
                merchantName = "Safari Tech & Solar Mart",
                merchantCategory = "Electronics & Energy",
                isVerified = true,
                timestamp = "25 mins ago",
                locationName = "Nairobi CBD, Kimathi Street #4",
                latitude = -1.286389,
                longitude = 36.817223,
                contentText = "⚡ HUGE Flash Sale on Hybrid Solar Inverters (3.5kVA)! Ideal for local shops during power surges. Comes with 2-year warranty and free installation in Nairobi area.",
                productName = "Hybrid Solar Inverter 3.5kVA",
                productPrice = 48500.0,
                hashtags = listOf("#SolarPower", "#NairobiDeals", "#BizPilotMerchants"),
                likesCount = 42,
                mediaUrl = "file:///app/src/main/res/drawable/solar_inverter_reel_1785398090536.jpg",
                mediaType = "VIDEO",
                soundTrack = "🎵 Safari Tech • Hybrid Inverter Showcase Reel",
                viewsCount = 2450,
                comments = mutableListOf(
                    PostComment(
                        id = "c1",
                        authorName = "Mama Mboga Supplies",
                        authorBadge = "Wholesale Buyer",
                        commentText = "Is delivery included to Westlands area for 2 units?",
                        timestamp = "15m ago",
                        likesCount = 3,
                        replies = mutableListOf(
                            PostComment(
                                id = "c1_r1",
                                authorName = "Safari Tech & Solar Mart",
                                authorBadge = "Post Owner",
                                commentText = "Yes! Free delivery and same-day installation for orders above KES 20,000 in Westlands.",
                                timestamp = "10m ago",
                                likesCount = 5,
                                replies = mutableListOf(
                                    PostComment(
                                        id = "c1_r1_1",
                                        authorName = "Mama Mboga Supplies",
                                        authorBadge = "Wholesale Buyer",
                                        commentText = "Awesome! Placing order right away.",
                                        timestamp = "5m ago",
                                        likesCount = 2
                                    )
                                )
                            )
                        )
                    ),
                    PostComment(
                        id = "c2",
                        authorName = "Kileleshwa Hardware",
                        authorBadge = "Partner SME",
                        commentText = "Do you offer wholesale bulk pricing for reseller stores?",
                        timestamp = "8m ago",
                        likesCount = 1
                    )
                )
            ),
            BusinessPost(
                id = "post_2",
                merchantName = "Mama Mboga Fresh Organics",
                merchantCategory = "Groceries & Produce",
                isVerified = true,
                timestamp = "2 hours ago",
                locationName = "City Market Stall 42, Nairobi",
                latitude = -1.283333,
                longitude = 36.816667,
                contentText = "🥦 Fresh farm-fresh Spinach, Tomatoes, and Avocados delivered straight from Kinangop farms this morning! Wholesale prices for restaurants and local caterers.",
                productName = "Fresh Produce Wholesale Crate",
                productPrice = 3200.0,
                hashtags = listOf("#FarmFresh", "#WholesaleProduce", "#OrganicKE"),
                likesCount = 28,
                mediaUrl = "file:///app/src/main/res/drawable/fresh_produce_reel_1785398103126.jpg",
                mediaType = "VIDEO",
                soundTrack = "🎵 Farm Fresh Kinangop Produce Highlights",
                viewsCount = 1820,
                comments = mutableListOf(
                    PostComment(
                        id = "c3",
                        authorName = "Chef Denis Bistro",
                        authorBadge = "Wholesale Buyer",
                        commentText = "Reserve 3 crates for us please!",
                        timestamp = "1h ago",
                        likesCount = 4
                    )
                )
            ),
            BusinessPost(
                id = "post_3",
                merchantName = "Kileleshwa Fashion Boutique",
                merchantCategory = "Apparel & Accessories",
                isVerified = true,
                timestamp = "5 hours ago",
                locationName = "Kileleshwa Mall, Shop 12B",
                latitude = -1.278,
                longitude = 36.791,
                contentText = "✨ New arrivals in official office wear & designer Ankara dresses! Stock limited. DM or tap order below to reserve via M-Pesa.",
                productName = "Designer Ankara Blazer Set",
                productPrice = 5800.0,
                hashtags = listOf("#FashionKE", "#AnkaraStyle", "#ShopLocal"),
                likesCount = 67,
                mediaUrl = "file:///app/src/main/res/drawable/fashion_gadgets_reel_1785398116533.jpg",
                mediaType = "VIDEO",
                soundTrack = "🎵 Nairobi Fashion Week SME Lookbook",
                viewsCount = 3110,
                comments = mutableListOf()
            )
        )
    }

    // Nearby Stores for Google Maps real-time access & promotions feature
    val nearbyStores = remember {
        listOf(
            NearbyStore(
                id = "s1", name = "Safari Tech & Solar", category = "Electronics", distanceKm = 0.4,
                address = "Kimathi Street #4, Nairobi CBD", phone = "+254 712 345678", rating = 4.9,
                latOffset = -0.2f, lngOffset = 0.1f,
                isOpenNow = true, openingHours = "Open • Closes 8:00 PM",
                currentPromotion = "⚡ 20% OFF 3.5kVA Solar Inverters + Free CBD Pickup",
                promoBadge = "🔥 20% OFF", stockStatus = "In Stock (14 Units Available)"
            ),
            NearbyStore(
                id = "s2", name = "Mama Mboga Organics", category = "Groceries", distanceKm = 0.8,
                address = "City Market Stall 42", phone = "+254 722 112233", rating = 4.8,
                latOffset = 0.3f, lngOffset = -0.2f,
                isOpenNow = true, openingHours = "Open • Closes 7:00 PM",
                currentPromotion = "🥑 Buy 2 Fresh Produce Baskets, Get 1 Organic Juice",
                promoBadge = "🎁 BOGO DEAL", stockStatus = "Fresh Batch Arrived Today"
            ),
            NearbyStore(
                id = "s3", name = "Kileleshwa Boutique", category = "Fashion", distanceKm = 2.3,
                address = "Kileleshwa Mall Shop 12B", phone = "+254 733 998877", rating = 4.7,
                latOffset = -0.4f, lngOffset = -0.3f,
                isOpenNow = true, openingHours = "Open • Closes 9:00 PM",
                currentPromotion = "👗 30% Flash Sale on Ankara Dresses & Accessories",
                promoBadge = "⚡ 30% OFF", stockStatus = "Limited Stock (5 items left)"
            ),
            NearbyStore(
                id = "s4", name = "Mombasa Road Hardware", category = "Hardware", distanceKm = 4.1,
                address = "Mombasa Rd Mile 6", phone = "+254 700 554433", rating = 4.6,
                latOffset = 0.5f, lngOffset = 0.4f,
                isOpenNow = false, openingHours = "Closed • Opens 8:00 AM",
                currentPromotion = "🛠️ Free M-Pesa Till Shipping on Cement & Paints",
                promoBadge = "🚚 FREE SHIP", stockStatus = "In Stock (Bulk Orders Ready)"
            )
        )
    }

    val categories = listOf("All", "Electronics", "Groceries", "Fashion", "Hardware", "Services")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Mode Sub-Header Row (Only shown for secondary tabs like REELS, MAP, SETTINGS)
        if (currentMode != "FEED") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when (currentMode) {
                            "REELS" -> Color.Red.copy(alpha = 0.9f)
                            "MAP" -> MpesaGreen
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (currentMode) {
                                    "REELS" -> Icons.Default.OndemandVideo
                                    "MAP" -> Icons.Default.Map
                                    "SETTINGS" -> Icons.Default.Settings
                                    else -> Icons.Default.DynamicFeed
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = when (currentMode) {
                                "REELS" -> "TikTok SME Reels 🎬"
                                "MAP" -> "SME Store Map 📍"
                                else -> "Social Hub & Platform Settings ⚙️"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = when (currentMode) {
                                "REELS" -> "Swipe vertical to discover product videos"
                                "MAP" -> "Interactive merchant locations in Nairobi"
                                else -> "Manage account, theme, currency & setup wizard"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (currentMode == "FEED") {
            // SINGLE UNIFIED SEARCH ENGINE BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stores, products, locations, deals...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MpesaGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_engine_feed_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MpesaGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // SEARCH ENGINE & SOCIAL FEED LIST
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                val filteredPosts = posts.filter { post ->
                    (selectedCategoryFilter == "All" || post.merchantCategory.contains(selectedCategoryFilter, ignoreCase = true)) &&
                            (searchQuery.isBlank() || post.contentText.contains(searchQuery, ignoreCase = true) || post.merchantName.contains(searchQuery, ignoreCase = true))
                }

                items(filteredPosts, key = { it.id }) { post ->
                    BusinessPostCard(
                        post = post,
                        viewModel = viewModel,
                        onLikeClick = {
                            post.isLiked = !post.isLiked
                            post.likesCount += if (post.isLiked) 1 else -1
                        },
                        onCommentClick = {
                            selectedPostForComments = post
                        },
                        onShareWhatsApp = {
                            selectedPostForSharing = post
                        },
                        onMapClick = {
                            onTabModeChange("social_map")
                            Toast.makeText(context, "Locating ${post.merchantName} on Map...", Toast.LENGTH_SHORT).show()
                        },
                        onWatchReel = { targetPost ->
                            val idx = posts.indexOf(targetPost)
                            if (idx >= 0) selectedReelIndex = idx
                            onTabModeChange("social_reels")
                        }
                    )
                }
            }
        } else if (currentMode == "REELS") {
            // FULLSCREEN TIKTOK REELS FEED
            TikTokReelsFeedView(
                posts = posts,
                initialIndex = selectedReelIndex,
                viewModel = viewModel,
                onLikeClick = { post ->
                    post.isLiked = !post.isLiked
                    post.likesCount += if (post.isLiked) 1 else -1
                },
                onCommentClick = { post ->
                    selectedPostForComments = post
                },
                onShareClick = { post ->
                    selectedPostForSharing = post
                },
                onOrderClick = { post ->
                    selectedPostForSharing = post
                },
                onCloseReels = {
                    onTabModeChange("social_feed")
                }
            )
        } else if (currentMode == "SETTINGS") {
            // SOCIAL HUB SETTINGS & PLATFORM CONTROL VIEW
            SocialSettingsScreenView(
                viewModel = viewModel,
                onOpenSetupWizard = onOpenSetupWizard,
                onOpenEditProfile = { showEditProfileModal = true },
                onSwitchAppMode = onSwitchAppMode
            )
        } else {
            // GOOGLE MAP ACCESS / STORE LOCATOR VIEW
            BusinessMapAccessView(
                stores = nearbyStores,
                onSelectStore = { store ->
                    selectedStoreForMapDetail = store
                },
                onOpenDirections = { store ->
                    Toast.makeText(context, "Opening Navigation to ${store.name} (${store.address})", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // SME SEARCH ENGINE DIALOG
    if (showSearchEngineModal) {
        SmeSearchEngineDialog(
            initialQuery = searchQuery,
            viewModel = viewModel,
            posts = posts,
            stores = nearbyStores,
            onDismiss = {
                showSearchEngineModal = false
                onDismissSearchEngine()
            },
            onNavigateToStoreMap = { store ->
                showSearchEngineModal = false
                onTabModeChange("social_map")
            },
            onOrderProduct = { post ->
                showSearchEngineModal = false
                selectedPostForSharing = post
            }
        )
    }

    // CREATE POST MODAL DIALOG
    if (showCreatePostModal) {
        CreateBusinessPostDialog(
            viewModel = viewModel,
            onDismiss = {
                showCreatePostModal = false
                if (activeTabMode == "social_post") {
                    onTabModeChange("social_feed")
                }
            },
            onPostCreated = { newPost ->
                posts.add(0, newPost)
                showCreatePostModal = false
                if (activeTabMode == "social_post") {
                    onTabModeChange("social_feed")
                }
                Toast.makeText(context, "🎉 Business Post published to Biz Social Hub!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // COMMENTS BOTTOM SHEET / MODAL DIALOG
    selectedPostForComments?.let { post ->
        PostCommentsDialog(
            post = post,
            onDismiss = { selectedPostForComments = null },
            onAddComment = { targetComment, commentText ->
                val newComment = PostComment(
                    id = "c_${System.currentTimeMillis()}",
                    authorName = viewModel.userName.value.ifBlank { "Safari Retail Mart" },
                    authorBadge = if (post.merchantName.contains("Safari", ignoreCase = true)) "Post Owner" else "Verified SME",
                    commentText = commentText,
                    timestamp = "Just now"
                )
                if (targetComment == null) {
                    post.comments.add(newComment)
                } else {
                    targetComment.replies.add(newComment)
                }
                Toast.makeText(context, if (targetComment == null) "Comment published!" else "Reply published to ${targetComment.authorName}!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // MULTI-PLATFORM SHARE MODAL DIALOG
    selectedPostForSharing?.let { post ->
        SharePostModalDialog(
            post = post,
            viewModel = viewModel,
            onDismiss = { selectedPostForSharing = null }
        )
    }

    // BUSINESS PROFILE EDIT MODAL DIALOG
    if (showEditProfileModal) {
        EditBusinessProfileModalDialog(
            currentName = merchantName,
            currentCategory = merchantCategory,
            currentBio = merchantBio,
            onDismiss = { showEditProfileModal = false },
            onSaveProfile = { newName, newCategory, newBio ->
                val oldName = merchantName
                viewModel.updateBusinessProfile(newName, newCategory, newBio) { success, msg ->
                    if (success) {
                        // Update posts on feed live
                        posts.forEachIndexed { idx, post ->
                            if (post.merchantName == oldName || post.merchantName.contains("Safari", ignoreCase = true)) {
                                posts[idx] = post.copy(
                                    merchantName = newName,
                                    merchantCategory = newCategory
                                )
                            }
                        }
                        Toast.makeText(context, "✅ $msg", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ $msg", Toast.LENGTH_SHORT).show()
                    }
                    showEditProfileModal = false
                }
            }
        )
    }

    // STORE MAP DETAIL MODAL
    selectedStoreForMapDetail?.let { store ->
        AlertDialog(
            onDismissRequest = { selectedStoreForMapDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MpesaGreen)
                    Text(store.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MpesaGreen.copy(alpha = 0.15f)) {
                        Text("⭐ ${store.rating} Rating • ${store.distanceKm} km away", color = MpesaGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                    }
                    Text("📍 Address: ${store.address}", style = MaterialTheme.typography.bodyMedium)
                    Text("📞 Contact: ${store.phone}", style = MaterialTheme.typography.bodyMedium)
                    Text("🏷️ Category: ${store.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        WhatsAppHelper.openWhatsAppChat(context, store.phone, "Hello ${store.name}, I found your business on BizPilot Map!")
                        selectedStoreForMapDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStoreForMapDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BusinessPostCard(
    post: BusinessPost,
    viewModel: BizPilotViewModel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onMapClick: () -> Unit,
    onWatchReel: (BusinessPost) -> Unit = {}
) {
    GlassCard(
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: Merchant Profile & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                post.merchantName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(post.merchantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (post.isVerified) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified Merchant",
                                    tint = TelegramBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            "${post.merchantCategory} • ${post.timestamp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onMapClick) {
                    Icon(Icons.Default.Place, contentDescription = "Locate on Map", tint = MpesaGreen)
                }
            }

            // Location Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMapClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(16.dp))
                    Text(post.locationName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Post Text Content
            Text(
                post.contentText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.hashtags.forEach { tag ->
                        Text(
                            tag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TelegramBlue
                        )
                    }
                }
            }

            // Media Reel / Large Video / Image Preview Showcase
            if (!post.mediaUrl.isNullOrBlank() || post.mediaType != "NONE") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clickable { onWatchReel(post) }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!post.mediaUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = post.mediaUrl,
                                contentDescription = "Post Media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Gradient Shadow Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        )

                        // Top Badges
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Red.copy(alpha = 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(
                                        if (post.mediaType == "VIDEO") "VIDEO REEL" else "PHOTO POST",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    "👁 ${post.viewsCount} views",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Play Button Icon Overlay in Center
                        Surface(
                            shape = CircleShape,
                            color = MpesaGreen.copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.Center)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play Reel",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Bottom-Right Launch Fullscreen Reel Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.8f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text(
                                    "Fullscreen Reel ↗",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Product Listing Showcase Card Inside Post (if product attached)
            if (post.productName != null && post.productPrice != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MpesaGreen.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(shape = CircleShape, color = MpesaGreen.copy(alpha = 0.2f)) {
                                Text("FEATURED PRODUCT", color = MpesaGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(post.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                viewModel.formatPrice(post.productPrice),
                                fontWeight = FontWeight.ExtraBold,
                                color = MpesaGreen,
                                fontSize = 15.sp
                            )
                        }

                        Button(
                            onClick = onShareWhatsApp,
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Order", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Order Now", fontSize = 12.sp)
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Reaction / Social Controls (Like, Comment, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like / React Button
                TextButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${post.likesCount}",
                        color = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                // Comment Button
                TextButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${countTotalComments(post.comments)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }

                // Share to WhatsApp Button
                TextButton(onClick = onShareWhatsApp) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MpesaGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Share", color = MpesaGreen, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun BusinessMapAccessView(
    stores: List<NearbyStore>,
    onSelectStore: (NearbyStore) -> Unit,
    onOpenDirections: (NearbyStore) -> Unit
) {
    var selectedStore by remember { mutableStateOf<NearbyStore?>(stores.firstOrNull()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Deals Only", "Groceries", "Tech", "Dining", "Services")

    val filteredStores = remember(stores, searchQuery, selectedCategory) {
        stores.filter { store ->
            val matchesCategory = when (selectedCategory) {
                "Deals Only" -> store.promoBadge != null
                "Groceries" -> store.category.contains("Grocer", ignoreCase = true) || store.category.contains("Market", ignoreCase = true) || store.category.contains("Wholesale", ignoreCase = true)
                "Tech" -> store.category.contains("Tech", ignoreCase = true) || store.category.contains("Electronic", ignoreCase = true) || store.category.contains("Phone", ignoreCase = true)
                "Dining" -> store.category.contains("Cafe", ignoreCase = true) || store.category.contains("Food", ignoreCase = true) || store.category.contains("Restau", ignoreCase = true)
                "Services" -> store.category.contains("Service", ignoreCase = true) || store.category.contains("Repair", ignoreCase = true) || store.category.contains("Salons", ignoreCase = true)
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    store.name.contains(searchQuery, ignoreCase = true) ||
                    store.category.contains(searchQuery, ignoreCase = true) ||
                    store.address.contains(searchQuery, ignoreCase = true) ||
                    (store.currentPromotion?.contains(searchQuery, ignoreCase = true) ?: false)

            matchesCategory && matchesQuery
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar & Filter Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stores, categories, promos...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MpesaGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 11.sp, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MpesaGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Interactive Google Map Graphical Canvas View Component with Real-Time Panning & Deals
        SearchInteractiveGoogleMapView(
            stores = filteredStores,
            selectedStore = selectedStore,
            onSelectStore = { store ->
                selectedStore = store
                store?.let { onSelectStore(it) }
            },
            isSatelliteView = true,
            onToggleSatellite = {}
        )

        Text(
            "Nearby Partner Stores (${filteredStores.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Store List below map with live availability and active promotion indicators
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredStores) { store ->
                GlassCard(
                    borderColor = if (selectedStore?.id == store.id) MpesaGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedStore = store
                            onSelectStore(store)
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(store.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        shape = CircleShape,
                                        color = if (store.isOpenNow) MpesaGreen else Color.Red,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                    Surface(shape = RoundedCornerShape(10.dp), color = MpesaGreen.copy(alpha = 0.15f)) {
                                        Text("${store.distanceKm} km", color = MpesaGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text(store.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            IconButton(onClick = { onOpenDirections(store) }) {
                                Icon(Icons.Default.Navigation, contentDescription = "Directions", tint = TelegramBlue)
                            }
                        }

                        // Availability Tag & Active Promotion Tag
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (store.isOpenNow) "🟢 ${store.openingHours}" else "🔴 ${store.openingHours}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (store.isOpenNow) MpesaGreen else Color.Red
                            )

                            if (store.promoBadge != null) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDC2626)) {
                                    Text(store.promoBadge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        if (store.currentPromotion != null) {
                            Text(
                                text = "🔥 ${store.currentPromotion}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBusinessPostDialog(
    viewModel: BizPilotViewModel,
    onDismiss: () -> Unit,
    onPostCreated: (BusinessPost) -> Unit
) {
    val context = LocalContext.current
    val productsList by viewModel.products.collectAsState()

    var contentText by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<String?>(null) }
    var productPrice by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("Nairobi Store Branch #1") }
    var category by remember { mutableStateOf("Electronics") }
    var isFetchingGps by remember { mutableStateOf(false) }

    var selectedMediaType by remember { mutableStateOf("VIDEO") } // "NONE", "IMAGE", "VIDEO"
    var mediaUrl by remember { mutableStateOf("file:///app/src/main/res/drawable/solar_inverter_reel_1785398090536.jpg") }

    val presetMediaList = listOf(
        Triple("Solar Reel ⚡", "file:///app/src/main/res/drawable/solar_inverter_reel_1785398090536.jpg", "VIDEO"),
        Triple("Fresh Produce 🥦", "file:///app/src/main/res/drawable/fresh_produce_reel_1785398103126.jpg", "VIDEO"),
        Triple("Fashion Look 👗", "file:///app/src/main/res/drawable/fashion_gadgets_reel_1785398116533.jpg", "VIDEO")
    )

    val presetLocations = listOf(
        "Moi Ave Store (-1.2864, 36.8172)",
        "Westlands Mall (-1.2674, 36.8115)",
        "Eastleigh Wholesale (-1.2778, 36.8481)",
        "Industrial Area Gate 4 (-1.3082, 36.8526)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = MpesaGreen)
                Text("Publish Media Post / TikTok Reel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("What product or deal are you promoting?") },
                    placeholder = { Text("e.g., 20% Discount on all Solar Chargers today only!") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Store Location Tag:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        
                        // Google Play Services Location Button
                        AssistChip(
                            onClick = {
                                isFetchingGps = true
                                try {
                                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                                    @SuppressLint("MissingPermission")
                                    val task = fusedClient.lastLocation
                                    task.addOnSuccessListener { loc ->
                                        if (loc != null) {
                                            locationName = "📍 Nairobi Store GPS (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                                        } else {
                                            locationName = "📍 Store Branch - Moi Ave (-1.2864, 36.8172)"
                                        }
                                        Toast.makeText(context, "⚡ GPS Tagged via Google Play Services Location API!", Toast.LENGTH_SHORT).show()
                                        isFetchingGps = false
                                    }.addOnFailureListener {
                                        locationName = "📍 Store Branch - Westlands (-1.2674, 36.8115)"
                                        Toast.makeText(context, "GPS Tagged via Play Services Location API", Toast.LENGTH_SHORT).show()
                                        isFetchingGps = false
                                    }
                                } catch (e: Exception) {
                                    locationName = "📍 Store Branch - CBD Nairobi (-1.2833, 36.8167)"
                                    Toast.makeText(context, "GPS Location Tagged", Toast.LENGTH_SHORT).show()
                                    isFetchingGps = false
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (isFetchingGps) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = MpesaGreen)
                                    } else {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Detect GPS", tint = MpesaGreen, modifier = Modifier.size(13.dp))
                                    }
                                    Text("Detect GPS (Play Services)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.5f))
                        )
                    }

                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Physical Location / GPS Tag") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MpesaGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Quick Preset Locations
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetLocations) { loc ->
                            FilterChip(
                                selected = locationName == loc,
                                onClick = { locationName = loc },
                                label = { Text("📍 $loc", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Media Format Type Selector
                Text("Post Format:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedMediaType == "VIDEO",
                        onClick = { selectedMediaType = "VIDEO" },
                        label = { Text("Video Reel 🎬", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedMediaType == "IMAGE",
                        onClick = { selectedMediaType = "IMAGE" },
                        label = { Text("Photo Image 📷", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedMediaType == "NONE",
                        onClick = { selectedMediaType = "NONE" },
                        label = { Text("Text Only 📝", fontSize = 11.sp) }
                    )
                }

                if (selectedMediaType != "NONE") {
                    Text("Attach SME Media Clip / Preset:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetMediaList) { (label, url, type) ->
                            FilterChip(
                                selected = mediaUrl == url,
                                onClick = {
                                    mediaUrl = url
                                    selectedMediaType = type
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = mediaUrl,
                        onValueChange = { mediaUrl = it },
                        label = { Text("Image / Video URL") },
                        placeholder = { Text("Paste image or video clip link") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Media Preview Box
                    if (mediaUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = mediaUrl,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MpesaGreen,
                                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                                ) {
                                    Text("Media Preview", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                // Select inventory product optional dropdown/fill
                if (productsList.isNotEmpty()) {
                    Text("Attach Item from Inventory (Optional):", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(productsList) { item ->
                            FilterChip(
                                selected = selectedProduct == item.name,
                                onClick = {
                                    if (selectedProduct == item.name) {
                                        selectedProduct = null
                                        productPrice = ""
                                    } else {
                                        selectedProduct = item.name
                                        productPrice = item.sellingPrice.toString()
                                    }
                                },
                                label = { Text(item.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                if (selectedProduct != null) {
                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Product Price") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (contentText.isBlank()) {
                        Toast.makeText(context, "Please enter post description", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val priceVal = productPrice.toDoubleOrNull()
                    val newPost = BusinessPost(
                        id = "post_${System.currentTimeMillis()}",
                        merchantName = viewModel.userName.value.ifBlank { "Safari Retail Mart" },
                        merchantCategory = category,
                        isVerified = true,
                        timestamp = "Just now",
                        locationName = locationName,
                        latitude = -1.286389,
                        longitude = 36.817223,
                        contentText = contentText,
                        productName = selectedProduct,
                        productPrice = priceVal,
                        hashtags = listOf("#BizPilot", "#KenyaBusiness", "#TikTokSME"),
                        likesCount = 0,
                        mediaUrl = if (selectedMediaType == "NONE") null else mediaUrl,
                        mediaType = selectedMediaType,
                        soundTrack = "🎵 ${viewModel.userName.value.ifBlank { "Safari Retail Mart" }} • Product Highlights",
                        viewsCount = 1
                    )
                    onPostCreated(newPost)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
            ) {
                Text("Publish to Social Hub")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PostCommentsDialog(
    post: BusinessPost,
    onDismiss: () -> Unit,
    onAddComment: (PostComment?, String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<PostComment?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("SME Discussion Thread", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${countTotalComments(post.comments)} responses • ${post.merchantName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                if (post.comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No comments yet. Start the business conversation!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(post.comments, key = { it.id }) { comment ->
                            NestedCommentItem(
                                comment = comment,
                                depth = 0,
                                onReply = { target ->
                                    replyingToComment = target
                                },
                                onLike = { target ->
                                    target.isLiked = !target.isLiked
                                    target.likesCount += if (target.isLiked) 1 else -1
                                }
                            )
                        }
                    }
                }

                // Reply indicator banner if replying to a specific comment
                AnimatedVisibility(visible = replyingToComment != null) {
                    replyingToComment?.let { target ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MpesaGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Reply, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(16.dp))
                                    Text(
                                        "Replying to @${target.authorName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MpesaGreen
                                    )
                                }

                                IconButton(
                                    onClick = { replyingToComment = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = {
                            Text(
                                if (replyingToComment != null) "Reply to @${replyingToComment?.authorName}..." else "Write a comment..."
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(replyingToComment, newCommentText)
                                newCommentText = ""
                                replyingToComment = null
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MpesaGreen)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun NestedCommentItem(
    comment: PostComment,
    depth: Int = 0,
    onReply: (PostComment) -> Unit,
    onLike: (PostComment) -> Unit
) {
    val indentDp = (depth * 14).dp
    var isLiked by remember { mutableStateOf(comment.isLiked) }
    var likesCount by remember { mutableStateOf(comment.likesCount) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentDp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Vertical connecting line for nested child replies
            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(MpesaGreen.copy(alpha = 0.5f))
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (depth == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = if (depth == 0) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Header: Author Name, Badge, Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (comment.authorBadge == "Post Owner") MpesaGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        comment.authorName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            Surface(
                                shape = CircleShape,
                                color = when (comment.authorBadge) {
                                    "Post Owner" -> MpesaGreen.copy(alpha = 0.2f)
                                    "Wholesale Buyer" -> SecondaryAmber.copy(alpha = 0.2f)
                                    else -> TelegramBlue.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    comment.authorBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (comment.authorBadge) {
                                        "Post Owner" -> MpesaGreen
                                        "Wholesale Buyer" -> SecondaryAmber
                                        else -> TelegramBlue
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(comment.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Comment Text
                    Text(comment.commentText, style = MaterialTheme.typography.bodyMedium, fontSize = 13.sp)

                    Spacer(Modifier.height(6.dp))

                    // Comment Actions (Like & Reply buttons)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    onLike(comment)
                                    isLiked = comment.isLiked
                                    likesCount = comment.likesCount
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like comment",
                                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (likesCount > 0) {
                                Text("$likesCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        TextButton(
                            onClick = { onReply(comment) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(12.dp), tint = MpesaGreen)
                            Spacer(Modifier.width(2.dp))
                            Text("Reply", fontSize = 11.sp, color = MpesaGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Render nested replies recursively
        if (comment.replies.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            comment.replies.forEach { reply ->
                NestedCommentItem(
                    comment = reply,
                    depth = depth + 1,
                    onReply = onReply,
                    onLike = onLike
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

object SocialShareHelper {
    fun generateShareText(post: BusinessPost, formattedPrice: String?): String {
        val sb = StringBuilder()
        sb.append("📢 *${post.merchantName}*\n")
        sb.append("🏷️ Category: ${post.merchantCategory}\n\n")
        sb.append("${post.contentText}\n\n")
        if (post.productName != null && formattedPrice != null) {
            sb.append("📦 *Product Offer:* ${post.productName}\n")
            sb.append("💰 *Price:* $formattedPrice\n")
        }
        sb.append("📍 *Location:* ${post.locationName}\n")
        if (post.hashtags.isNotEmpty()) {
            sb.append("\n${post.hashtags.joinToString(" ")}\n")
        }
        sb.append("\n📲 Shared via BizPilot SME Commerce Hub")
        return sb.toString()
    }

    fun shareToTarget(context: Context, packageName: String?, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        if (packageName.isNullOrEmpty()) {
            val chooserIntent = Intent.createChooser(sendIntent, "Share post via...")
            context.startActivity(chooserIntent)
        } else {
            sendIntent.setPackage(packageName)
            try {
                context.startActivity(sendIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Selected app not installed. Opening system share options...",
                    Toast.LENGTH_SHORT
                ).show()
                val chooserIntent = Intent.createChooser(sendIntent, "Share post via...")
                context.startActivity(chooserIntent)
            }
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BizPilot Post", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "📋 Post & product details copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}

data class SocialPlatformOption(
    val id: String,
    val name: String,
    val packageName: String?,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    val brandColor: Color
)

@Composable
fun SharePostModalDialog(
    post: BusinessPost,
    viewModel: BizPilotViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedPrice = post.productPrice?.let { viewModel.formatPrice(it) }
    val shareText = remember(post) { SocialShareHelper.generateShareText(post, formattedPrice) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val platforms = remember(primaryColor) {
        listOf(
            SocialPlatformOption("whatsapp", "WhatsApp", "com.whatsapp", Icons.Default.Chat, Color(0xFF25D366)),
            SocialPlatformOption("telegram", "Telegram", "org.telegram.messenger", Icons.Default.Send, Color(0xFF229ED9)),
            SocialPlatformOption("instagram", "Instagram", "com.instagram.android", Icons.Default.CameraAlt, Color(0xFFE1306C)),
            SocialPlatformOption("facebook", "Facebook", "com.facebook.katana", Icons.Default.Public, Color(0xFF1877F2)),
            SocialPlatformOption("twitter", "X (Twitter)", "com.twitter.android", Icons.Default.AlternateEmail, Color(0xFF1DA1F2)),
            SocialPlatformOption("tiktok", "TikTok", "com.zhiliaoapp.musically", Icons.Default.MusicNote, Color(0xFF000000)),
            SocialPlatformOption("system", "More Apps", null, Icons.Default.Share, primaryColor)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MpesaGreen)
                    Column {
                        Text("Share Business Post", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Intent-based Multi-Platform Sharing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Post Preview Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(post.merchantName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MpesaGreen)
                            Text(post.locationName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            post.contentText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            fontSize = 12.sp
                        )
                        if (post.productName != null) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MpesaGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "📦 ${post.productName} • $formattedPrice",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MpesaGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Text("Direct Social Media Channels:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                // Social platforms horizontal row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(platforms) { platform ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = platform.brandColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, platform.brandColor.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                SocialShareHelper.shareToTarget(context, platform.packageName, shareText)
                                onDismiss()
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = platform.brandColor,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = platform.iconVector,
                                            contentDescription = platform.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    platform.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Copy to Clipboard Action
                OutlinedButton(
                    onClick = {
                        SocialShareHelper.copyToClipboard(context, shareText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Post Text & Product Details", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun EditBusinessProfileModalDialog(
    currentName: String,
    currentCategory: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var categoryInput by remember { mutableStateOf(currentCategory) }
    var bioInput by remember { mutableStateOf(currentBio) }

    val suggestedCategories = listOf(
        "Solar & Consumer Electronics",
        "Wholesale Fresh Produce",
        "Fashion & Boutique Apparel",
        "Hardware & Construction",
        "Restaurant & Food Catering",
        "General Retail Mart"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = MpesaGreen)
                    Column {
                        Text("Edit SME Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Firestore Cloud Sync Active", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Business Name Field
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Business / Merchant Name") },
                    placeholder = { Text("e.g., Safari Retail & Tech Mart") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MpesaGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Business Category Field
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("Business Category") },
                    placeholder = { Text("e.g., Solar & Electronics") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = MpesaGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Suggested Categories Chips
                Text("Quick Category Selection:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedCategories) { cat ->
                        FilterChip(
                            selected = categoryInput == cat,
                            onClick = { categoryInput = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MpesaGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Business Bio / Description Field
                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it },
                    label = { Text("Business Bio / Description") },
                    placeholder = { Text("Describe your products, location, or M-Pesa deals...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = MpesaGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4
                )

                // Cloud Sync Notice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MpesaGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(20.dp))
                        Text(
                            "Saving updates your social feed posts and syncs your profile to Firestore cloud storage.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onSaveProfile(nameInput, categoryInput, bioInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save & Sync to Firestore", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TikTokReelsFeedView(
    posts: List<BusinessPost>,
    initialIndex: Int = 0,
    viewModel: BizPilotViewModel,
    onLikeClick: (BusinessPost) -> Unit,
    onCommentClick: (BusinessPost) -> Unit,
    onShareClick: (BusinessPost) -> Unit,
    onOrderClick: (BusinessPost) -> Unit,
    onCloseReels: () -> Unit
) {
    val context = LocalContext.current
    var currentPostIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, (posts.size - 1).coerceAtLeast(0))) }

    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("No Video Reels Available", color = Color.White, fontWeight = FontWeight.Bold)
                TextButton(onClick = onCloseReels) {
                    Text("Return to Social Feed", color = MpesaGreen)
                }
            }
        }
        return
    }

    val currentPost = posts[currentPostIndex.coerceIn(0, posts.size - 1)]

    var isPlaying by remember(currentPost.id) { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "disc")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val progressAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    fun getCommentsTotal(list: List<PostComment>): Int {
        return list.sumOf { 1 + getCommentsTotal(it.replies) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // FULLSCREEN MEDIA BACKGROUND
        if (!currentPost.mediaUrl.isNullOrBlank()) {
            AsyncImage(
                model = currentPost.mediaUrl,
                contentDescription = "TikTok Reel Media",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPlaying = !isPlaying }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617))
                        )
                    )
                    .clickable { isPlaying = !isPlaying }
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(80.dp))
                    Text(currentPost.merchantName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Full Screen SME Showcase Reel", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        // Dark gradient overlay top and bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // Animated play/pause indicator overlay if paused
        if (!isPlaying) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }

        // TOP CONTROLS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onCloseReels) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Red.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(6.dp)) {}
                        Text("REEL LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = { isMuted = !isMuted }) {
                    Icon(
                        if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text("HD 1080p", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
        }

        // SWIPE REEL PREV / NEXT INDICATOR BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPostIndex > 0) {
                IconButton(
                    onClick = { currentPostIndex-- },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = "Prev Reel", tint = Color.White)
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }

            if (currentPostIndex < posts.size - 1) {
                IconButton(
                    onClick = { currentPostIndex++ },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.NavigateNext, contentDescription = "Next Reel", tint = Color.White)
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        // TIKTOK RIGHT ACTION COLUMN
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // SME Merchant Avatar with + Follow Badge
            Box(contentAlignment = Alignment.BottomCenter) {
                Surface(
                    shape = CircleShape,
                    color = MpesaGreen,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(currentPost.merchantName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MpesaGreen,
                    modifier = Modifier
                        .offset(y = 6.dp)
                        .size(18.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Follow", tint = Color.White, modifier = Modifier.padding(2.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            // Like Heart Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onLikeClick(currentPost) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        if (currentPost.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (currentPost.isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text("${currentPost.likesCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Comment Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onCommentClick(currentPost) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Comments", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Text("${getCommentsTotal(currentPost.comments)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Share Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onShareClick(currentPost) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Reply, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Text("${currentPost.sharesCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // M-Pesa Order Quick Buy Action (if product attached)
            if (currentPost.productName != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MpesaGreen,
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { onOrderClick(currentPost) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Order Now", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Text("Order", color = MpesaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Rotating Vinyl Sound Disc
            Surface(
                shape = CircleShape,
                color = Color.DarkGray,
                border = androidx.compose.foundation.BorderStroke(3.dp, Color.LightGray.copy(alpha = 0.5f)),
                modifier = Modifier
                    .size(42.dp)
                    .rotate(if (isPlaying) rotation else 0f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // TIKTOK BOTTOM-LEFT CONTENT OVERLAY
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Author Name & Verified Badge & Category
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("@${currentPost.merchantName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (currentPost.isVerified) {
                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = TelegramBlue, modifier = Modifier.size(16.dp))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MpesaGreen.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.6f))
                ) {
                    Text(currentPost.merchantCategory, color = MpesaGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            // Post Caption
            Text(
                currentPost.contentText,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Hashtags
            if (currentPost.hashtags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    currentPost.hashtags.forEach { tag ->
                        Text(tag, color = TelegramBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Featured Product Tag Pill (Clickable to buy)
            if (currentPost.productName != null && currentPost.productPrice != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen),
                    modifier = Modifier
                        .clickable { onOrderClick(currentPost) }
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = CircleShape, color = MpesaGreen) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp).size(14.dp))
                        }
                        Column {
                            Text(currentPost.productName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(viewModel.formatPrice(currentPost.productPrice), color = MpesaGreen, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Rolling Sound Marquee
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(
                    currentPost.soundTrack,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // VIDEO PROGRESS BAR AT VERY BOTTOM
        LinearProgressIndicator(
            progress = { if (isPlaying) progressAnim else 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = MpesaGreen,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun SearchInteractiveGoogleMapView(
    stores: List<NearbyStore>,
    selectedStore: NearbyStore?,
    onSelectStore: (NearbyStore?) -> Unit,
    isSatelliteView: Boolean,
    onToggleSatellite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mapType by remember { mutableStateOf("Earth 3D") }

    val animPanX = remember { Animatable(0f) }
    val animPanY = remember { Animatable(0f) }
    val animZoom = remember { Animatable(1.0f) }
    val routePulseAnim = remember { Animatable(0f) }

    val panOffset = Offset(animPanX.value, animPanY.value)
    val zoomLevel = animZoom.value
    val routePulseProgress = routePulseAnim.value

    // Open Google Earth Fullscreen Helper
    val openGoogleEarthFullScreen = {
        val storeQuery = selectedStore?.let { "${it.name}, ${it.address}" } ?: "Nairobi Kenya SMEs"
        val earthUri = Uri.parse("https://earth.google.com/web/search/${Uri.encode(storeQuery)}")
        val intent = Intent(Intent.ACTION_VIEW, earthUri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://earth.google.com/web/")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    // Smooth camera pan & zoom focus when an SME store is clicked, with SLOW ONE-TIME non-repeating route animation
    LaunchedEffect(selectedStore?.id) {
        routePulseAnim.snapTo(0f)
        selectedStore?.let { store ->
            val targetPanX = -store.lngOffset * 450f
            val targetPanY = -store.latOffset * 450f
            val targetZoom = 1.4f

            coroutineScope.launch {
                animPanX.animateTo(targetPanX, tween(900, easing = FastOutSlowInEasing))
            }
            coroutineScope.launch {
                animPanY.animateTo(targetPanY, tween(900, easing = FastOutSlowInEasing))
            }
            coroutineScope.launch {
                animZoom.animateTo(targetZoom, tween(900, easing = FastOutSlowInEasing))
            }
            // Slow, one-time animation (3000ms duration, non-repeating)
            coroutineScope.launch {
                routePulseAnim.animateTo(1.0f, tween(3000, easing = LinearOutSlowInEasing))
            }
        } ?: run {
            // When route cleared, return camera to center overview
            coroutineScope.launch {
                animPanX.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
                animPanY.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
                animZoom.animateTo(1.0f, tween(600, easing = FastOutSlowInEasing))
                routePulseAnim.animateTo(1.0f, tween(2000, easing = LinearOutSlowInEasing))
            }
        }
    }

    GlassCard(
        borderColor = MpesaGreen.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Authentic Vanilla Google Maps Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (mapType == "Satellite") Color(0xFF1B263B) else Color(0xFFF4F3F0))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            coroutineScope.launch {
                                animPanX.snapTo(animPanX.value + pan.x)
                                animPanY.snapTo(animPanY.value + pan.y)
                                animZoom.snapTo((animZoom.value * zoom).coerceIn(0.5f, 2.5f))
                            }
                        }
                    }
            ) {
                // Interactive Vanilla Google Maps Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val panX = panOffset.x
                    val panY = panOffset.y

                    // 1. Base Ground Surface (Standard Warm Beige Land vs Satellite Dark)
                    val baseLandColor = if (mapType == "Satellite") Color(0xFF1B263B) else Color(0xFFF4F3F0)
                    drawRect(color = baseLandColor)

                    if (mapType != "Satellite") {
                        // 2. City Parks / Green Spaces (Google Maps Pastel Green #C8FACC)
                        val parkPath = Path().apply {
                            moveTo(w * 0.55f + panX * 0.4f, 0f)
                            quadraticTo(w * 0.85f + panX * 0.4f, h * 0.25f + panY * 0.4f, w, h * 0.35f + panY * 0.4f)
                            lineTo(w, 0f)
                            close()
                        }
                        drawPath(parkPath, color = Color(0xFFC8FACC))

                        val parkPath2 = Path().apply {
                            moveTo(0f, h * 0.55f + panY * 0.3f)
                            quadraticTo(w * 0.25f + panX * 0.3f, h * 0.65f + panY * 0.3f, w * 0.15f + panX * 0.3f, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(parkPath2, color = Color(0xFFD2F1D2))

                        // 3. Water Body / River (Google Maps Light Sky Blue #AADAFF)
                        val waterPath = Path().apply {
                            moveTo(0f, h * 0.20f + panY * 0.5f)
                            quadraticTo(w * 0.50f + panX * 0.5f, h * 0.40f + panY * 0.5f, w, h * 0.18f + panY * 0.5f)
                        }
                        drawPath(waterPath, color = Color(0xFFAADAFF), style = Stroke(width = 16.dp.toPx() * zoomLevel))
                    }

                    // 4. Roads & Highways (Vanilla Google Maps Colors)
                    // Yellow Highways (#FFDA66), White Secondary Roads (#FFFFFF), Grey local grid (#E6E6E6)
                    val highwayColor = if (mapType == "Satellite") Color(0xFFF59E0B) else Color(0xFFFFDA66)
                    val highwayBorderColor = if (mapType == "Satellite") Color(0xFFB45309) else Color(0xFFE0B030)
                    val roadColor = if (mapType == "Satellite") Color(0xFF475569) else Color.White
                    val roadBorderColor = if (mapType == "Satellite") Color(0xFF1E293B) else Color(0xFFDCDCDC)

                    val highwayWidth = 12.dp.toPx() * zoomLevel
                    val roadWidth = 8.dp.toPx() * zoomLevel

                    // Highway 1 (Horizontal Main Expressway)
                    val h1Start = Offset(0f, h * 0.32f + panY * 0.5f)
                    val h1End = Offset(w, h * 0.38f + panY * 0.5f)
                    drawLine(highwayBorderColor, h1Start, h1End, strokeWidth = highwayWidth + 2.dp.toPx())
                    drawLine(highwayColor, h1Start, h1End, strokeWidth = highwayWidth)

                    // Highway 2 (Vertical Avenue)
                    val h2Start = Offset(w * 0.38f + panX * 0.5f, 0f)
                    val h2End = Offset(w * 0.44f + panX * 0.5f, h)
                    drawLine(highwayBorderColor, h2Start, h2End, strokeWidth = highwayWidth + 2.dp.toPx())
                    drawLine(highwayColor, h2Start, h2End, strokeWidth = highwayWidth)

                    // Secondary White Streets
                    val r1Start = Offset(0f, h * 0.68f + panY * 0.5f)
                    val r1End = Offset(w, h * 0.62f + panY * 0.5f)
                    drawLine(roadBorderColor, r1Start, r1End, strokeWidth = roadWidth + 2.dp.toPx())
                    drawLine(roadColor, r1Start, r1End, strokeWidth = roadWidth)

                    val r2Start = Offset(w * 0.72f + panX * 0.5f, 0f)
                    val r2End = Offset(w * 0.68f + panX * 0.5f, h)
                    drawLine(roadBorderColor, r2Start, r2End, strokeWidth = roadWidth + 2.dp.toPx())
                    drawLine(roadColor, r2Start, r2End, strokeWidth = roadWidth)

                    // 5. User Current Location ("My Location" - Standard Google Blue Dot)
                    val userX = w * 0.22f + panX * 0.8f
                    val userY = h * 0.78f + panY * 0.8f

                    // Translucent Blue Pulse Halo (#4285F4)
                    drawCircle(
                        color = Color(0xFF4285F4).copy(alpha = 0.22f),
                        radius = (18.dp.toPx() + routePulseProgress * 14.dp.toPx()),
                        center = Offset(userX, userY)
                    )
                    // Core Google Maps Blue Dot with White Ring
                    drawCircle(
                        color = Color.White,
                        radius = 9.dp.toPx(),
                        center = Offset(userX, userY)
                    )
                    drawCircle(
                        color = Color(0xFF4285F4),
                        radius = 6.dp.toPx(),
                        center = Offset(userX, userY)
                    )

                    // 6. DIRECT ROUTE NAVIGATION LINE (Official Google Maps Navigation Blue #4285F4)
                    selectedStore?.let { store ->
                        val targetX = (0.5f + (store.lngOffset + panX / 600f) * zoomLevel).coerceIn(0.12f, 0.88f) * w
                        val targetY = (0.5f + (store.latOffset + panY / 600f) * zoomLevel).coerceIn(0.24f, 0.88f) * h

                        val midX = (userX + targetX) / 2f + 25.dp.toPx()
                        val midY = (userY + targetY) / 2f - 20.dp.toPx()

                        val directPath = Path().apply {
                            moveTo(userX, userY)
                            quadraticTo(midX, midY, targetX, targetY)
                        }

                        // Outer Navigation Line Border (#1557B0)
                        drawPath(
                            path = directPath,
                            color = Color(0xFF1557B0),
                            style = Stroke(width = 9.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Main Vibrant Navigation Line (#4285F4)
                        drawPath(
                            path = directPath,
                            color = Color(0xFF4285F4),
                            style = Stroke(width = 5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Animated Moving Pulse Point along the navigation path
                        val animPointX = userX + (targetX - userX) * routePulseProgress
                        val animPointY = userY + (targetY - userY) * routePulseProgress
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(animPointX, animPointY)
                        )
                        drawCircle(
                            color = Color(0xFF1557B0),
                            radius = 8.dp.toPx(),
                            center = Offset(animPointX, animPointY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // 7. User Origin Badge (Clean Google White Card)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(shape = CircleShape, color = Color(0xFF4285F4), modifier = Modifier.size(8.dp)) {}
                            Text("Your Location", color = Color(0xFF202124), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 8. Interactive Business Store Pins Overlay (Vanilla Google Maps Red Pins #EA4335)
                stores.forEach { store ->
                    val isSelected = selectedStore?.id == store.id

                    // CRITICAL UNCLUTTERING RULE:
                    // If a store is selected, hide other non-selected stores completely so the map focuses on the route!
                    if (selectedStore != null && !isSelected) {
                        return@forEach
                    }

                    val xRatio = (0.5f + (store.lngOffset + panOffset.x / 600f) * zoomLevel).coerceIn(0.08f, 0.92f)
                    val yRatio = (0.5f + (store.latOffset + panOffset.y / 600f) * zoomLevel).coerceIn(0.24f, 0.88f)

                    Box(
                        modifier = Modifier
                            .align(
                                androidx.compose.ui.BiasAlignment(
                                    (xRatio - 0.5f) * 2,
                                    (yRatio - 0.5f) * 2
                                )
                            )
                            .clickable { onSelectStore(store) }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (store.promoBadge != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEA4335),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = store.promoBadge,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Classic Google Maps Red Store Pin Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFD93025) else Color(0xFFEA4335),
                                shadowElevation = if (isSelected) 8.dp else 4.dp,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier.scale(if (isSelected) 1.25f else 1.0f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (store.isOpenNow) Color(0xFF34A853) else Color(0xFFEA4335),
                                        modifier = Modifier.size(7.dp)
                                    ) {}

                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(
                                        store.name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            "★${store.rating}",
                                            color = Color(0xFFFBBC04),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            // Pin Pointer Needle
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = store.name,
                                tint = if (isSelected) Color(0xFFD93025) else Color(0xFFEA4335),
                                modifier = Modifier
                                    .size(22.dp)
                                    .offset(y = (-5).dp)
                            )
                        }
                    }
                }

                // 9. Header Guidance Badge (Clean Google Maps Style White Card)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedStore != null) Icons.Default.DirectionsRun else Icons.Default.Place,
                            contentDescription = null,
                            tint = if (selectedStore != null) Color(0xFF1A73E8) else Color(0xFFEA4335),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (selectedStore != null)
                                "Route to ${selectedStore.name} (${selectedStore.distanceKm} km)"
                            else
                                "Google Maps • Tap store to navigate",
                            color = Color(0xFF202124),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 10. Top-Right Map Mode Selector ("Map" vs "Satellite") & Earth Fullscreen
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Google Earth Fullscreen Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1A73E8),
                        shadowElevation = 4.dp,
                        modifier = Modifier.clickable { openGoogleEarthFullScreen() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Public, contentDescription = "Earth Fullscreen", tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("Earth Fullscreen", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            listOf("Map", "Satellite").forEach { type ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if ((mapType == "Satellite" && type == "Satellite") || (mapType != "Satellite" && type == "Map")) Color(0xFF1A73E8) else Color.Transparent,
                                    modifier = Modifier.clickable { mapType = if (type == "Satellite") "Satellite" else "Default" }
                                ) {
                                    Text(
                                        type,
                                        color = if ((mapType == "Satellite" && type == "Satellite") || (mapType != "Satellite" && type == "Map")) Color.White else Color(0xFF5F6368),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Clear Route Button
                    if (selectedStore != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onSelectStore(null) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Route", tint = Color(0xFF5F6368), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // 11. Google Maps Style Compass Widget (North Dial)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp, bottom = 10.dp)
                        .size(38.dp)
                        .clickable {
                            coroutineScope.launch {
                                animPanX.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
                                animPanY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
                                animZoom.animateTo(1.0f, tween(500, easing = FastOutSlowInEasing))
                            }
                            Toast.makeText(context, "🧭 Compass aligned to North", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("N", color = Color(0xFFEA4335), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = "Compass North Alignment",
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 9. Direct Navigation & Route Info Card under Map
            selectedStore?.let { store ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MpesaGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Directions, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                Column {
                                    Text("⚡ Direct Route to ${store.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("⏱️ ~3 mins walk (${store.distanceKm} km away)", fontSize = 11.sp, color = MpesaGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Clear Route Button
                            IconButton(
                                onClick = { onSelectStore(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Route", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Direct Turn-By-Turn Path Guidance Text
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(14.dp))
                                Text(
                                    "Path: Head East via CBD Plaza -> Turn right at Kimathi St -> Arrive at ${store.name}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Real-Time Promotion Highlight Banner
                        if (store.currentPromotion != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDC2626).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🔥 LIVE DEAL:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                                    Text(store.currentPromotion, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val uri = Uri.parse("geo:${store.latitude},${store.longitude}?q=${Uri.encode(store.name + ", " + store.address)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(store.name + " " + store.address)}"))
                                            context.startActivity(webIntent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Start Navigation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=${store.phone}&text=${Uri.encode("Hello ${store.name}, I am navigating to your store via BizPilot Maps for '${store.currentPromotion ?: "Store Products"}'!")}")
                                        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                                        context.startActivity(intent)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp, color = MpesaGreen)
                                }
                            }

                            // Fullscreen Earth.Google Direct Action Button
                            Button(
                                onClick = { openGoogleEarthFullScreen() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open earth.google Fullscreen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } ?: run {
                // Guidance banner when no store is selected
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(18.dp))
                        Text(
                            "Select any SME store pin or list item above to immediately highlight its direct path & distance route.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmeSearchEngineDialog(
    initialQuery: String = "",
    viewModel: BizPilotViewModel,
    posts: List<BusinessPost>,
    stores: List<NearbyStore>,
    onDismiss: () -> Unit,
    onNavigateToStoreMap: (NearbyStore) -> Unit,
    onOrderProduct: (BusinessPost) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf(initialQuery) }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Stores & GPS, 2: Satellite View, 3: Hot Deals
    var isSatelliteView by remember { mutableStateOf(true) }
    var selectedStoreOnMap by remember { mutableStateOf<NearbyStore?>(stores.firstOrNull()) }

    val matchingStores = remember(query, stores) {
        if (query.isBlank()) stores else stores.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    val matchingPosts = remember(query, posts) {
        if (query.isBlank()) posts else posts.filter {
            it.contentText.contains(query, ignoreCase = true) ||
                    it.merchantName.contains(query, ignoreCase = true) ||
                    it.merchantCategory.contains(query, ignoreCase = true) ||
                    (it.productName != null && it.productName.contains(query, ignoreCase = true))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Title & Close Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MpesaGreen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TravelExplore, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("SME Search Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Interactive Google Maps • Satellite HD • Live GPS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Full Search Engine Input Bar
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search stores, GPS locations, electronics, produce, solar...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MpesaGreen) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_engine_dialog_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MpesaGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Search Engine Filter Tabs
                TabRow(selectedTabIndex = selectedFilterTab) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = { Text("🔍 All (${matchingStores.size + matchingPosts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = { Text("📍 GPS Stores (${matchingStores.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedFilterTab == 2,
                        onClick = { selectedFilterTab = 2 },
                        text = { Text("🛰️ Interactive Google Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedFilterTab == 3,
                        onClick = { selectedFilterTab = 3 },
                        text = { Text("🏷️ Deals (${matchingPosts.filter { it.productName != null }.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Embedded Interactive Google Maps View inside Search Interface
                if (selectedFilterTab == 2 || (selectedFilterTab == 0 && matchingStores.isNotEmpty())) {
                    SearchInteractiveGoogleMapView(
                        stores = matchingStores,
                        selectedStore = selectedStoreOnMap,
                        onSelectStore = { selectedStoreOnMap = it },
                        isSatelliteView = isSatelliteView,
                        onToggleSatellite = { isSatelliteView = !isSatelliteView },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Search Results List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // STORES & MAP SATELLITE SECTION
                    if (selectedFilterTab == 0 || selectedFilterTab == 1) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📍 Verified Merchant Stores Near You", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { isSatelliteView = !isSatelliteView }
                                ) {
                                    Icon(
                                        imageVector = if (isSatelliteView) Icons.Default.Satellite else Icons.Default.Map,
                                        contentDescription = null,
                                        tint = MpesaGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (isSatelliteView) "Satellite: ON" else "Standard Map",
                                        fontSize = 11.sp,
                                        color = MpesaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        items(matchingStores, key = { "store_${it.id}" }) { store ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStoreOnMap = store }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MpesaGreen,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(store.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    if (store.isVerified) {
                                                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = TelegramBlue, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                                Text("${store.category} • ${store.distanceKm} km away", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SecondaryAmber.copy(alpha = 0.2f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(12.dp))
                                                Text("${store.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryAmber)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Text(store.description, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)

                                    Spacer(Modifier.height(8.dp))

                                    // Turn-by-Turn GPS Directions Card
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSatelliteView) Color.DarkGray.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSatelliteView) Icons.Default.SatelliteAlt else Icons.Default.Navigation,
                                                contentDescription = null,
                                                tint = if (isSatelliteView) Color.Cyan else MpesaGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    if (isSatelliteView) "🛰️ Satellite HD Pin: ${store.address}" else "📍 GPS Route: Head towards ${store.address}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSatelliteView) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    "Turn-by-turn navigation active • ETA ~ 6 mins",
                                                    fontSize = 10.sp,
                                                    color = if (isSatelliteView) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Direct Launch Actions: Google Maps App, WhatsApp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val uri = Uri.parse("geo:${store.latitude},${store.longitude}?q=${Uri.encode(store.name + ", " + store.address)}")
                                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                    setPackage("com.google.android.apps.maps")
                                                }
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(store.name + " " + store.address)}"))
                                                    context.startActivity(webIntent)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=${store.phone}&text=${Uri.encode("Hello ${store.name}, I found your store on BizPilot Search! Is your store currently open?")}")
                                                val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                                                context.startActivity(intent)
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 11.sp, color = MpesaGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // POSTS & PRODUCTS SECTION
                    if (selectedFilterTab == 0 || selectedFilterTab == 3) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("📦 Available Product Offers & Merchant Posts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }

                        items(matchingPosts, key = { "post_${it.id}" }) { post ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(post.merchantName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MpesaGreen)
                                        Text(post.locationName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(post.contentText, fontSize = 12.sp, maxLines = 2)

                                    if (post.productName != null && post.productPrice != null) {
                                        Spacer(Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MpesaGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    "📦 ${post.productName} • ${viewModel.formatPrice(post.productPrice)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MpesaGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Button(
                                                onClick = { onOrderProduct(post) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Order Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialSettingsScreenView(
    viewModel: BizPilotViewModel,
    onOpenSetupWizard: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onSwitchAppMode: (String) -> Unit
) {
    val context = LocalContext.current
    val merchantName by viewModel.userName.collectAsState()
    val merchantCategory by viewModel.businessCategory.collectAsState()
    val merchantBio by viewModel.businessBio.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncItemsCount.collectAsState()
    val isSyncing by viewModel.isOfflineSyncActive.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MpesaGreen,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Column {
                Text("App & SME Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Manage your account, theme, currency & setup wizard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Profile Card Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MpesaGreen)
                        Text("SME Profile Details", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onOpenEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MpesaGreen)
                        Spacer(Modifier.width(4.dp))
                        Text("Edit Profile", color = MpesaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Merchant Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(merchantName.ifBlank { "Safari Retail Mart" }, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Category:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(merchantCategory, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MpesaGreen)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Role Permissions:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (isLoggedIn) userRole else "Public Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Setup Wizard Card Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MpesaGreen.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("App Installation & Setup Wizard 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Re-configure app usage (Business vs Customer mode) or reset business details.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onOpenSetupWizard,
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Run Setup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Theme Customization
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🎨 Visual Theme", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = themeMode == AppThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                        label = { Text("Light Mode") },
                        leadingIcon = { Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == AppThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                        label = { Text("Dark Mode") },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == AppThemeMode.TELEGRAM_MINI_APP,
                        onClick = { viewModel.setThemeMode(AppThemeMode.TELEGRAM_MINI_APP) },
                        label = { Text("Telegram") },
                        leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Multi-Currency Customization
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("💱 Currency Selector", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("KES", "USD", "NGN", "GHS", "UGX").forEach { curr ->
                        FilterChip(
                            selected = currency == curr,
                            onClick = { viewModel.setCurrency(curr) },
                            label = { Text(curr, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MpesaGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Cloud & Offline Sync
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("☁️ Firestore Cloud Sync", fontWeight = FontWeight.Bold)
                    Text(if (isSyncing) "Syncing..." else "Pending: $pendingSyncCount items", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = {
                        viewModel.syncNow { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Trigger Manual Sync Now")
                }
            }
        }

        // Mode Switching & Logout
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("💼 App Access Mode", fontWeight = FontWeight.Bold)

                Button(
                    onClick = { onSwitchAppMode("BUSINESS") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enter Business OS (Authorized)")
                }

                if (isLoggedIn) {
                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log Out of Business Profile")
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}


