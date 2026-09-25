package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CompanyProfile
import com.example.data.DatabaseManager
import com.example.data.formatCnpj
import com.example.ui.components.DatabaseSelectorDialog
import com.example.ui.components.ForgotPasswordDialog
import com.example.ui.theme.WineBurgundyDark
import com.example.ui.theme.WineBurgundyPrimary

@Composable
fun LoginScreen(
    savedCompanies: List<CompanyProfile> = emptyList(),
    currentDatabaseId: String = "adega_database",
    onSelectDatabase: ((String) -> Unit)? = null,
    onRequestResetCode: ((companyCnpj: String, identifier: String, callback: (Boolean, String, String?, String?) -> Unit) -> Unit)? = null,
    onConfirmReset: ((companyCnpj: String, identifier: String, newPass: String, callback: (Boolean, String) -> Unit) -> Unit)? = null,
    onLoginSuccess: (companyCnpj: String, companyName: String, username: String, fullName: String, role: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatabaseDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Current step (0: Empresa CNPJ, 1: Funcionário & Senha)
    var currentStep by remember { mutableIntStateOf(0) }

    // Step 1 - Empresa
    var cnpjInput by remember { mutableStateOf("12.345.678/0001-90") }
    var companyNameInput by remember { mutableStateOf("Adega & Distribuidora Matriz") }

    // Step 2 - Funcionário
    var usernameInput by remember { mutableStateOf("carlos.silva") }
    var employeeFullNameInput by remember { mutableStateOf("Carlos Silva") }
    var passwordInput by remember { mutableStateOf("123456") }
    var roleInput by remember { mutableStateOf("Operador de Estoque") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetCompanies = listOf(
        Triple("12.345.678/0001-90", "Adega & Distribuidora Matriz", "Matriz Comércio"),
        Triple("98.765.432/0001-10", "Adega Empório Prime", "Empório Prime"),
        Triple("34.567.890/0001-22", "Vinhos & Cia Distribuidora", "Vinhos & Cia")
    )

    data class ProfilePreset(val user: String, val full: String, val role: String, val pass: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
    val presetEmployees = listOf(
        ProfilePreset("carlos.silva", "Carlos Silva", "Operador de Estoque", "123456", Icons.Default.Inventory),
        ProfilePreset("admin", "Administrador Geral", "Administrador", "admin123", Icons.Default.AdminPanelSettings),
        ProfilePreset("mariana.lima", "Mariana Lima", "Gerente de Logística", "123456", Icons.Default.SupervisorAccount)
    )

    // Dynamic animations
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val cleanCnpj = cnpjInput.filter { it.isDigit() }
    val isCnpjValid = cleanCnpj.length == 14

    // Password strength computation
    val passwordStrength = remember(passwordInput) {
        when {
            passwordInput.length < 4 -> 1
            passwordInput.length < 6 -> 2
            passwordInput.length < 8 && passwordInput.any { it.isDigit() } -> 3
            passwordInput.length >= 8 && passwordInput.any { it.isDigit() } && passwordInput.any { !it.isLetterOrDigit() } -> 4
            else -> 3
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WineBurgundyPrimary,
                        WineBurgundyDark,
                        Color(0xFF19060C)
                    )
                )
            )
            .imePadding()
    ) {
        // Dynamic decorative floating glowing ambient elements in background
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x33FF5252), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x228B0000), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic live system connectivity badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4469F0AE)),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .background(Color(0xFF69F0AE), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sistema Conectado • Max Server Ativo • 12ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB9F6CA),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // App Logo with glowing dynamic frame
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x66FF8A80), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.max_bebidas_icon),
                        contentDescription = "Max Bebidas Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Max Bebidas & Estoque",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Controle Integrado de Estoque, Adegas & Auditoria",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Database Selector Chip with Interactive Pulse
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.16f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showDatabaseDialog = true }
                    .testTag("login_database_selector_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Banco: ${DatabaseManager.getDatabaseDisplayName(currentDatabaseId)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "▼",
                        fontSize = 9.sp,
                        color = Color(0xFFFFD54F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Step Switcher with animated pill highlight
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Step 1: Empresa
                    val isStep0 = currentStep == 0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isStep0) Color.White else Color.Transparent,
                        shadowElevation = if (isStep0) 4.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { currentStep = 0 }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = if (isStep0) WineBurgundyPrimary else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Empresa",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isStep0) WineBurgundyPrimary else Color.White
                            )
                        }
                    }

                    // Step 2: Funcionário
                    val isStep1 = currentStep == 1
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isStep1) Color.White else Color.Transparent,
                        shadowElevation = if (isStep1) 4.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (cleanCnpj.length >= 8) {
                                    currentStep = 1
                                } else {
                                    errorMessage = "Digite o CNPJ da empresa primeiro."
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isStep1) WineBurgundyPrimary else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. Usuário & Acesso",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isStep1) WineBurgundyPrimary else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Dynamic Form Card with Animated Transitions
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "login_step_animation"
                    ) { targetStep ->
                        if (targetStep == 0) {
                            // STEP 1: EMPRESA POR CNPJ
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "Identificação da Empresa",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = WineBurgundyPrimary
                                        )
                                        Text(
                                            text = "Vincule o estoque e auditoria de logs ao CNPJ.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isCnpjValid) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFE8F5E9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Válido", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = cnpjInput,
                                    onValueChange = { raw ->
                                        val clean = raw.filter { it.isDigit() }.take(14)
                                        cnpjInput = formatCnpj(clean)
                                        errorMessage = null
                                    },
                                    label = { Text("CNPJ da Empresa *") },
                                    placeholder = { Text("00.000.000/0000-00") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.CorporateFare, contentDescription = null, tint = WineBurgundyPrimary)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_cnpj_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = companyNameInput,
                                    onValueChange = {
                                        companyNameInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("Nome Fantasia ou Razão Social") },
                                    placeholder = { Text("Ex: Adega & Distribuidora Matriz") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = WineBurgundyPrimary)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_company_name_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Empresa Presets Chips
                                Text(
                                    text = "Empresas pré-cadastradas (clique para preencher):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    presetCompanies.forEach { (cnpj, name, fantasy) ->
                                        val isSelected = cnpjInput == cnpj
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) WineBurgundyPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, WineBurgundyPrimary) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    cnpjInput = cnpj
                                                    companyNameInput = name
                                                    errorMessage = null
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = fantasy,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) WineBurgundyPrimary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = cnpj,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = WineBurgundyPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (cleanCnpj.length < 8) {
                                            errorMessage = "Por favor, digite um CNPJ válido com ao menos 8 dígitos."
                                            return@Button
                                        }
                                        currentStep = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_step1_next")
                                ) {
                                    Text("Avançar para Usuário", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // STEP 2: FUNCIONÁRIO, SENHA & CARGO
                            Column {
                                Text(
                                    text = "Acesso do Funcionário",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WineBurgundyPrimary
                                )
                                Text(
                                    text = "Selecione o perfil ou digite as credenciais de acesso.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dynamic Profile Cards Deck (Interactive Avatars)
                                Text(
                                    text = "Perfis Rápidos (1 clique para selecionar):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    presetEmployees.forEach { profile ->
                                        val isSelected = usernameInput == profile.user
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) WineBurgundyPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, WineBurgundyPrimary) else androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    usernameInput = profile.user
                                                    employeeFullNameInput = profile.full
                                                    roleInput = profile.role
                                                    passwordInput = profile.pass
                                                    errorMessage = null
                                                }
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            if (isSelected) WineBurgundyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = profile.icon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = profile.full.split(" ").first(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) WineBurgundyPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = profile.role.split(" ").first(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = usernameInput,
                                    onValueChange = {
                                        usernameInput = it.lowercase().trim()
                                        errorMessage = null
                                    },
                                    label = { Text("Usuário / Login *") },
                                    placeholder = { Text("Ex: carlos.silva") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = WineBurgundyPrimary)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_employee_username_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = employeeFullNameInput,
                                    onValueChange = {
                                        employeeFullNameInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("Nome do Funcionário (para logs)") },
                                    placeholder = { Text("Ex: Carlos Silva") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = WineBurgundyPrimary)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_employee_fullname_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = {
                                        passwordInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("Senha do Funcionário *") },
                                    placeholder = { Text("Digite sua senha") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = WineBurgundyPrimary)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input")
                                )

                                // Dynamic Password Strength Meter
                                if (passwordInput.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        for (i in 1..4) {
                                            val barColor = when {
                                                i > passwordStrength -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                passwordStrength == 1 -> Color(0xFFFF5252)
                                                passwordStrength == 2 -> Color(0xFFFFB74D)
                                                passwordStrength == 3 -> Color(0xFF64B5F6)
                                                else -> Color(0xFF69F0AE)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp)
                                                    .padding(horizontal = 2.dp)
                                                    .background(barColor, RoundedCornerShape(2.dp))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val strengthLabel = when (passwordStrength) {
                                            1 -> "Fraca"
                                            2 -> "Média"
                                            3 -> "Boa"
                                            else -> "Forte"
                                        }
                                        Text(
                                            text = strengthLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (passwordStrength) {
                                                1 -> Color(0xFFFF5252)
                                                2 -> Color(0xFFFFB74D)
                                                3 -> Color(0xFF1976D2)
                                                else -> Color(0xFF2E7D32)
                                            }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { currentStep = 0 },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Text("← Alterar Empresa", style = MaterialTheme.typography.bodySmall)
                                    }

                                    TextButton(
                                        onClick = { showForgotPasswordDialog = true },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                                        modifier = Modifier.testTag("btn_forgot_password")
                                    ) {
                                        Text(
                                            text = "Esqueceu a senha?",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = WineBurgundyPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (usernameInput.isBlank()) {
                                            errorMessage = "Por favor, digite o login/usuário do funcionário."
                                            return@Button
                                        }
                                        if (passwordInput.isBlank()) {
                                            errorMessage = "Por favor, digite a senha do funcionário."
                                            return@Button
                                        }

                                        val finalName = if (employeeFullNameInput.isNotBlank()) employeeFullNameInput else usernameInput
                                        onLoginSuccess(
                                            cnpjInput,
                                            companyNameInput,
                                            usernameInput,
                                            finalName,
                                            roleInput
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WineBurgundyPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_submit_login")
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Entrar no Sistema de Estoque", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Demo Access Button with Dynamic Pulse Effect
            OutlinedButton(
                onClick = {
                    onLoginSuccess(
                        "12.345.678/0001-90",
                        "Adega & Distribuidora Matriz",
                        "carlos.silva",
                        "Carlos Silva",
                        "Operador de Estoque"
                    )
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("quick_demo_login_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Entrar com Acesso Instantâneo (Demo)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Max Bebidas • Versão 3.2.0 • Modo Offline-First",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }

    if (showDatabaseDialog) {
        DatabaseSelectorDialog(
            currentDatabaseId = currentDatabaseId,
            onSelectDatabase = { newDbId ->
                onSelectDatabase?.invoke(newDbId)
            },
            onDismiss = { showDatabaseDialog = false }
        )
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialCnpj = cnpjInput,
            onDismiss = { showForgotPasswordDialog = false },
            onRequestResetCode = { cnpj, identifier, callback ->
                if (onRequestResetCode != null) {
                    onRequestResetCode(cnpj, identifier, callback)
                } else {
                    callback(true, "Código enviado para o e-mail cadastrado.", "$identifier@maxbebidas.com.br", "123456")
                }
            },
            onConfirmReset = { cnpj, identifier, newPass, callback ->
                if (onConfirmReset != null) {
                    onConfirmReset(cnpj, identifier, newPass, callback)
                } else {
                    callback(true, "Senha redefinida com sucesso!")
                }
            },
            onSuccessReset = { user, newPass ->
                usernameInput = user
                passwordInput = newPass
                errorMessage = null
            }
        )
    }
}
