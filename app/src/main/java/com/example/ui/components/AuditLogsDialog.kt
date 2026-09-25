package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AuditLog
import com.example.ui.theme.WineBurgundyPrimary

@Composable
fun AuditLogsDialog(
    logs: List<AuditLog>,
    searchQuery: String,
    selectedFilter: String,
    isAdmin: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelect: (String) -> Unit,
    onClearLogs: () -> Unit,
    onAcceptLog: ((AuditLog) -> Unit)? = null,
    onRevertLog: (AuditLog) -> Unit,
    onSwitchToAdmin: () -> Unit,
    onSwitchToOperator: () -> Unit,
    onDismiss: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    var logToRevert by remember { mutableStateOf<AuditLog?>(null) }
    var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.93f)
                .testTag("audit_logs_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = WineBurgundyPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = WineBurgundyPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auditoria & Alterações ADM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${logs.size} modificações • Painel de Controle de Dados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAdmin && logs.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Limpar Logs",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Profile Admin Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAdmin) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAdmin) Color(0xFF2E7D32) else Color(0xFFF57C00)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isAdmin) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isAdmin) "Perfil: Administrador (ADM) Ativo" else "Perfil: Operador (Somente Leitura)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) Color(0xFF1B5E20) else Color(0xFFE65100)
                                )
                                Text(
                                    text = if (isAdmin)
                                        "Você pode aceitar ou reverter qualquer alteração feita no banco."
                                    else
                                        "Alterne para perfil de Administrador para aceitar ou reverter alterações.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAdmin) Color(0xFF2E7D32) else Color(0xFFB26A00)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (isAdmin) {
                            OutlinedButton(
                                onClick = onSwitchToOperator,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Mudar p/ Operador", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Button(
                                onClick = onSwitchToAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("switch_to_admin_button")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ativar ADM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (!actionFeedbackMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WineBurgundyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = actionFeedbackMessage!!,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Pesquisar por produto, operador, CNPJ ou ação...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Pesquisar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar Busca")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WineBurgundyPrimary,
                        cursorColor = WineBurgundyPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_logs_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                val filters = listOf(
                    "TODOS" to "Todos",
                    "PENDENTE" to "⏳ Pendentes",
                    "REVERTIDA" to "🔄 Revertidas",
                    "CADASTRO" to "Cadastros",
                    "EDICAO" to "Edições",
                    "EXCLUSAO" to "Exclusões",
                    "ENTRADA_ESTOQUE" to "Entradas",
                    "SAIDA_ESTOQUE" to "Baixas"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterSelect(key) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WineBurgundyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Logs List
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Nenhuma modificação encontrada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Todas as alterações feitas no banco de dados serão exibidas aqui para validação.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("audit_logs_list")
                    ) {
                        items(logs, key = { it.id }) { log ->
                            AdminLogItemCard(
                                log = log,
                                isAdmin = isAdmin,
                                onRevert = {
                                    logToRevert = log
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog before reverting database operation
    if (logToRevert != null) {
        val target = logToRevert!!
        AlertDialog(
            onDismissRequest = { logToRevert = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reverter Modificação no Banco?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Você está prestes a reverter a alteração '${target.actionType}' feita por ${target.employeeName} em '${target.productName}'.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ação que será executada:\n" + when (target.actionType) {
                            "CADASTRO" -> "• O produto cadastrado indevidamente será removido do estoque."
                            "EXCLUSAO" -> "• O produto excluído será recuperado e reinserido no estoque."
                            "EDICAO" -> "• Os valores anteriores do produto serão restaurados."
                            "ENTRADA_ESTOQUE", "SAIDA_ESTOQUE" -> "• A quantidade em estoque será restaurada para o valor prévio."
                            else -> "• A modificação será marcada como revertida."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRevertLog(target)
                        actionFeedbackMessage = "Modificação de '${target.productName}' foi revertida no banco com sucesso!"
                        logToRevert = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Reverter Agora")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToRevert = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Limpar Histórico de Logs?") },
            text = { Text("Todos os registros de auditoria e histórico de modificações serão excluídos. Deseja continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Limpar Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AdminLogItemCard(
    log: AuditLog,
    isAdmin: Boolean,
    onRevert: () -> Unit
) {
    // Detecção dinâmica de modo escuro para garantir máximo contraste e visibilidade
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = (0.299 * surfaceColor.red + 0.587 * surfaceColor.green + 0.114 * surfaceColor.blue) < 0.5

    val actionStyle = when (log.actionType) {
        "CADASTRO" -> LogActionStyle(
            if (isDark) Color(0xFF69F0AE) else Color(0xFF1B5E20),
            if (isDark) Color(0x3369F0AE) else Color(0xFFE8F5E9),
            if (isDark) Color(0x9969F0AE) else Color(0xFFA5D6A7),
            Icons.Default.AddCircle,
            "CADASTRO"
        )
        "EDICAO" -> LogActionStyle(
            if (isDark) Color(0xFF40C4FF) else Color(0xFF0D47A1),
            if (isDark) Color(0x3340C4FF) else Color(0xFFE3F2FD),
            if (isDark) Color(0x9940C4FF) else Color(0xFF90CAF9),
            Icons.Default.Edit,
            "EDIÇÃO"
        )
        "EXCLUSAO" -> LogActionStyle(
            if (isDark) Color(0xFFFF5252) else Color(0xFFB71C1C),
            if (isDark) Color(0x33FF5252) else Color(0xFFFFEBEE),
            if (isDark) Color(0x99FF5252) else Color(0xFFEF9A9A),
            Icons.Default.Delete,
            "EXCLUSÃO"
        )
        "ENTRADA_ESTOQUE" -> LogActionStyle(
            if (isDark) Color(0xFF1DE9B6) else Color(0xFF004D40),
            if (isDark) Color(0x331DE9B6) else Color(0xFFE0F2F1),
            if (isDark) Color(0x991DE9B6) else Color(0xFF80CBC4),
            Icons.Default.AddCircle,
            "ENTRADA"
        )
        "SAIDA_ESTOQUE" -> LogActionStyle(
            if (isDark) Color(0xFFFFAB40) else Color(0xFFBF360C),
            if (isDark) Color(0x33FFAB40) else Color(0xFFFBE9E7),
            if (isDark) Color(0x99FFAB40) else Color(0xFFFFAB91),
            Icons.Default.RemoveCircle,
            "BAIXA / SAÍDA"
        )
        "IMPORTACAO_LOTE" -> LogActionStyle(
            if (isDark) Color(0xFFE040FB) else Color(0xFF4A148C),
            if (isDark) Color(0x33E040FB) else Color(0xFFF3E5F5),
            if (isDark) Color(0x99E040FB) else Color(0xFFCE93D8),
            Icons.Default.PlaylistAdd,
            "IMPORTAÇÃO"
        )
        "LIMPEZA_ESTOQUE" -> LogActionStyle(
            if (isDark) Color(0xFFFF1744) else Color(0xFFB71C1C),
            if (isDark) Color(0x33FF1744) else Color(0xFFFFEBEE),
            if (isDark) Color(0x99FF1744) else Color(0xFFEF9A9A),
            Icons.Default.Warning,
            "LIMPEZA GERAL"
        )
        "LOGIN", "LOGOUT" -> LogActionStyle(
            if (isDark) Color(0xFFCFD8DC) else Color(0xFF37474F),
            if (isDark) Color(0x33CFD8DC) else Color(0xFFECEFF1),
            if (isDark) Color(0x99CFD8DC) else Color(0xFFB0BEC5),
            Icons.Default.Lock,
            log.actionType
        )
        "REVERSAO_ADM" -> LogActionStyle(
            if (isDark) Color(0xFFB388FF) else Color(0xFF4527A0),
            if (isDark) Color(0x33B388FF) else Color(0xFFEDE7F6),
            if (isDark) Color(0x99B388FF) else Color(0xFFD1C4E9),
            Icons.Default.Undo,
            "REVERSÃO ADM"
        )
        else -> LogActionStyle(
            if (isDark) Color(0xFFB388FF) else Color(0xFF5E35B1),
            if (isDark) Color(0x33B388FF) else Color(0xFFEDE7F6),
            if (isDark) Color(0x99B388FF) else Color(0xFFD1C4E9),
            Icons.Default.SwapVert,
            log.actionType
        )
    }

    val statusStyle = when (log.status) {
        "REVERTIDA" -> LogStatusStyle(
            if (isDark) Color(0xFFFF5252) else Color(0xFFC62828),
            if (isDark) Color(0x33FF5252) else Color(0xFFFFEBEE),
            if (isDark) Color(0x99FF5252) else Color(0xFFEF9A9A),
            "🔄 Revertida"
        )
        "ACEITA" -> LogStatusStyle(
            if (isDark) Color(0xFF69F0AE) else Color(0xFF2E7D32),
            if (isDark) Color(0x3369F0AE) else Color(0xFFE8F5E9),
            if (isDark) Color(0x9969F0AE) else Color(0xFFA5D6A7),
            "✅ Validada"
        )
        else -> LogStatusStyle(
            if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
            if (isDark) Color(0x33FFD54F) else Color(0xFFFFF3E0),
            if (isDark) Color(0x99FFD54F) else Color(0xFFFFB74D),
            "⏳ Pendente"
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.65f else 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Action Badge + Status Badge + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Badge de Tipo de Ação com alto contraste
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = actionStyle.bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, actionStyle.borderColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(imageVector = actionStyle.icon, contentDescription = null, tint = actionStyle.textColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = actionStyle.text,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = actionStyle.textColor
                            )
                        }
                    }

                    // Badge de Status de Ação no Modo Escuro e Claro com alto contraste
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusStyle.bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusStyle.borderColor)
                    ) {
                        Text(
                            text = statusStyle.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusStyle.textColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = log.getFormattedDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name & Category
            Text(
                text = log.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Description
            Text(
                text = log.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            if (log.oldValue != null || log.newValue != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (log.oldValue != null) {
                        Text(
                            text = "Antes: ${log.oldValue}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (log.newValue != null) {
                        Text(
                            text = "Depois: ${log.newValue}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFFF8A80) else WineBurgundyPrimary
                        )
                    }
                }
            }

            if (log.reviewedBy != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Revisado por ${log.reviewedBy} em ${log.getFormattedReviewDateTime() ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusStyle.textColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Operator info + ADMIN REVERT ACTION ONLY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFF8A80) else WineBurgundyPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = log.employeeName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFFFF8A80) else WineBurgundyPrimary
                    )
                }

                // Opção do ADM: apenas a opção de Reverter Ação (Aceitar removido conforme solicitação)
                if (isAdmin && !log.actionType.equals("REVERSAO_ADM", ignoreCase = true) && !log.actionType.equals("LOGIN", ignoreCase = true) && !log.actionType.equals("LOGOUT", ignoreCase = true)) {
                    if (!log.isReverted) {
                        Button(
                            onClick = onRevert,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFFD32F2F) else MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("revert_log_${log.id}")
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reverter Ação", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0x33FF5252) else Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0x99FF5252) else Color(0xFFEF9A9A))
                        ) {
                            Text(
                                text = "Revertida pelo ADM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFF5252) else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data holders for colors and icons
private data class LogActionStyle(
    val textColor: Color,
    val bgColor: Color,
    val borderColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val text: String
)

private data class LogStatusStyle(
    val textColor: Color,
    val bgColor: Color,
    val borderColor: Color,
    val label: String
)
