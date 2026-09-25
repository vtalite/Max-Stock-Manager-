package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DatabaseManager
import com.example.data.WineItem
import com.example.ui.components.AddEditWineDialog
import com.example.ui.components.AppReleaseReportDialog
import com.example.ui.components.AuditLogsDialog
import com.example.ui.components.CategoryConfigTableDialog
import com.example.ui.components.ChangePasswordDialog
import com.example.ui.components.DatabaseSelectorDialog
import com.example.ui.components.WineDetailSheet
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.BarcodeScannerScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NearExpirationScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.WineBurgundyDark
import com.example.ui.theme.WineBurgundyPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WineViewModel,
    modifier: Modifier = Modifier
) {
    val authSession by viewModel.authSession.collectAsStateWithLifecycle()
    val savedCompanies by viewModel.allCompanies.collectAsStateWithLifecycle()
    val currentDatabaseId by viewModel.currentDatabaseName.collectAsStateWithLifecycle()

    // If not logged in, show Enterprise Login Screen (CNPJ + Employee + Password)
    if (!authSession.isLoggedIn) {
        LoginScreen(
            savedCompanies = savedCompanies,
            currentDatabaseId = currentDatabaseId,
            onSelectDatabase = { newDbId ->
                viewModel.selectDatabase(newDbId)
            },
            onRequestResetCode = { cnpj, identifier, cb ->
                viewModel.requestPasswordReset(cnpj, identifier, cb)
            },
            onConfirmReset = { cnpj, identifier, newPass, cb ->
                viewModel.confirmPasswordReset(cnpj, identifier, newPass, cb)
            },
            onLoginSuccess = { cnpj, compName, user, full, role ->
                viewModel.login(cnpj, compName, user, full, role)
            }
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val wines by viewModel.winesList.collectAsStateWithLifecycle()
    val allDbWines by viewModel.allWines.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsStateWithLifecycle()

    val scanMode by viewModel.scanMode.collectAsStateWithLifecycle()
    val scanFeedback by viewModel.scanFeedback.collectAsStateWithLifecycle()

    val lowStockWines by viewModel.lowStockWines.collectAsStateWithLifecycle()
    val outOfStockWines by viewModel.outOfStockWines.collectAsStateWithLifecycle()
    val nearExpirationWines by viewModel.nearExpirationWines.collectAsStateWithLifecycle()
    val filteredNearExpirationWines by viewModel.filteredNearExpirationWines.collectAsStateWithLifecycle()
    val expirationSearchQuery by viewModel.expirationSearchQuery.collectAsStateWithLifecycle()
    val expirationFilterType by viewModel.expirationFilterType.collectAsStateWithLifecycle()
    val allCategoryConfigs by viewModel.allCategoryConfigs.collectAsStateWithLifecycle()

    // Audit Logs State
    val allLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val filteredLogs by viewModel.filteredAuditLogs.collectAsStateWithLifecycle()
    val logSearchQuery by viewModel.logSearchQuery.collectAsStateWithLifecycle()
    val selectedLogFilter by viewModel.selectedLogActionFilter.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()

    // Dev Change Logs State
    val allDevLogs by viewModel.allDevChangeLogs.collectAsStateWithLifecycle()
    val filteredDevChangeLogs by viewModel.filteredDevChangeLogs.collectAsStateWithLifecycle()
    val devLogSearchQuery by viewModel.devLogSearchQuery.collectAsStateWithLifecycle()
    val selectedDevLogCategoryFilter by viewModel.selectedDevLogCategoryFilter.collectAsStateWithLifecycle()

    val alertCount = lowStockWines.size + outOfStockWines.size + nearExpirationWines.size
    val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()

    val stockChangesCount = remember(allLogs) {
        allLogs.count {
            it.actionType in listOf("ENTRADA_ESTOQUE", "SAIDA_ESTOQUE", "AJUSTE", "EDICAO", "EXCLUSAO", "CADASTRO")
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Dialog & Sheet State
    var showReleaseReportDialog by rememberSaveable {
        mutableStateOf(com.example.data.ReleaseReportManager.shouldShowOnStartup(context))
    }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showUserAccountDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showCategoryConfigDialog by remember { mutableStateOf(false) }
    var showDatabaseSelectorDialog by remember { mutableStateOf(false) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var wineToEdit by remember { mutableStateOf<WineItem?>(null) }
    var prefilledBarcodeForDialog by remember { mutableStateOf("") }

    var showBatchImportDialog by remember { mutableStateOf(false) }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var recentlySavedWine by remember { mutableStateOf<WineItem?>(null) }
    var isRecentSaveEdit by remember { mutableStateOf(false) }

    var wineForDetailSheet by remember { mutableStateOf<WineItem?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.max_bebidas_icon),
                                    contentDescription = "Max Bebidas Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Max Bebidas • Estoque",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        // Active Company & Employee indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "${authSession.employeeName} • ${authSession.companyName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Database Pill Indicator (Clickable to switch database)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatabaseSelectorDialog = true }
                                .testTag("topbar_database_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "BD: ${DatabaseManager.getDatabaseDisplayName(currentDatabaseId)}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Database Selector Icon Button
                    IconButton(
                        onClick = { showDatabaseSelectorDialog = true },
                        modifier = Modifier.testTag("topbar_database_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Selecionar Banco de Dados Ativo",
                            tint = Color(0xFFFFD54F)
                        )
                    }

                    // Contador de Alterações no Estoque (Número posicionado abaixo do ícone, fonte aumentada)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAuditLogsDialog = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("topbar_stock_changes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Alterações no Estoque",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFB300),
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "$stockChangesCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Contador de Logs do Sistema (Número posicionado abaixo do ícone, fonte aumentada)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAuditLogsDialog = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("topbar_audit_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Histórico de Logs",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.32f),
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "${allLogs.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Dev Changelog / Release Notes Info Button (ao lado do perfil do usuário)
                    IconButton(
                        onClick = { showReleaseReportDialog = true },
                        modifier = Modifier.testTag("open_release_report_button")
                    ) {
                        if (allDevLogs.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFF00B0FF),
                                        contentColor = Color.White
                                    ) {
                                        Text("${allDevLogs.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Relatório de Alterações do Desenvolvedor",
                                    tint = Color.White
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Relatório de Alterações do Desenvolvedor",
                                tint = Color.White
                            )
                        }
                    }

                    // Account & Logout Profile Icon (ao lado do ícone de informação)
                    IconButton(
                        onClick = { showUserAccountDialog = true },
                        modifier = Modifier.testTag("user_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil da Empresa e Usuário",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WineBurgundyPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                val tabSelectedBgColor = WineBurgundyPrimary
                val tabUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

                // Tab 0: Estoque
                val isTab0 = selectedTab == 0
                NavigationBarItem(
                    selected = isTab0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isTab0) tabSelectedBgColor else Color.Transparent,
                            shadowElevation = if (isTab0) 3.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = "Estoque",
                                    tint = if (isTab0) Color.White else tabUnselectedColor
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            "Estoque",
                            fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = WineBurgundyPrimary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = tabUnselectedColor,
                        unselectedTextColor = tabUnselectedColor
                    ),
                    modifier = Modifier.testTag("tab_inventory")
                )

                // Tab 1: Validades (Nova Aba de Produtos Próximos ao Vencimento)
                val isTab1 = selectedTab == 1
                NavigationBarItem(
                    selected = isTab1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isTab1) tabSelectedBgColor else Color.Transparent,
                            shadowElevation = if (isTab1) 3.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (nearExpirationWines.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = Color(0xFFD97706)) {
                                                Text("${nearExpirationWines.size}")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassBottom,
                                            contentDescription = "Validades",
                                            tint = if (isTab1) Color.White else tabUnselectedColor
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = "Validades",
                                        tint = if (isTab1) Color.White else tabUnselectedColor
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            "Validades",
                            fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = WineBurgundyPrimary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = tabUnselectedColor,
                        unselectedTextColor = tabUnselectedColor
                    ),
                    modifier = Modifier.testTag("tab_expirations")
                )

                // Tab 2: Alertas
                val isTab2 = selectedTab == 2
                NavigationBarItem(
                    selected = isTab2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isTab2) tabSelectedBgColor else Color.Transparent,
                            shadowElevation = if (isTab2) 3.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (alertCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text("$alertCount")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Alertas",
                                            tint = if (isTab2) Color.White else tabUnselectedColor
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alertas",
                                        tint = if (isTab2) Color.White else tabUnselectedColor
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            "Alertas",
                            fontWeight = if (isTab2) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = WineBurgundyPrimary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = tabUnselectedColor,
                        unselectedTextColor = tabUnselectedColor
                    ),
                    modifier = Modifier.testTag("tab_alerts")
                )

                // Tab 3: Leitor Barcode
                val isTab3 = selectedTab == 3
                NavigationBarItem(
                    selected = isTab3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isTab3) tabSelectedBgColor else Color.Transparent,
                            shadowElevation = if (isTab3) 3.dp else 0.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Leitor Barcode",
                                    tint = if (isTab3) Color.White else tabUnselectedColor
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            "Leitor",
                            fontWeight = if (isTab3) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = WineBurgundyPrimary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = tabUnselectedColor,
                        unselectedTextColor = tabUnselectedColor
                    ),
                    modifier = Modifier.testTag("tab_barcode_scanner")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> InventoryScreen(
                    wines = wines,
                    allDbWines = allDbWines,
                    searchQuery = searchQuery,
                    selectedCategoryFilter = selectedCategoryFilter,
                    selectedTypeFilter = selectedTypeFilter,
                    selectedSortOrder = selectedSortOrder,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    onCategoryFilterSelected = viewModel::onCategoryFilterSelected,
                    onTypeFilterSelected = viewModel::onTypeFilterSelected,
                    onSortOrderChange = viewModel::onSortOrderChanged,
                    onWineClick = { wine -> wineForDetailSheet = wine },
                    onIncrement = { wine -> viewModel.quickAdjustQuantity(wine, 1, "Entrada rápida no app") },
                    onDecrement = { wine -> viewModel.quickAdjustQuantity(wine, -1, "Consumo rápido no app") },
                    onAddNewWineClick = {
                        wineToEdit = null
                        prefilledBarcodeForDialog = ""
                        showAddEditDialog = true
                    },
                    onClearAllProducts = viewModel::clearAllProducts
                )

                1 -> NearExpirationScreen(
                    wines = filteredNearExpirationWines,
                    searchQuery = expirationSearchQuery,
                    selectedFilter = expirationFilterType,
                    onSearchQueryChange = viewModel::setExpirationSearchQuery,
                    onFilterChange = viewModel::setExpirationFilterType,
                    onConsume = { wine ->
                        viewModel.quickAdjustQuantity(wine, -1, "Consumo / Baixa por validade")
                    },
                    onRestock = { wine, amount ->
                        viewModel.quickAdjustQuantity(wine, amount, "Reposição de estoque")
                    },
                    onWineClick = { wine -> wineForDetailSheet = wine },
                    onAddNewProduct = {
                        wineToEdit = null
                        prefilledBarcodeForDialog = ""
                        showAddEditDialog = true
                    },
                    onOpenCategoryConfigTable = {
                        showCategoryConfigDialog = true
                    }
                )

                2 -> AlertsScreen(
                    lowStockWines = lowStockWines,
                    outOfStockWines = outOfStockWines,
                    nearExpirationWines = nearExpirationWines,
                    onRestock = { wine, amount ->
                        viewModel.quickAdjustQuantity(wine, amount, "Reposição de estoque")
                    },
                    onConsume = { wine ->
                        viewModel.quickAdjustQuantity(wine, -1, "Consumo / Baixa por validade")
                    },
                    onWineClick = { wine -> wineForDetailSheet = wine },
                    onNavigateToExpirationTab = {
                        selectedTab = 1
                    },
                    onOpenCategoryConfigTable = {
                        showCategoryConfigDialog = true
                    }
                )

                3 -> BarcodeScannerScreen(
                    scanMode = scanMode,
                    scanFeedback = scanFeedback,
                    sampleWines = wines,
                    onModeChanged = viewModel::setScanMode,
                    onBarcodeDetected = { code ->
                        viewModel.processBarcode(code) { newCode ->
                            prefilledBarcodeForDialog = newCode
                        }
                    },
                    onClearFeedback = viewModel::clearScanFeedback,
                    onRegisterNewWithBarcode = { code ->
                        wineToEdit = null
                        prefilledBarcodeForDialog = code
                        showAddEditDialog = true
                    }
                )
            }
        }
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AuditLogsDialog(
            logs = filteredLogs,
            searchQuery = logSearchQuery,
            selectedFilter = selectedLogFilter,
            isAdmin = isAdmin,
            onSearchQueryChange = viewModel::setLogSearchQuery,
            onFilterSelect = viewModel::setLogActionFilter,
            onClearLogs = viewModel::clearAllLogs,
            onAcceptLog = { log ->
                viewModel.acceptLog(log)
            },
            onRevertLog = { log ->
                viewModel.revertLog(log)
            },
            onSwitchToAdmin = {
                viewModel.switchToAdminProfile("Administrador Geral")
            },
            onSwitchToOperator = {
                viewModel.switchToOperatorProfile()
            },
            onDismiss = { showAuditLogsDialog = false }
        )
    }

    // User Account & Logout Dialog
    if (showUserAccountDialog) {
        AlertDialog(
            onDismissRequest = { showUserAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = WineBurgundyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sessão Atual do Usuário", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = authSession.employeeName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WineBurgundyPrimary
                                    )
                                    Text(
                                        text = "Login: ${authSession.employeeUsername}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = if (isAdmin) Color(0xFFFFA000).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isAdmin) "🛡️ ADM" else "👤 Operador",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAdmin) Color(0xFFD97706) else WineBurgundyPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Empresa: ${authSession.companyName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "CNPJ: ${authSession.companyCnpj}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Perfil de Administrador (ADM) Switch
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAdmin) Color(0xFFFFA000).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdmin) Color(0xFFFFA000) else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isAdmin) {
                                            viewModel.switchToOperatorProfile()
                                        } else {
                                            viewModel.switchToAdminProfile("Administrador Geral")
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = if (isAdmin) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAdmin) "Perfil: Administrador (Ativo)" else "Ativar Perfil de Administrador (ADM)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAdmin) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isAdmin) "Pode aprovar e reverter alterações" else "Toque para alternar para modo ADM",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Database section in profile
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showUserAccountDialog = false
                                        showDatabaseSelectorDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Banco: ${DatabaseManager.getDatabaseDisplayName(currentDatabaseId)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Arquivo: $currentDatabaseId",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Alterar",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Seletor de Modo Claro / Escuro
                            Text(
                                text = "Tema do Aplicativo",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val modes = listOf(
                                    Triple(AppThemeMode.LIGHT, "Claro", Icons.Default.LightMode),
                                    Triple(AppThemeMode.DARK, "Escuro", Icons.Default.DarkMode),
                                    Triple(AppThemeMode.SYSTEM, "Auto", Icons.Default.BrightnessAuto)
                                )
                                modes.forEach { (mode, label, icon) ->
                                    val isSelected = appThemeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setAppThemeMode(mode) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Botão para Alterar Senha de Acesso
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showUserAccountDialog = false
                                        showChangePasswordDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Alterar Senha",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Modificar senha de acesso do seu perfil",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Editar",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Todas as alterações que você realizar no estoque ficarão registradas no histórico de logs em seu nome.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUserAccountDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trocar Usuário / Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserAccountDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Add / Edit Wine Dialog
    if (showAddEditDialog) {
        AddEditWineDialog(
            initialWine = wineToEdit,
            prefilledBarcode = prefilledBarcodeForDialog,
            existingWines = wines,
            categoryConfigs = allCategoryConfigs,
            onDismiss = {
                showAddEditDialog = false
                wineToEdit = null
                prefilledBarcodeForDialog = ""
            },
            onSave = { savedWine ->
                val wasEdit = wineToEdit != null
                viewModel.saveWine(savedWine) {
                    // Success callback
                }
                showAddEditDialog = false
                wineToEdit = null
                prefilledBarcodeForDialog = ""
                recentlySavedWine = savedWine
                isRecentSaveEdit = wasEdit
                showSaveSuccessDialog = true
            },
            onScanBarcodeClick = {
                selectedTab = 3 // Navigate to Barcode Scanner Tab!
                showAddEditDialog = false
            },
            onOpenBatchImport = {
                showAddEditDialog = false
                showBatchImportDialog = true
            }
        )
    }

    // Tabela Padrão de Índices Mínimos por Categoria
    if (showCategoryConfigDialog) {
        CategoryConfigTableDialog(
            categoryConfigs = allCategoryConfigs,
            onSaveConfig = { categoryName, newMinQuantity, updateExistingProducts ->
                viewModel.updateCategoryMinStock(categoryName, newMinQuantity, updateExistingProducts)
            },
            onAddNewCategory = { categoryName, minQuantity, unitDescription, notes ->
                viewModel.addCategoryConfig(categoryName, minQuantity, unitDescription, notes)
            },
            onDeleteCategory = { categoryName ->
                viewModel.deleteCategoryConfig(categoryName)
            },
            onDismiss = { showCategoryConfigDialog = false }
        )
    }

    // Save Success Confirmation Dialog
    if (showSaveSuccessDialog && recentlySavedWine != null) {
        com.example.ui.components.SaveSuccessDialog(
            savedWine = recentlySavedWine!!,
            isEdit = isRecentSaveEdit,
            onDismiss = {
                showSaveSuccessDialog = false
                recentlySavedWine = null
            },
            onNavigateToRegister = {
                showSaveSuccessDialog = false
                recentlySavedWine = null
                wineToEdit = null
                prefilledBarcodeForDialog = ""
                showAddEditDialog = true
            },
            onNavigateToInventory = {
                showSaveSuccessDialog = false
                recentlySavedWine = null
                selectedTab = 0 // Navigate directly to Inventory tab!
            }
        )
    }

    // Batch Import Dialog (Enviar Lista de Produtos)
    if (showBatchImportDialog) {
        com.example.ui.components.BatchImportDialog(
            onDismiss = { showBatchImportDialog = false },
            onImportBatch = { items, summaryMsg ->
                viewModel.saveWinesBatch(items)
                showBatchImportDialog = false
                if (items.isNotEmpty()) {
                    recentlySavedWine = items.last()
                    isRecentSaveEdit = false
                    showSaveSuccessDialog = true
                }
            }
        )
    }

    // Wine Detail Bottom Sheet
    if (wineForDetailSheet != null) {
        val currentDetailWine = wineForDetailSheet!!

        WineDetailSheet(
            wine = currentDetailWine,
            sheetState = sheetState,
            onDismiss = {
                wineForDetailSheet = null
            },
            onEdit = {
                wineToEdit = currentDetailWine
                prefilledBarcodeForDialog = currentDetailWine.barcode
                wineForDetailSheet = null
                showAddEditDialog = true
            },
            onDelete = {
                viewModel.deleteWine(currentDetailWine)
                wineForDetailSheet = null
            },
            onAdjustQuantity = { delta ->
                viewModel.quickAdjustQuantity(
                    currentDetailWine,
                    delta,
                    if (delta > 0) "Adicionado via detalhes" else "Baixa via detalhes"
                )
                val newQty = (currentDetailWine.quantity + delta).coerceAtLeast(0)
                wineForDetailSheet = currentDetailWine.copy(quantity = newQty)
            }
        )
    }

    if (showReleaseReportDialog) {
        AppReleaseReportDialog(
            devChangeLogs = filteredDevChangeLogs,
            searchQuery = devLogSearchQuery,
            selectedCategoryFilter = selectedDevLogCategoryFilter,
            onSearchQueryChange = viewModel::setDevLogSearchQuery,
            onCategoryFilterSelect = viewModel::setSelectedDevLogCategoryFilter,
            onDismiss = { showReleaseReportDialog = false }
        )
    }

    // Database Selector Dialog
    if (showDatabaseSelectorDialog) {
        DatabaseSelectorDialog(
            currentDatabaseId = currentDatabaseId,
            onSelectDatabase = { newDbId ->
                viewModel.selectDatabase(newDbId)
            },
            onDismiss = { showDatabaseSelectorDialog = false }
        )
    }

    // Change Password Dialog (Perfil do Usuário)
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = { currentPass, newPass, callback ->
                viewModel.changePassword(currentPass, newPass, callback)
            }
        )
    }
}
