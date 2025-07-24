package io.yavero.almasasuite.pos.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.model.AuthenticatedUser
import io.yavero.almasasuite.model.UserRole
import io.yavero.almasasuite.pos.localization.LocalLocalizationManager
import io.yavero.almasasuite.pos.localization.LocalizationManager
import io.yavero.almasasuite.pos.localization.StringResources
import io.yavero.almasasuite.pos.localization.getString
import io.yavero.almasasuite.pos.service.SalesSubmissionResult
import io.yavero.almasasuite.pos.service.SalesSubmissionService
import io.yavero.almasasuite.pos.ui.components.CompactLanguageSwitcher
import io.yavero.almasasuite.pos.ui.inventory.InventoryScreen
import io.yavero.almasasuite.pos.ui.purchases.PurchasesScreen
import io.yavero.almasasuite.pos.ui.reports.DailySummaryScreen
import io.yavero.almasasuite.pos.ui.sales.DailySalesLoggingScreen
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedApp(
    user: AuthenticatedUser,
    onLogout: () -> Unit
) {
    val localizationManager = remember { LocalizationManager() }

    CompositionLocalProvider(LocalLocalizationManager provides localizationManager) {
        val currentLanguage by localizationManager.currentLanguage.collectAsState()
        val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            AuthenticatedAppContent(user = user, onLogout = onLogout)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedAppContent(
    user: AuthenticatedUser,
    onLogout: () -> Unit
) {
    val focusManager = LocalFocusManager.current


    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF2196F3),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFFBB86FC),
            surface = Color(0xFF1E1E1E),
            background = Color(0xFF121212),
            error = Color(0xFFCF6679),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onSurface = Color(0xFFE1E1E1),
            onBackground = Color(0xFFE1E1E1)
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->

                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when {

                            keyEvent.isCtrlPressed && keyEvent.key == Key.F -> {


                                false
                            }

                            keyEvent.key == Key.Escape -> {
                                focusManager.clearFocus()
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Column {

                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Surface(
                                color = when (user.role) {
                                    UserRole.ADMIN -> MaterialTheme.colorScheme.error
                                    UserRole.MANAGER -> MaterialTheme.colorScheme.primary
                                    UserRole.STAFF -> MaterialTheme.colorScheme.secondary
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${user.name} (${user.role.name})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    },
                    actions = {

                        CompactLanguageSwitcher()

                        Spacer(modifier = Modifier.width(8.dp))


                        var showLogoutConfirm by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showLogoutConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = getString(StringResources.LOGOUT))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(getString(StringResources.LOGOUT))
                        }

                        Spacer(modifier = Modifier.width(16.dp))


                        if (showLogoutConfirm) {
                            AlertDialog(
                                onDismissRequest = { showLogoutConfirm = false },
                                title = { Text(getString(StringResources.CONFIRM_LOGOUT)) },
                                text = { Text(getString(StringResources.CONFIRM_LOGOUT_MESSAGE)) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showLogoutConfirm = false
                                            onLogout()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(getString(StringResources.LOGOUT))
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showLogoutConfirm = false }
                                    ) {
                                        Text(getString(StringResources.CANCEL))
                                    }
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )


                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (user.role) {
                        UserRole.STAFF -> StaffInterface(user)
                        UserRole.MANAGER -> ManagerInterface(user)
                        UserRole.ADMIN -> AdminInterface(user)
                    }
                }
            }
        }
    }
}


@Composable
private fun StaffInterface(user: AuthenticatedUser) {
    var currentScreen by remember { mutableStateOf(StaffScreen.DAILY_SALES) }
    val salesService = remember { SalesSubmissionService() }
    val coroutineScope = rememberCoroutineScope()


    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TabRow(
            selectedTabIndex = currentScreen.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = currentScreen == StaffScreen.DAILY_SALES,
                onClick = { currentScreen = StaffScreen.DAILY_SALES },
                text = { Text(getString(StringResources.DAILY_SALES_LOGGING)) },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = getString(StringResources.DAILY_SALES)) }
            )
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (currentScreen) {
                StaffScreen.DAILY_SALES -> {
                    DailySalesLoggingScreen(
                        user = user,
                        onSubmitSales = { salesLog ->
                            coroutineScope.launch {
                                when (val result = salesService.submitSales(salesLog)) {
                                    is SalesSubmissionResult.Success -> {
                                        successMessage = "Sales submitted successfully! Sale ID: ${result.saleResponse.id}"
                                        errorMessage = null
                                    }
                                    is SalesSubmissionResult.Error -> {
                                        errorMessage = "Sales submission failed: ${result.message}"
                                        successMessage = null
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }


    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            kotlinx.coroutines.delay(4000)
            successMessage = null
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }


    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Sales Submission Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
private fun ManagerInterface(user: AuthenticatedUser) {
    var currentScreen by remember { mutableStateOf(ManagerScreen.INVENTORY) }
    val salesService = remember { SalesSubmissionService() }
    val coroutineScope = rememberCoroutineScope()


    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TabRow(
            selectedTabIndex = currentScreen.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = currentScreen == ManagerScreen.INVENTORY,
                onClick = { currentScreen = ManagerScreen.INVENTORY },
                text = { Text(getString(StringResources.INVENTORY)) },
                icon = { Icon(Icons.Default.Inventory, contentDescription = getString(StringResources.INVENTORY)) }
            )
            Tab(
                selected = currentScreen == ManagerScreen.DAILY_SALES,
                onClick = { currentScreen = ManagerScreen.DAILY_SALES },
                text = { Text(getString(StringResources.DAILY_SALES)) },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = getString(StringResources.DAILY_SALES)) }
            )
            Tab(
                selected = currentScreen == ManagerScreen.PURCHASES,
                onClick = { currentScreen = ManagerScreen.PURCHASES },
                text = { Text(getString(StringResources.PURCHASES)) },
                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = getString(StringResources.PURCHASES)) }
            )
            Tab(
                selected = currentScreen == ManagerScreen.REPORTS,
                onClick = { currentScreen = ManagerScreen.REPORTS },
                text = { Text(getString(StringResources.REPORTS)) },
                icon = { Icon(Icons.Default.Assessment, contentDescription = getString(StringResources.REPORTS)) }
            )
        }


        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                ManagerScreen.INVENTORY -> {
                    InventoryScreen(user)
                }
                ManagerScreen.DAILY_SALES -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DailySalesLoggingScreen(
                            user = user,
                            onSubmitSales = { salesLog ->
                                coroutineScope.launch {
                                    when (val result = salesService.submitSales(salesLog)) {
                                        is SalesSubmissionResult.Success -> {
                                            successMessage = "Sales submitted successfully! Sale ID: ${result.saleResponse.id}"
                                            errorMessage = null
                                        }
                                        is SalesSubmissionResult.Error -> {
                                            errorMessage = "Sales submission failed: ${result.message}"
                                            successMessage = null
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                ManagerScreen.PURCHASES -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        PurchasesScreen(user = user)
                    }
                }
                ManagerScreen.REPORTS -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DailySummaryScreen(
                            user = user,
                            onExportCsv = { report ->

                            println("Exporting daily summary report to CSV...")

                            },
                            onPrintReport = { report ->

                            println("Printing daily summary report...")

                            }
                        )
                    }
                }
            }
        }
    }


    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            kotlinx.coroutines.delay(4000)
            successMessage = null
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }


    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Sales Submission Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
private fun AdminInterface(user: AuthenticatedUser) {
    var currentScreen by remember { mutableStateOf(AdminScreen.INVENTORY) }
    val salesService = remember { SalesSubmissionService() }
    val coroutineScope = rememberCoroutineScope()


    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TabRow(
            selectedTabIndex = currentScreen.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = currentScreen == AdminScreen.INVENTORY,
                onClick = { currentScreen = AdminScreen.INVENTORY },
                text = { Text(getString(StringResources.INVENTORY)) },
                icon = { Icon(Icons.Default.Inventory, contentDescription = getString(StringResources.INVENTORY)) }
            )
            Tab(
                selected = currentScreen == AdminScreen.DAILY_SALES,
                onClick = { currentScreen = AdminScreen.DAILY_SALES },
                text = { Text(getString(StringResources.DAILY_SALES)) },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = getString(StringResources.DAILY_SALES)) }
            )
            Tab(
                selected = currentScreen == AdminScreen.PURCHASES,
                onClick = { currentScreen = AdminScreen.PURCHASES },
                text = { Text(getString(StringResources.PURCHASES)) },
                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = getString(StringResources.PURCHASES)) }
            )
            Tab(
                selected = currentScreen == AdminScreen.REPORTS,
                onClick = { currentScreen = AdminScreen.REPORTS },
                text = { Text(getString(StringResources.REPORTS)) },
                icon = { Icon(Icons.Default.Assessment, contentDescription = getString(StringResources.REPORTS)) }
            )
        }


        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                AdminScreen.INVENTORY -> {
                    InventoryScreen(user)
                }
                AdminScreen.DAILY_SALES -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DailySalesLoggingScreen(
                            user = user,
                            onSubmitSales = { salesLog ->
                                coroutineScope.launch {
                                    when (val result = salesService.submitSales(salesLog)) {
                                        is SalesSubmissionResult.Success -> {
                                            successMessage = "Sales submitted successfully! Sale ID: ${result.saleResponse.id}"
                                            errorMessage = null
                                        }
                                        is SalesSubmissionResult.Error -> {
                                            errorMessage = "Sales submission failed: ${result.message}"
                                            successMessage = null
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                AdminScreen.PURCHASES -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        PurchasesScreen(user = user)
                    }
                }
                AdminScreen.REPORTS -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DailySummaryScreen(
                            user = user,
                            onExportCsv = { report ->

                            println("Exporting daily summary report to CSV...")

                            },
                            onPrintReport = { report ->

                            println("Printing daily summary report...")

                            }
                        )
                    }
                }
            }
        }
    }


    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            kotlinx.coroutines.delay(4000)
            successMessage = null
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }


    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Sales Submission Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}


enum class StaffScreen {
    DAILY_SALES
}

enum class ManagerScreen {
    INVENTORY, DAILY_SALES, PURCHASES, REPORTS
}

enum class AdminScreen {
    INVENTORY, DAILY_SALES, PURCHASES, REPORTS
}