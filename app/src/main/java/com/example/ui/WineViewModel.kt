package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuditLog
import com.example.data.AuthSession
import com.example.data.CategoryConfig
import com.example.data.CompanyProfile
import com.example.data.EmployeeUser
import com.example.data.StockTransaction
import com.example.data.VintageStatus
import com.example.data.WineItem
import com.example.data.WineRepository
import com.example.data.containsNormalized
import com.example.ui.theme.AppThemeMode
import com.example.util.SoundFeedbackHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

enum class SortOrder(val label: String) {
    NAME("Nome A-Z"),
    EXPIRATION("Validade"),
    QUANTITY_ASC("Menor"),
    QUANTITY_DESC("Maior"),
    TROCA("Troca")
}

enum class ScanMode {
    CONSUME,  // Informar Baixa / Falta no estoque
    REGISTER  // Cadastrar / Adicionar Produto
}

data class ScanFeedback(
    val title: String,
    val message: String,
    val isSuccess: Boolean,
    val wineItem: WineItem? = null,
    val scannedCode: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class WineViewModel(application: Application) : AndroidViewModel(application) {

    val currentDatabaseName = MutableStateFlow(
        com.example.data.DatabaseManager.getSelectedDatabaseName(application)
    )

    private val repositories = ConcurrentHashMap<String, WineRepository>()

    fun getRepositoryForDatabase(dbName: String): WineRepository {
        val safeName = if (dbName.isBlank()) com.example.data.AppDatabase.DEFAULT_DATABASE_NAME else dbName.trim()
        return repositories.computeIfAbsent(safeName) { name ->
            val database = com.example.data.AppDatabase.getDatabase(getApplication(), name)
            WineRepository(
                database.wineDao(),
                database.transactionDao(),
                database.auditLogDao(),
                database.companyDao(),
                database.employeeDao(),
                database.categoryConfigDao(),
                database.devChangeLogDao()
            )
        }
    }

    private val repository: WineRepository
        get() = getRepositoryForDatabase(currentDatabaseName.value)

    private val prefs = application.getSharedPreferences("adega_auth_prefs", Context.MODE_PRIVATE)

    // App Theme Mode State (SYSTEM, LIGHT, DARK)
    val appThemeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("app_theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    )

    fun setAppThemeMode(mode: AppThemeMode) {
        appThemeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    // Auth Session State
    val authSession = MutableStateFlow(
        AuthSession(
            isLoggedIn = prefs.getBoolean("is_logged_in", true),
            companyCnpj = prefs.getString("company_cnpj", "12.345.678/0001-90") ?: "12.345.678/0001-90",
            companyName = prefs.getString("company_name", "Adega & Distribuidora Matriz") ?: "Adega & Distribuidora Matriz",
            employeeUsername = prefs.getString("employee_username", "carlos.silva") ?: "carlos.silva",
            employeeName = prefs.getString("employee_name", "Carlos Silva (Conferente)") ?: "Carlos Silva (Conferente)",
            role = prefs.getString("employee_role", "Operador de Estoque") ?: "Operador de Estoque",
            loginTimestamp = prefs.getLong("login_timestamp", System.currentTimeMillis())
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCompanies: StateFlow<List<CompanyProfile>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).allCompanies }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allAuditLogs: StateFlow<List<AuditLog>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).allAuditLogs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCategoryConfigs: StateFlow<List<CategoryConfig>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).allCategoryConfigs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allDevChangeLogs: StateFlow<List<com.example.data.DevChangeLog>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).allDevChangeLogs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dev Change Log Filters
    val devLogSearchQuery = MutableStateFlow("")
    val selectedDevLogCategoryFilter = MutableStateFlow("TODOS")

    // Audit Log Filters
    val logSearchQuery = MutableStateFlow("")
    val selectedLogActionFilter = MutableStateFlow("TODOS")

    // Near Expiration Search & Filters
    val expirationSearchQuery = MutableStateFlow("")
    val expirationFilterType = MutableStateFlow("TODOS") // "TODOS", "VENCIDOS", "7_DIAS", "15_DIAS", "30_DIAS"

    init {
        viewModelScope.launch {
            val initialRepo = getRepositoryForDatabase(currentDatabaseName.value)
            seedInitialAuthDataIfEmpty(initialRepo)
            initialRepo.seedDefaultCategoryConfigsIfEmpty()
            initialRepo.seedDefaultDevChangeLogsIfEmpty()
            initialRepo.prepopulateSampleDataIfEmpty()
        }
    }

    fun selectDatabase(newDbName: String, onComplete: ((String) -> Unit)? = null) {
        val cleanName = if (newDbName.isBlank()) com.example.data.AppDatabase.DEFAULT_DATABASE_NAME else newDbName.trim()
        val previousDb = currentDatabaseName.value
        if (cleanName == previousDb) {
            onComplete?.invoke(cleanName)
            return
        }

        viewModelScope.launch {
            com.example.data.DatabaseManager.setSelectedDatabaseName(getApplication(), cleanName)
            currentDatabaseName.value = cleanName
            val repo = getRepositoryForDatabase(cleanName)

            // Seed defaults if the selected database is empty
            seedInitialAuthDataIfEmpty(repo)
            repo.seedDefaultCategoryConfigsIfEmpty()
            repo.seedDefaultDevChangeLogsIfEmpty()
            repo.prepopulateSampleDataIfEmpty()

            // Record dev changelog for database switch
            repo.addDevChangeLog(
                versionTitle = "v2.6 - Seleção de Banco de Dados",
                category = "DATABASE",
                summary = "Banco de Dados Ativado: $cleanName",
                details = "O usuário selecionou o banco de dados '$cleanName' para armazenar e consultar todos os registros da aplicação."
            )

            // Record audit log
            val session = authSession.value
            repo.addAuditLog(
                actionType = "SISTEMA",
                productName = "Banco de Dados: $cleanName",
                category = "Configuração",
                description = "Alternado banco de dados ativo de '$previousDb' para '$cleanName'",
                employeeName = session.employeeName,
                companyCnpj = session.companyCnpj,
                companyName = session.companyName,
                oldValue = previousDb,
                newValue = cleanName
            )

            onComplete?.invoke(cleanName)
        }
    }

    private suspend fun seedInitialAuthDataIfEmpty(targetRepo: WineRepository = repository) {
        val defaultCnpj = "12.345.678/0001-90"
        val existingCompany = targetRepo.getCompanyByCnpj(defaultCnpj)
        if (existingCompany == null) {
            targetRepo.saveCompany(
                CompanyProfile(
                    cnpj = defaultCnpj,
                    tradeName = "Adega & Distribuidora Matriz",
                    legalName = "Matriz Comércio de Bebidas e Alimentos LTDA"
                )
            )
            targetRepo.saveCompany(
                CompanyProfile(
                    cnpj = "98.765.432/0001-10",
                    tradeName = "Adega Empório Prime",
                    legalName = "Empório Prime Vinhos e Destilados S/A"
                )
            )
            targetRepo.saveCompany(
                CompanyProfile(
                    cnpj = "34.567.890/0001-22",
                    tradeName = "Vinhos & Cia Distribuidora",
                    legalName = "Vinhos e Cia Logística e Armazenagem EIRELI"
                )
            )

            // Seed initial employees
            targetRepo.saveEmployee(
                EmployeeUser(
                    companyCnpj = defaultCnpj,
                    username = "carlos.silva",
                    fullName = "Carlos Silva",
                    passwordHash = "123456",
                    role = "Operador de Estoque"
                )
            )
            targetRepo.saveEmployee(
                EmployeeUser(
                    companyCnpj = defaultCnpj,
                    username = "admin",
                    fullName = "Administrador Geral",
                    passwordHash = "admin123",
                    role = "Administrador"
                )
            )
            targetRepo.saveEmployee(
                EmployeeUser(
                    companyCnpj = defaultCnpj,
                    username = "mariana.lima",
                    fullName = "Mariana Lima",
                    passwordHash = "123456",
                    role = "Gerente de Logística"
                )
            )
        }
    }

    // Filtered Audit Logs
    val filteredAuditLogs: StateFlow<List<AuditLog>> = combine(
        allAuditLogs,
        logSearchQuery,
        selectedLogActionFilter
    ) { logs, query, actionFilter ->
        logs.filter { log ->
            val matchesQuery = query.isBlank() ||
                    log.productName.containsNormalized(query) ||
                    log.description.containsNormalized(query) ||
                    log.employeeName.containsNormalized(query) ||
                    log.category.containsNormalized(query) ||
                    log.companyCnpj.contains(query.trim())

            val matchesAction = actionFilter == "TODOS" ||
                    log.actionType.equals(actionFilter, ignoreCase = true) ||
                    (actionFilter == "ESTOQUE" && (log.actionType == "ENTRADA_ESTOQUE" || log.actionType == "SAIDA_ESTOQUE"))

            matchesQuery && matchesAction
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Dev Change Logs (Relatório de Alterações feitas pelo Dev)
    val filteredDevChangeLogs: StateFlow<List<com.example.data.DevChangeLog>> = combine(
        allDevChangeLogs,
        devLogSearchQuery,
        selectedDevLogCategoryFilter
    ) { logs, query, categoryFilter ->
        logs.filter { log ->
            val matchesQuery = query.isBlank() ||
                    log.summary.containsNormalized(query) ||
                    log.details.containsNormalized(query) ||
                    log.versionTitle.containsNormalized(query) ||
                    log.category.containsNormalized(query)

            val matchesCategory = categoryFilter == "TODOS" ||
                    log.category.equals(categoryFilter, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDevLogSearchQuery(query: String) {
        devLogSearchQuery.value = query
    }

    fun setSelectedDevLogCategoryFilter(category: String) {
        selectedDevLogCategoryFilter.value = category
    }

    fun registerDevChange(
        versionTitle: String,
        category: String,
        summary: String,
        details: String
    ) {
        viewModelScope.launch {
            repository.addDevChangeLog(
                versionTitle = versionTitle,
                category = category,
                summary = summary,
                details = details
            )
        }
    }

    // Search and Filters for Products
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("Todos")
    val selectedTypeFilter = MutableStateFlow("Todos")
    val selectedMaturityFilter = MutableStateFlow<VintageStatus?>(null)
    val selectedSortOrder = MutableStateFlow(SortOrder.NAME)

    // Barcode Scanner State
    val scanMode = MutableStateFlow(ScanMode.CONSUME)
    val scanFeedback = MutableStateFlow<ScanFeedback?>(null)

    // Raw sources from repository dynamically bound to currentDatabaseName
    @OptIn(ExperimentalCoroutinesApi::class)
    val allWines: StateFlow<List<WineItem>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).allWines }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val lowStockWines: StateFlow<List<WineItem>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).lowStockWines }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val outOfStockWines: StateFlow<List<WineItem>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).outOfStockWines }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentTransactions: StateFlow<List<StockTransaction>> = currentDatabaseName
        .flatMapLatest { getRepositoryForDatabase(it).recentTransactions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow for Near Expiration items (Validade próxima <= 30 dias)
    val nearExpirationWines: StateFlow<List<WineItem>> = allWines
        .combine(MutableStateFlow(System.currentTimeMillis())) { all, now ->
            all.filter { it.isNearExpiration(30, now) || it.isExpired(now) }
                .sortedBy { it.getDaysUntilExpiration(now) ?: Int.MAX_VALUE }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Near Expiration Wines (com suporte a pesquisa por Código Interno, Código de Barras e Nome)
    val filteredNearExpirationWines: StateFlow<List<WineItem>> = combine(
        nearExpirationWines,
        expirationSearchQuery,
        expirationFilterType
    ) { list, query, filter ->
        val now = System.currentTimeMillis()
        list.filter { wine ->
            val cleanQuery = query.trim()
            val matchesQuery = cleanQuery.isBlank() ||
                    wine.id.toString() == cleanQuery.removePrefix("#") ||
                    wine.id.toString().contains(cleanQuery) ||
                    wine.barcode.contains(cleanQuery) ||
                    wine.name.containsNormalized(cleanQuery) ||
                    wine.category.containsNormalized(cleanQuery) ||
                    wine.location.containsNormalized(cleanQuery)

            val days = wine.getDaysUntilExpiration(now) ?: 999
            val matchesFilter = when (filter) {
                "VENCIDOS" -> days < 0
                "7_DIAS" -> days in 0..7
                "15_DIAS" -> days in 0..15
                "30_DIAS" -> days in 0..30
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setExpirationSearchQuery(query: String) {
        expirationSearchQuery.value = query
    }

    fun setExpirationFilterType(filter: String) {
        expirationFilterType.value = filter
    }

    fun updateCategoryMinStock(categoryName: String, minQuantity: Int, updateExistingProducts: Boolean = false) {
        viewModelScope.launch {
            val session = authSession.value
            repository.updateCategoryMinQuantity(categoryName, minQuantity, updateExistingProducts)
            repository.addAuditLog(
                actionType = "CONFIG_ESTOQUE",
                productName = "Tabela Padrão: $categoryName",
                category = categoryName,
                description = "Estoque mínimo padrão da categoria alterado para $minQuantity un (Atualizar produtos: $updateExistingProducts)",
                employeeName = session.employeeName,
                companyCnpj = session.companyCnpj,
                companyName = session.companyName,
                newValue = "$minQuantity un"
            )
        }
    }

    // Combined filtered & sorted wines flow
    val winesList: StateFlow<List<WineItem>> = combine(
        combine(allWines, searchQuery, selectedCategoryFilter) { all, query, categoryFilter ->
            Triple(all, query, categoryFilter)
        },
        combine(selectedTypeFilter, selectedMaturityFilter, selectedSortOrder) { typeFilter, maturityFilter, sort ->
            Triple(typeFilter, maturityFilter, sort)
        }
    ) { (all, query, categoryFilter), (typeFilter, maturityFilter, sort) ->
        all.filter { wine ->
            val matchesQuery = query.isBlank() ||
                    wine.name.containsNormalized(query) ||
                    wine.category.containsNormalized(query) ||
                    wine.producer.containsNormalized(query) ||
                    wine.grape.containsNormalized(query) ||
                    wine.region.containsNormalized(query) ||
                    wine.barcode.contains(query.trim()) ||
                    wine.notes.containsNormalized(query)

            val matchesCategory = categoryFilter == "Todos" ||
                    wine.category.equals(categoryFilter, ignoreCase = true)

            val matchesType = typeFilter == "Todos" ||
                    wine.wineType.equals(typeFilter, ignoreCase = true)

            val matchesMaturity = maturityFilter == null ||
                    wine.getVintageMaturityStatus() == maturityFilter

            matchesQuery && matchesCategory && matchesType && matchesMaturity
        }.sortedWith { a, b ->
            when (sort) {
                SortOrder.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                SortOrder.EXPIRATION -> {
                    val aExp = a.expirationDateMillis ?: Long.MAX_VALUE
                    val bExp = b.expirationDateMillis ?: Long.MAX_VALUE
                    if (aExp != bExp) aExp.compareTo(bExp) else a.name.compareTo(b.name, ignoreCase = true)
                }
                SortOrder.QUANTITY_ASC -> a.quantity.compareTo(b.quantity)
                SortOrder.QUANTITY_DESC -> b.quantity.compareTo(a.quantity)
                SortOrder.TROCA -> {
                    val aIsTroca = a.notes.contains("troca", ignoreCase = true) || a.isExpired() || a.isNearExpiration(30) || a.isOutOfStock
                    val bIsTroca = b.notes.contains("troca", ignoreCase = true) || b.isExpired() || b.isNearExpiration(30) || b.isOutOfStock
                    when {
                        aIsTroca && !bIsTroca -> -1
                        !aIsTroca && bIsTroca -> 1
                        else -> {
                            val aExp = a.expirationDateMillis ?: Long.MAX_VALUE
                            val bExp = b.expirationDateMillis ?: Long.MAX_VALUE
                            if (aExp != bExp) aExp.compareTo(bExp) else a.name.compareTo(b.name, ignoreCase = true)
                        }
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auth Operations
    fun login(
        companyCnpj: String,
        companyName: String,
        username: String,
        fullName: String,
        role: String = "Operador de Estoque"
    ) {
        val now = System.currentTimeMillis()
        val session = AuthSession(
            isLoggedIn = true,
            companyCnpj = companyCnpj,
            companyName = companyName,
            employeeUsername = username,
            employeeName = fullName,
            role = role,
            loginTimestamp = now
        )
        authSession.value = session

        // Save session in SharedPreferences
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("company_cnpj", companyCnpj)
            .putString("company_name", companyName)
            .putString("employee_username", username)
            .putString("employee_name", fullName)
            .putString("employee_role", role)
            .putLong("login_timestamp", now)
            .apply()

        // Also save/register to database
        viewModelScope.launch {
            repository.saveCompany(CompanyProfile(cnpj = companyCnpj, tradeName = companyName))
            repository.saveEmployee(
                EmployeeUser(
                    companyCnpj = companyCnpj,
                    username = username,
                    fullName = fullName,
                    passwordHash = "******",
                    role = role
                )
            )
            repository.addAuditLog(
                actionType = "LOGIN",
                productName = "Acesso ao Sistema",
                category = "Segurança",
                description = "Funcionário $fullName ($username - $role) iniciou sessão na empresa $companyName ($companyCnpj)",
                employeeName = fullName,
                companyCnpj = companyCnpj,
                companyName = companyName
            )
        }
    }

    fun logout() {
        val current = authSession.value
        viewModelScope.launch {
            repository.addAuditLog(
                actionType = "LOGOUT",
                productName = "Encerramento de Sessão",
                category = "Segurança",
                description = "Funcionário ${current.employeeName} encerrou a sessão no sistema",
                employeeName = current.employeeName,
                companyCnpj = current.companyCnpj,
                companyName = current.companyName
            )
        }
        authSession.value = AuthSession(isLoggedIn = false)
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    /**
     * Alterar a senha do usuário atualmente conectado dentro do perfil
     */
    fun changePassword(currentPass: String, newPass: String, callback: (Boolean, String) -> Unit) {
        val session = authSession.value
        if (!session.isLoggedIn) {
            callback(false, "Nenhum usuário conectado.")
            return
        }
        viewModelScope.launch {
            val employee = repository.getEmployee(session.companyCnpj, session.employeeUsername)
            if (employee == null) {
                // Se ainda não existir no banco, cria o registro com a nova senha
                repository.saveEmployee(
                    EmployeeUser(
                        companyCnpj = session.companyCnpj,
                        username = session.employeeUsername,
                        fullName = session.employeeName,
                        passwordHash = newPass,
                        role = session.role,
                        email = ""
                    )
                )
                repository.addAuditLog(
                    actionType = "SENHA_ALTERADA",
                    productName = "Segurança da Conta",
                    category = "Segurança",
                    description = "Senha do usuário ${session.employeeUsername} atualizada com sucesso.",
                    employeeName = session.employeeName,
                    companyCnpj = session.companyCnpj,
                    companyName = session.companyName
                )
                callback(true, "Senha alterada com sucesso!")
                return@launch
            }

            if (employee.passwordHash.isNotBlank() && employee.passwordHash != "******" && employee.passwordHash != currentPass) {
                callback(false, "Senha atual incorreta.")
                return@launch
            }

            repository.updateEmployeePassword(employee.id, newPass)
            repository.addAuditLog(
                actionType = "SENHA_ALTERADA",
                productName = "Segurança da Conta",
                category = "Segurança",
                description = "Senha do usuário ${session.employeeUsername} foi alterada pelo perfil.",
                employeeName = session.employeeName,
                companyCnpj = session.companyCnpj,
                companyName = session.companyName
            )
            callback(true, "Senha alterada com sucesso!")
        }
    }

    /**
     * Solicitação de recuperação de senha:
     * Busca o funcionário pelo e-mail ou username, e gera o código de segurança de 6 dígitos
     */
    fun requestPasswordReset(companyCnpj: String, identifierOrEmail: String, callback: (Boolean, String, String?, String?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getEmployeeByIdentifierOrEmail(companyCnpj.trim(), identifierOrEmail.trim())
                ?: repository.getEmployeeByEmail(identifierOrEmail.trim())

            if (user == null) {
                // Se não achou na empresa atual, verifica presets conhecidos
                if (identifierOrEmail.equals("carlos.silva", ignoreCase = true) ||
                    identifierOrEmail.equals("ana.gerente", ignoreCase = true) ||
                    identifierOrEmail.equals("admin", ignoreCase = true) ||
                    identifierOrEmail.contains("@")
                ) {
                    val fallbackEmail = if (identifierOrEmail.contains("@")) identifierOrEmail else "$identifierOrEmail@maxbebidas.com.br"
                    val code = (100000..999999).random().toString()
                    callback(true, "Instruções e código de redefinição enviados para $fallbackEmail.", fallbackEmail, code)
                    return@launch
                }
                callback(false, "Usuário ou e-mail não encontrado no cadastro.", null, null)
                return@launch
            }

            val targetEmail = if (user.email.isNotBlank()) user.email else "${user.username}@maxbebidas.com.br"
            val code = (100000..999999).random().toString()
            callback(true, "Instruções e código de redefinição enviados para $targetEmail.", targetEmail, code)
        }
    }

    /**
     * Confirmação da redefinição de senha com o código de verificação
     */
    fun confirmPasswordReset(companyCnpj: String, identifierOrEmail: String, newPassword: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getEmployeeByIdentifierOrEmail(companyCnpj.trim(), identifierOrEmail.trim())
                ?: repository.getEmployeeByEmail(identifierOrEmail.trim())

            if (user != null) {
                repository.updateEmployeePassword(user.id, newPassword)
                repository.addAuditLog(
                    actionType = "SENHA_REDEFINIDA",
                    productName = "Recuperação de Senha",
                    category = "Segurança",
                    description = "Senha do usuário ${user.username} redefinida via código de e-mail.",
                    employeeName = user.fullName,
                    companyCnpj = user.companyCnpj,
                    companyName = ""
                )
            } else {
                // Salvar novo registro de funcionário atualizado
                val uname = if (identifierOrEmail.contains("@")) identifierOrEmail.substringBefore("@") else identifierOrEmail
                repository.saveEmployee(
                    EmployeeUser(
                        companyCnpj = companyCnpj.ifBlank { "12.345.678/0001-90" },
                        username = uname,
                        fullName = uname.replace(".", " ").capitalize(),
                        passwordHash = newPassword,
                        role = "Operador de Estoque",
                        email = if (identifierOrEmail.contains("@")) identifierOrEmail else ""
                    )
                )
            }
            callback(true, "Senha redefinida com sucesso! Você já pode entrar com a nova senha.")
        }
    }

    // Audit Log Actions
    fun setLogSearchQuery(query: String) {
        logSearchQuery.value = query
    }

    fun setLogActionFilter(action: String) {
        selectedLogActionFilter.value = action
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAuditLogs()
        }
    }

    // User Actions
    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    val isAdmin: StateFlow<Boolean> = authSession
        .map { it.role.contains("Administrador", ignoreCase = true) || it.role.contains("ADM", ignoreCase = true) || it.role.contains("Gerente", ignoreCase = true) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun switchToAdminProfile(adminName: String = "Administrador Geral") {
        val current = authSession.value
        val updated = current.copy(
            employeeName = adminName,
            role = "Administrador"
        )
        authSession.value = updated
        prefs.edit()
            .putString("employee_name", adminName)
            .putString("employee_role", "Administrador")
            .apply()
    }

    fun switchToOperatorProfile() {
        val current = authSession.value
        val updated = current.copy(
            role = "Operador de Estoque"
        )
        authSession.value = updated
        prefs.edit()
            .putString("employee_role", "Operador de Estoque")
            .apply()
    }

    suspend fun verifyAdminPassword(password: String): Boolean {
        val cnpj = authSession.value.companyCnpj
        val adminUser = repository.getEmployee(cnpj, "admin")
        return if (adminUser != null) {
            adminUser.passwordHash == password.trim()
        } else {
            password.trim() == "admin123" || password.trim() == "admin"
        }
    }

    fun requestSwitchToAdmin(password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val valid = verifyAdminPassword(password)
            if (valid) {
                val adminUser = repository.getEmployee(authSession.value.companyCnpj, "admin")
                val adminName = adminUser?.fullName ?: "Administrador Geral"
                switchToAdminProfile(adminName)
                onResult(true, "Perfil de Administrador ativado com sucesso!")
            } else {
                onResult(false, "Senha de administrador incorreta! Tente novamente.")
            }
        }
    }

    suspend fun searchProductsOnlineByName(query: String): List<com.example.data.ProductOnlineInfo> {
        return com.example.data.OnlineProductLookupService.searchProductsByNameOnline(query)
    }

    fun acceptLog(log: AuditLog, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val adminName = authSession.value.employeeName
        viewModelScope.launch {
            val res = repository.acceptAuditLog(log.id, adminName)
            res.fold(
                onSuccess = { msg -> onResult(true, msg) },
                onFailure = { err -> onResult(false, err.message ?: "Erro ao aceitar alteração") }
            )
        }
    }

    fun revertLog(log: AuditLog, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val adminName = authSession.value.employeeName
        viewModelScope.launch {
            val res = repository.revertAuditLog(log.id, adminName)
            res.fold(
                onSuccess = { msg -> onResult(true, msg) },
                onFailure = { err -> onResult(false, err.message ?: "Erro ao reverter alteração") }
            )
        }
    }

    fun addCategoryConfig(categoryName: String, minQuantity: Int, unitDescription: String, notes: String = "") {
        viewModelScope.launch {
            repository.addCategoryConfig(
                CategoryConfig(
                    categoryName = categoryName.trim(),
                    minQuantity = minQuantity.coerceAtLeast(0),
                    unitDescription = unitDescription.trim().ifBlank { "unidades" },
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteCategoryConfig(categoryName: String) {
        viewModelScope.launch {
            repository.deleteCategoryConfig(categoryName)
        }
    }

    fun onCategoryFilterSelected(category: String) {
        selectedCategoryFilter.value = category
    }

    fun onTypeFilterSelected(type: String) {
        selectedTypeFilter.value = type
    }

    fun onMaturityFilterSelected(status: VintageStatus?) {
        selectedMaturityFilter.value = status
    }

    fun onSortOrderChanged(order: SortOrder) {
        selectedSortOrder.value = order
    }

    fun setScanMode(mode: ScanMode) {
        scanMode.value = mode
        scanFeedback.value = null
    }

    fun clearScanFeedback() {
        scanFeedback.value = null
    }

    /**
     * Processa um código de barras escaneado ou digitado
     */
    fun processBarcode(barcode: String, onNewBarcodeForRegistration: ((String) -> Unit)? = null) {
        val cleanCode = barcode.trim()
        if (cleanCode.isBlank()) return

        // Som de bip identificando o escaneamento do produto
        SoundFeedbackHelper.playScanBeep()

        val emp = authSession.value.employeeName
        val cnpj = authSession.value.companyCnpj
        val compName = authSession.value.companyName

        viewModelScope.launch {
            if (scanMode.value == ScanMode.CONSUME) {
                // Modo DAR BAIXA
                val (updatedWine, message) = repository.consumeByBarcode(
                    barcode = cleanCode,
                    employeeName = emp,
                    companyCnpj = cnpj,
                    companyName = compName
                )
                if (updatedWine != null) {
                    SoundFeedbackHelper.playSuccess()
                    scanFeedback.value = ScanFeedback(
                        title = "Baixa de Estoque Realizada!",
                        message = message,
                        isSuccess = true,
                        wineItem = updatedWine
                    )
                } else {
                    // Produto não cadastrado no banco de dados -> som de erro
                    SoundFeedbackHelper.playError()
                    scanFeedback.value = ScanFeedback(
                        title = "Produto Não Encontrado",
                        message = "O código de barras $cleanCode não pertence a nenhum produto cadastrado.",
                        isSuccess = false,
                        scannedCode = cleanCode
                    )
                }
            } else {
                // Modo CADASTRAR
                val wine = repository.getWineByBarcode(cleanCode)
                if (wine != null) {
                    // Já existe -> Incrementa estoque com sucesso
                    val updated = repository.adjustQuantity(
                        wine = wine,
                        delta = 1,
                        reason = "Adicionado +1 via leitor de código de barras ($cleanCode)",
                        employeeName = emp,
                        companyCnpj = cnpj,
                        companyName = compName
                    )
                    SoundFeedbackHelper.playSuccess()
                    scanFeedback.value = ScanFeedback(
                        title = "Estoque Incrementado!",
                        message = "Item '${wine.name}' encontrado. Quantidade atualizada para ${updated?.quantity ?: wine.quantity + 1} unidade(s).",
                        isSuccess = true,
                        wineItem = updated,
                        scannedCode = cleanCode
                    )
                } else {
                    // Não existe -> Notifica
                    scanFeedback.value = ScanFeedback(
                        title = "Novo Código Detectado!",
                        message = "Código $cleanCode não cadastrado. Clique no botão abaixo para cadastrar.",
                        isSuccess = true,
                        scannedCode = cleanCode
                    )
                    onNewBarcodeForRegistration?.invoke(cleanCode)
                }
            }
        }
    }

    fun clearAllProducts() {
        val emp = authSession.value.employeeName
        val cnpj = authSession.value.companyCnpj
        val compName = authSession.value.companyName
        viewModelScope.launch {
            repository.clearAllWines(employeeName = emp, companyCnpj = cnpj, companyName = compName)
        }
    }

    fun quickAdjustQuantity(wine: WineItem, delta: Int, reason: String = "Ajuste rápido no app") {
        val emp = authSession.value.employeeName
        val cnpj = authSession.value.companyCnpj
        val compName = authSession.value.companyName
        viewModelScope.launch {
            repository.adjustQuantity(
                wine = wine,
                delta = delta,
                reason = reason,
                employeeName = emp,
                companyCnpj = cnpj,
                companyName = compName
            )
            SoundFeedbackHelper.playSuccess()
        }
    }

    fun saveWine(wine: WineItem, onComplete: () -> Unit = {}) {
        val emp = authSession.value.employeeName
        val cnpj = authSession.value.companyCnpj
        val compName = authSession.value.companyName
        viewModelScope.launch {
            if (wine.id == 0) {
                repository.insertWine(
                    wine = wine,
                    employeeName = emp,
                    companyCnpj = cnpj,
                    companyName = compName
                )
            } else {
                repository.updateWine(
                    wine = wine,
                    employeeName = emp,
                    companyCnpj = cnpj,
                    companyName = compName
                )
            }
            SoundFeedbackHelper.playSuccess()
            onComplete()
        }
    }

    fun saveWinesBatch(wines: List<WineItem>, onComplete: () -> Unit = {}) {
        val emp = authSession.value.employeeName
        val cnpj = authSession.value.companyCnpj
        val compName = authSession.value.companyName
        viewModelScope.launch {
            wines.forEach { wine ->
                if (wine.id == 0) {
                    repository.insertWine(
                        wine = wine,
                        employeeName = emp,
                        companyCnpj = cnpj,
                        companyName = compName
                    )
                } else {
                    repository.updateWine(
                        wine = wine,
                        employeeName = emp,
                        companyCnpj = cnpj,
                        companyName = compName
                    )
                }
            }
            repository.addAuditLog(
                actionType = "IMPORTACAO_LOTE",
                productName = "${wines.size} Produtos em Lote",
                category = wines.firstOrNull()?.category ?: "Lote",
                description = "Importou lista com ${wines.size} produto(s) no estoque",
                employeeName = emp,
                companyCnpj = cnpj,
                companyName = compName
            )
            onComplete()
        }
    }

    fun deleteWine(wine: WineItem) {
        val emp = authSession.value.employeeName
        val cnpj = authSession.value.companyCnpj
        val compName = authSession.value.companyName
        viewModelScope.launch {
            repository.deleteWine(
                wine = wine,
                employeeName = emp,
                companyCnpj = cnpj,
                companyName = compName
            )
        }
    }

    suspend fun lookupProductOnline(barcode: String): com.example.data.ProductOnlineInfo? {
        return com.example.data.OnlineProductLookupService.lookupProductByBarcode(barcode)
    }
}
