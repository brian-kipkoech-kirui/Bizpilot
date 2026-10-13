package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel

@Composable
fun AppInstallationSetupWizard(
    viewModel: BizPilotViewModel,
    onSetupFinished: (selectedMode: String) -> Unit
) {
    val context = LocalContext.current

    // Step state: 0 = Choose Type (Customer vs Business), 1 = Setup Details, 2 = Staff Setup (if Business)
    var currentStep by remember { mutableStateOf(0) }
    var selectedUserType by remember { mutableStateOf("CUSTOMER") } // "CUSTOMER" or "BUSINESS"

    // Customer Setup Questions State
    var customerNameInput by remember { mutableStateOf("Alex Mwangi") }
    var customerInterestsInput by remember { mutableStateOf("Solar, Electronics & Hardware Deals") }
    var customerLocationInput by remember { mutableStateOf("Nairobi, Kenya") }

    // Business Setup Questions State
    var businessNameInput by remember { mutableStateOf("Safari Retail Mart") }
    var businessCategoryInput by remember { mutableStateOf("Solar & Consumer Electronics") }
    var businessRoleInput by remember { mutableStateOf("Owner") }

    // Staff Users Setup State (if Business)
    var staffNameInput by remember { mutableStateOf("") }
    var staffRoleInput by remember { mutableStateOf("Cashier") }
    var staffPinInput by remember { mutableStateOf("") }
    var createdStaffList by remember {
        mutableStateOf(
            listOf(
                Triple("Jane Wambui", "Cashier", "1234"),
                Triple("Peter Ochieng", "Accountant", "5678")
            )
        )
    }

    Dialog(
        onDismissRequest = {
            // Default option is customer when skipped
            viewModel.skipSetupDefaultAsCustomer()
            onSetupFinished("SOCIAL")
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column {
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
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SettingsSuggest,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "BizPilot Setup Wizard",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Installation & User Configuration",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Skip Setup Action (Defaults to Customer Mode)
                        TextButton(
                            onClick = {
                                viewModel.skipSetupDefaultAsCustomer()
                                Toast.makeText(context, "Setup skipped: Set as Customer Mode (Default)", Toast.LENGTH_SHORT).show()
                                onSetupFinished("SOCIAL")
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Skip", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))
                }

                // Body Content based on Step
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (currentStep) {
                        0 -> {
                            // Step 0: Choose Usage Type (Customer vs Business)
                            Text(
                                text = "How will you be using BizPilot?",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Select your account type to configure your app environment. Skipping defaults to Customer mode.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(4.dp))

                            // Customer Option Card (Default)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedUserType == "CUSTOMER") MpesaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    width = if (selectedUserType == "CUSTOMER") 2.dp else 1.dp,
                                    color = if (selectedUserType == "CUSTOMER") MpesaGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUserType = "CUSTOMER" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MpesaGreen,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("As a Customer / Buyer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MpesaGreen.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "DEFAULT",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MpesaGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Access Biz Social Hub, explore verified local merchant deals, chat, and place retail orders.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    RadioButton(
                                        selected = selectedUserType == "CUSTOMER",
                                        onClick = { selectedUserType = "CUSTOMER" },
                                        colors = RadioButtonDefaults.colors(selectedColor = MpesaGreen)
                                    )
                                }
                            }

                            // Business Option Card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedUserType == "BUSINESS") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    width = if (selectedUserType == "BUSINESS") 2.dp else 1.dp,
                                    color = if (selectedUserType == "BUSINESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUserType = "BUSINESS" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("For Business / Merchant OS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "AUTH REQUIRED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Manage POS till sales, inventory ERP, staff access PINs, M-Pesa payments, and AI Copilot.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    RadioButton(
                                        selected = selectedUserType == "BUSINESS",
                                        onClick = { selectedUserType = "BUSINESS" },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }

                        1 -> {
                            // Step 1: Relevant Questions based on Selected Type
                            if (selectedUserType == "CUSTOMER") {
                                Text(
                                    text = "Customer Profile Questions",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Help us personalize your Biz Social experience and deals feed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = customerNameInput,
                                    onValueChange = { customerNameInput = it },
                                    label = { Text("Your Full Name / Buyer Handle") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = customerInterestsInput,
                                    onValueChange = { customerInterestsInput = it },
                                    label = { Text("Shopping Interests & Preferred Products") },
                                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = customerLocationInput,
                                    onValueChange = { customerLocationInput = it },
                                    label = { Text("Primary Location / City") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "Business OS Setup Questions",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Provide your business details to configure POS receipts and store settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = businessNameInput,
                                    onValueChange = { businessNameInput = it },
                                    label = { Text("Business / Store Name") },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = businessCategoryInput,
                                    onValueChange = { businessCategoryInput = it },
                                    label = { Text("Industry / Business Category") },
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = "Select Your Primary Role:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf("Owner", "Manager", "Cashier", "Accountant").forEach { role ->
                                        FilterChip(
                                            selected = businessRoleInput == role,
                                            onClick = { businessRoleInput = role },
                                            label = { Text(role, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Step 2: Staff Setup (For Business OS)
                            Text(
                                text = "Setup Staff & Cashier Access PINs",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Add staff members with 4-digit PIN fallback authentication for quick POS counter login.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick Staff Add Form
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Add New Staff User:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = staffNameInput,
                                            onValueChange = { staffNameInput = it },
                                            placeholder = { Text("Staff Name") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = staffPinInput,
                                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) staffPinInput = it },
                                            placeholder = { Text("4-Digit PIN") },
                                            modifier = Modifier.width(100.dp),
                                            singleLine = true
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            if (staffNameInput.isNotBlank() && staffPinInput.length == 4) {
                                                createdStaffList = createdStaffList + Triple(staffNameInput, staffRoleInput, staffPinInput)
                                                staffNameInput = ""
                                                staffPinInput = ""
                                                Toast.makeText(context, "Staff added!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Enter staff name and 4-digit PIN", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add Staff", fontSize = 12.sp)
                                    }
                                }
                            }

                            Text("Configured Staff Users (${createdStaffList.size}):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            createdStaffList.forEach { staff ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text(staff.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Role: ${staff.second} • PIN: ${staff.third}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Surface(shape = RoundedCornerShape(4.dp), color = MpesaGreen.copy(alpha = 0.2f)) {
                                            Text("READY", color = MpesaGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Buttons (Back / Next / Finish)
                Column {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- }
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Back")
                            }
                        } else {
                            // Default option on skip button
                            TextButton(
                                onClick = {
                                    viewModel.skipSetupDefaultAsCustomer()
                                    onSetupFinished("SOCIAL")
                                }
                            ) {
                                Text("Default as Customer", fontSize = 12.sp, color = MpesaGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStep == 0) {
                                    currentStep = 1
                                } else if (currentStep == 1) {
                                    if (selectedUserType == "BUSINESS") {
                                        currentStep = 2
                                    } else {
                                        // Save Customer setup
                                        viewModel.setupCustomerUser(customerNameInput, customerInterestsInput, customerLocationInput)
                                        Toast.makeText(context, "Customer Profile Created!", Toast.LENGTH_SHORT).show()
                                        onSetupFinished("SOCIAL")
                                    }
                                } else if (currentStep == 2) {
                                    // Save Business setup
                                    viewModel.setupBusinessUser(businessNameInput, businessCategoryInput, businessRoleInput)
                                    Toast.makeText(context, "Business OS Configured with ${createdStaffList.size} Staff Users!", Toast.LENGTH_SHORT).show()
                                    onSetupFinished("BUSINESS")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedUserType == "CUSTOMER") MpesaGreen else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            val nextText = when {
                                currentStep == 0 -> "Next Setup Step"
                                currentStep == 1 && selectedUserType == "BUSINESS" -> "Next: Staff Setup"
                                else -> "Complete Setup & Launch"
                            }
                            Text(nextText, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
