package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WineRepository(
    private val wineDao: WineDao,
    private val transactionDao: StockTransactionDao,
    private val auditLogDao: AuditLogDao,
    private val companyDao: CompanyDao,
    private val employeeDao: EmployeeDao,
    private val categoryConfigDao: CategoryConfigDao,
    private val devChangeLogDao: DevChangeLogDao
) {
    val allWines: Flow<List<WineItem>> = wineDao.getAllWines()
    val lowStockWines: Flow<List<WineItem>> = wineDao.getLowStockWines()
    val outOfStockWines: Flow<List<WineItem>> = wineDao.getOutOfStockWines()
    val recentTransactions: Flow<List<StockTransaction>> = transactionDao.getRecentTransactions()
    val allAuditLogs: Flow<List<AuditLog>> = auditLogDao.getAllLogs()
    val allCompanies: Flow<List<CompanyProfile>> = companyDao.getAllCompanies()
    val allCategoryConfigs: Flow<List<CategoryConfig>> = categoryConfigDao.getAllCategoryConfigs()
    val allDevChangeLogs: Flow<List<DevChangeLog>> = devChangeLogDao.getAllLogs()

    suspend fun addDevChangeLog(
        versionTitle: String,
        category: String,
        summary: String,
        details: String,
        developer: String = "Equipe Dev / AI Assistant"
    ) {
        devChangeLogDao.insertLog(
            DevChangeLog(
                versionTitle = versionTitle,
                category = category,
                summary = summary,
                details = details,
                developer = developer
            )
        )
    }

    suspend fun seedDefaultDevChangeLogsIfEmpty() {
        val existing = devChangeLogDao.getAllLogsSync()
        if (existing.isEmpty()) {
            val defaultLogs = listOf(
                DevChangeLog(
                    versionTitle = "v2.10 - Redimensionamento do Botão e Label 'Adicionar Unidades'",
                    category = "UI",
                    summary = "Redimensionamento e Destaque da Ação 'Adicionar Unidades' na Ficha do Produto",
                    details = "Ao clicar em qualquer produto para exibir sua descrição e detalhes de estoque, o botão de adição foi ampliado com altura tátil de 46dp, ícone de 22dp e label explícito 'Adicionar Unidades' com tipografia destacada em negrito (14sp), além de reorganização horizontal espaçada ao lado de 'Dar Baixa'.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis()
                ),
                DevChangeLog(
                    versionTitle = "v2.9 - Correção de Logs HWUI, Ashmem, Dataspace e FirebaseApp",
                    category = "SYSTEM",
                    summary = "Correção Integral de Avisos de HWUI, Ashmem, Dataspace e FirebaseApp",
                    details = "1) Suprimido FirebaseInitProvider transitivo no AndroidManifest para erradicar aviso de inicialização do FirebaseApp sem google-services.json. 2) Substituído asset 1024x1024 JFIF JPEG sem dataspace por PNG sRGB otimizado (256x256 com safe zone de 66dp no ícone adaptativo), eliminando 'Unknown dataspace 0', 'Image decoding logging dropped' e 'ashmem pinning deprecated'. 3) Configurado explicitamente ActivityInfo.COLOR_MODE_DEFAULT e PixelFormat.RGBA_8888 no tema e na Window para evitar tentativas de handshake EGL 10-bit não suportadas pelo driver virtual.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis()
                ),
                DevChangeLog(
                    versionTitle = "v2.8 - Limpeza de Labels de Safra & Destaque do Estoque Atual",
                    category = "UI",
                    summary = "Remoção de Rastreamento de Safra/Consumo e Redimensionamento do Estoque Atual",
                    details = "Removidos os cartões/rótulos 'Rastreamento da Safra' e 'Momento ideal para o consumo' na exibição de detalhes do produto. O bloco de 'Estoque Atual' foi redimensionado e ampliado com tipografia proeminente (20sp bold), ícone de armazém/estoque de 40dp, destaque numérico de 32sp e layout mais espaçoso e intuitivo.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis()
                ),
                DevChangeLog(
                    versionTitle = "v2.7 - Redimensionamento e Otimização de Ícones na Exibição do Produto",
                    category = "UI",
                    summary = "Redimensionamento e Interatividade de Ícones na Exibição do Produto",
                    details = "Redimensionamento geral de ícones no painel de detalhes do produto exibido ao clicar nele: ícone hero por categoria expansível (56dp a 84dp) ao clicar, botões de ação (Editar, Excluir, Fechar) com superfície de 40dp e ícones de 20dp, botões de baixa/adicionar com 20dp e linhas de atributos com ícones temáticos de 19dp.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis()
                ),
                DevChangeLog(
                    versionTitle = "v2.6 - Seleção Dinâmica de Banco de Dados & Otimização HWUI/Logs",
                    category = "DATABASE",
                    summary = "Campo Seletor de Banco de Dados de Armazenamento",
                    details = "Implementado seletor dinâmico de banco de dados SQLite/Room ('adega_database', 'estoque_filial_1', 'estoque_reserva', 'adega_backup' ou personalizado), permitindo alternar de banco com persistência e atualização reativa instantânea de toda a interface.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis()
                ),
                DevChangeLog(
                    versionTitle = "v2.6 - Seleção Dinâmica de Banco de Dados & Otimização HWUI/Logs",
                    category = "FIX",
                    summary = "Resolução de Avisos do Sistema (HWUI, EGL, Firebase e Ashmem)",
                    details = "Desativado Firebase desnecessário para evitar W/FirebaseApp, habilitada aceleração por hardware e formato RGBA_8888 de janela para evitar falha EGL de 101010-2 e dataspace 0, e otimizado decodificador de imagem Coil.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5
                ),
                DevChangeLog(
                    versionTitle = "v2.5 - Relatórios Dev em Banco, Aba Validades e Tabela de Índices",
                    category = "FEATURE",
                    summary = "Tabela de Relatórios de Alterações do Desenvolvedor (DevChangeLog)",
                    details = "Criação da tabela de banco de dados 'dev_changelogs' persistindo data, hora, versão, categoria, resumo e detalhes técnicos de cada alteração feita pelo dev.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis()
                ),
                DevChangeLog(
                    versionTitle = "v2.5 - Relatórios Dev em Banco, Aba Validades e Tabela de Índices",
                    category = "FIX",
                    summary = "Correção de avisos do OnBackInvokedCallback / WindowOnBackDispatcher",
                    details = "Ajuste e sincronização do despacho de retorno e encerramento de diálogos/sheets no Android 14+ (API 34/35).",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 10
                ),
                DevChangeLog(
                    versionTitle = "v2.5 - Relatórios Dev em Banco, Aba Validades e Tabela de Índices",
                    category = "UI",
                    summary = "Aba de Validades e Otimização para Smartphone",
                    details = "Nova aba dedicada de produtos a vencer com pesquisa inteligente por código de barras, ID e nome, além de leitor de câmera integrado e layout de alertas compacto.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30
                ),
                DevChangeLog(
                    versionTitle = "v2.5 - Relatórios Dev em Banco, Aba Validades e Tabela de Índices",
                    category = "DATABASE",
                    summary = "Tabela Padrão de Índices Mínimos por Categoria",
                    details = "Criação da entidade e DAO CategoryConfig no Room Database com sincronização automática com o formulário de cadastro de produtos.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60
                ),
                DevChangeLog(
                    versionTitle = "v2.4 - Sistema de Logs de Auditoria & Login Empresarial",
                    category = "FEATURE",
                    summary = "Auditoria Corporativa em Tempo Real e Autenticação em Duas Etapas",
                    details = "Rastreamento total de operações de estoque por funcionário e CNPJ da empresa com busca e filtros.",
                    developer = "Dev / AI Assistant",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24
                )
            )
            devChangeLogDao.insertAll(defaultLogs)
        }
    }

    suspend fun updateCategoryMinQuantity(categoryName: String, minQuantity: Int, updateExistingProducts: Boolean = false) {
        categoryConfigDao.updateMinQuantity(categoryName, minQuantity)
        if (updateExistingProducts) {
            categoryConfigDao.updateAllWinesMinQuantityForCategory(categoryName, minQuantity)
        }
    }

    suspend fun getCategoryMinQuantity(categoryName: String): Int {
        return categoryConfigDao.getByCategoryName(categoryName)?.minQuantity ?: 2
    }

    suspend fun seedDefaultCategoryConfigsIfEmpty() {
        val existing = categoryConfigDao.getAllCategoryConfigsSync()
        if (existing.isEmpty()) {
            val defaults = listOf(
                CategoryConfig("Mercearia", minQuantity = 5, unitDescription = "unidades", notes = "Alimentos, temperos, descartáveis e utilidades"),
                CategoryConfig("Vinhos", minQuantity = 3, unitDescription = "garrafas", notes = "Vinhos tintos, brancos, rosés e espumantes"),
                CategoryConfig("Destilados", minQuantity = 2, unitDescription = "garrafas", notes = "Whiskies, vodkas, gins, runs, cachaças e licores"),
                CategoryConfig("Salgadinhos", minQuantity = 8, unitDescription = "pacotes", notes = "Batatas, amendoins, snacks e petiscos"),
                CategoryConfig("Bomboniere", minQuantity = 10, unitDescription = "unidades", notes = "Chocolates, balas, gomas e doces"),
                CategoryConfig("Congelados", minQuantity = 4, unitDescription = "unidades", notes = "Petiscos, pizzas, hambúrgueres e sobremesas"),
                CategoryConfig("Gelos", minQuantity = 8, unitDescription = "sacos / pacotes", notes = "Gelo em cubo, britado e saborizado"),
                CategoryConfig("Cervejas", minQuantity = 12, unitDescription = "latas / garrafas", notes = "Cervejas artesanais, pilsen, puro malte e especiais"),
                CategoryConfig("Sucos e Diversos", minQuantity = 10, unitDescription = "unidades", notes = "Sucos naturais, águas, refrigerantes e energéticos"),
                CategoryConfig("Tabaco", minQuantity = 5, unitDescription = "maços / unidades", notes = "Cigarros, palheiros, sedas, isqueiros e tabacaria"),
                CategoryConfig("Produtos em Alteração", minQuantity = 2, unitDescription = "unidades", notes = "Produtos em ajuste ou validação de cadastro")
            )
            categoryConfigDao.insertAll(defaults)
        }
    }

    suspend fun addCategoryConfig(config: CategoryConfig) {
        categoryConfigDao.insertOrUpdate(config)
    }

    suspend fun deleteCategoryConfig(categoryName: String) {
        categoryConfigDao.deleteCategory(categoryName)
    }

    fun searchLogs(query: String): Flow<List<AuditLog>> {
        return if (query.isBlank()) {
            auditLogDao.getAllLogs()
        } else {
            auditLogDao.searchLogs(query.trim())
        }
    }

    suspend fun clearAuditLogs() {
        auditLogDao.clearAllLogs()
    }

    suspend fun addAuditLog(
        actionType: String,
        productName: String,
        category: String,
        description: String,
        employeeName: String,
        companyCnpj: String,
        companyName: String,
        oldValue: String? = null,
        newValue: String? = null,
        affectedWineId: Int? = null,
        wineSnapshotJson: String? = null
    ) {
        auditLogDao.insertLog(
            AuditLog(
                actionType = actionType,
                productName = productName,
                category = category,
                description = description,
                employeeName = employeeName,
                companyCnpj = companyCnpj,
                companyName = companyName,
                oldValue = oldValue,
                newValue = newValue,
                status = "PENDENTE",
                affectedWineId = affectedWineId,
                wineSnapshotJson = wineSnapshotJson
            )
        )
    }

    suspend fun acceptAuditLog(logId: Long, adminName: String): Result<String> {
        val log = auditLogDao.getLogById(logId) ?: return Result.failure(Exception("Log não encontrado"))
        auditLogDao.updateLogStatus(
            logId = logId,
            status = "ACEITA",
            reviewedBy = adminName,
            reviewedAt = System.currentTimeMillis()
        )
        return Result.success("Modificação '${log.productName}' aprovada por $adminName.")
    }

    suspend fun revertAuditLog(logId: Long, adminName: String): Result<String> {
        val log = auditLogDao.getLogById(logId) ?: return Result.failure(Exception("Log não encontrado"))
        if (log.isReverted) return Result.failure(Exception("Esta alteração já foi revertida anteriormente."))

        val targetWineId = log.affectedWineId
        val snapshot = log.wineSnapshotJson?.let { wineItemFromJson(it) }

        try {
            when (log.actionType) {
                "CADASTRO" -> {
                    // Reverter cadastro: remove o item do banco de dados
                    if (targetWineId != null) {
                        wineDao.deleteWineById(targetWineId)
                    } else if (snapshot != null && snapshot.barcode.isNotBlank()) {
                        val found = wineDao.getWineByBarcode(snapshot.barcode)
                        if (found != null) wineDao.deleteWine(found)
                    }
                }
                "EXCLUSAO" -> {
                    // Reverter exclusão: insere o item de volta
                    if (snapshot != null) {
                        wineDao.insertWine(snapshot.copy(id = 0))
                    }
                }
                "EDICAO" -> {
                    // Reverter edição: restaura o estado anterior
                    if (snapshot != null) {
                        wineDao.updateWine(snapshot)
                    }
                }
                "ENTRADA_ESTOQUE", "SAIDA_ESTOQUE" -> {
                    // Reverter ajuste de estoque: restaura a quantidade anterior
                    if (snapshot != null) {
                        wineDao.updateQuantity(snapshot.id, snapshot.quantity)
                    }
                }
            }

            auditLogDao.updateLogStatus(
                logId = logId,
                status = "REVERTIDA",
                reviewedBy = adminName,
                reviewedAt = System.currentTimeMillis()
            )

            addAuditLog(
                actionType = "REVERSAO_ADM",
                productName = log.productName,
                category = log.category,
                description = "Administrador $adminName reverteu a modificação '${log.actionType}' de ${log.employeeName} (${log.description})",
                employeeName = adminName,
                companyCnpj = log.companyCnpj,
                companyName = log.companyName,
                oldValue = log.newValue,
                newValue = log.oldValue
            )

            return Result.success("Modificação revertida com sucesso no banco de dados!")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun searchWines(query: String): Flow<List<WineItem>> {
        return if (query.isBlank()) {
            wineDao.getAllWines()
        } else {
            wineDao.searchWines(query)
        }
    }

    suspend fun getWineById(id: Int): WineItem? = wineDao.getWineById(id)

    suspend fun getWineByBarcode(barcode: String): WineItem? {
        if (barcode.isBlank()) return null
        return wineDao.getWineByBarcode(barcode.trim())
    }

    suspend fun insertWine(
        wine: WineItem,
        employeeName: String = "Operador",
        companyCnpj: String = "00.000.000/0001-00",
        companyName: String = "Empresa"
    ): Long {
        val id = wineDao.insertWine(wine)
        val savedWine = wine.copy(id = id.toInt())
        transactionDao.insertTransaction(
            StockTransaction(
                wineId = id.toInt(),
                wineName = wine.name,
                type = "ADD",
                quantityChange = wine.quantity,
                newQuantity = wine.quantity,
                reason = "Cadastro inicial / Adicionado ao estoque"
            )
        )
        addAuditLog(
            actionType = "CADASTRO",
            productName = wine.name,
            category = wine.category,
            description = "Cadastrou novo produto com ${wine.quantity} un. em estoque na seção '${wine.category}'" +
                    if (wine.hasExpirationDate) " (Validade: ${wine.getFormattedExpirationDate()})" else "",
            employeeName = employeeName,
            companyCnpj = companyCnpj,
            companyName = companyName,
            newValue = "Qtd: ${wine.quantity}, Seção: ${wine.category}",
            affectedWineId = id.toInt(),
            wineSnapshotJson = savedWine.toJsonString()
        )
        return id
    }

    suspend fun updateWine(
        wine: WineItem,
        employeeName: String = "Operador",
        companyCnpj: String = "00.000.000/0001-00",
        companyName: String = "Empresa",
        changeSummary: String = "Edição de dados do produto"
    ) {
        val existing = wineDao.getWineById(wine.id)
        wineDao.updateWine(wine)

        val desc = if (existing != null && existing.quantity != wine.quantity) {
            "Editou produto: quantidade alterada de ${existing.quantity} para ${wine.quantity} un. ($changeSummary)"
        } else {
            "Atualizou informações do produto '${wine.name}' ($changeSummary)"
        }

        addAuditLog(
            actionType = "EDICAO",
            productName = wine.name,
            category = wine.category,
            description = desc,
            employeeName = employeeName,
            companyCnpj = companyCnpj,
            companyName = companyName,
            oldValue = existing?.let { "Qtd: ${it.quantity}, Seção: ${it.category}" },
            newValue = "Qtd: ${wine.quantity}, Seção: ${wine.category}",
            affectedWineId = wine.id,
            wineSnapshotJson = existing?.toJsonString()
        )
    }

    suspend fun deleteWine(
        wine: WineItem,
        employeeName: String = "Operador",
        companyCnpj: String = "00.000.000/0001-00",
        companyName: String = "Empresa"
    ) {
        wineDao.deleteWine(wine)
        addAuditLog(
            actionType = "EXCLUSAO",
            productName = wine.name,
            category = wine.category,
            description = "Excluiu produto do estoque (Tinha ${wine.quantity} un. em estoque)",
            employeeName = employeeName,
            companyCnpj = companyCnpj,
            companyName = companyName,
            oldValue = "Qtd: ${wine.quantity}, EAN: ${wine.barcode}",
            affectedWineId = wine.id,
            wineSnapshotJson = wine.toJsonString()
        )
    }

    suspend fun clearAllWines(
        employeeName: String = "Operador",
        companyCnpj: String = "00.000.000/0001-00",
        companyName: String = "Empresa"
    ) {
        wineDao.deleteAllWines()
        addAuditLog(
            actionType = "LIMPEZA_ESTOQUE",
            productName = "Todos os Produtos",
            category = "Geral",
            description = "Limpou e apagou todos os registros do banco de dados de estoque",
            employeeName = employeeName,
            companyCnpj = companyCnpj,
            companyName = companyName
        )
    }

    /**
     * Altera o estoque de um vinho (Ex: Baixa via leitor de código de barras ou manual).
     */
    suspend fun adjustQuantity(
        wine: WineItem,
        delta: Int,
        reason: String,
        employeeName: String = "Operador",
        companyCnpj: String = "00.000.000/0001-00",
        companyName: String = "Empresa"
    ): WineItem? {
        val newQty = (wine.quantity + delta).coerceAtLeast(0)
        if (newQty == wine.quantity) return wine

        val updatedWine = wine.copy(quantity = newQty)
        wineDao.updateWine(updatedWine)

        val transType = if (delta < 0) "CONSUME" else "ADD"
        transactionDao.insertTransaction(
            StockTransaction(
                wineId = wine.id,
                wineName = wine.name,
                type = transType,
                quantityChange = delta,
                newQuantity = newQty,
                reason = reason
            )
        )

        val action = if (delta > 0) "ENTRADA_ESTOQUE" else "SAIDA_ESTOQUE"
        val changeText = if (delta > 0) "+$delta" else "$delta"
        addAuditLog(
            actionType = action,
            productName = wine.name,
            category = wine.category,
            description = "$changeText un. ($reason) • Estoque atual: $newQty un.",
            employeeName = employeeName,
            companyCnpj = companyCnpj,
            companyName = companyName,
            oldValue = "Qtd: ${wine.quantity}",
            newValue = "Qtd: $newQty",
            affectedWineId = wine.id,
            wineSnapshotJson = wine.toJsonString()
        )
        return updatedWine
    }

    /**
     * Tenta dar baixa de 1 unidade via código de barras.
     */
    suspend fun consumeByBarcode(
        barcode: String,
        employeeName: String = "Operador",
        companyCnpj: String = "00.000.000/0001-00",
        companyName: String = "Empresa"
    ): Pair<WineItem?, String> {
        val cleanBarcode = barcode.trim()
        val wine = wineDao.getWineByBarcode(cleanBarcode)
            ?: return Pair(null, "Nenhum produto encontrado com o código de barras $cleanBarcode.")

        if (wine.quantity <= 0) {
            return Pair(wine, "Atenção: '${wine.name}' já está fora de estoque (0 garrafas)!")
        }

        val updated = adjustQuantity(
            wine = wine,
            delta = -1,
            reason = "Baixa rápida via Leitor Barcode ($cleanBarcode)",
            employeeName = employeeName,
            companyCnpj = companyCnpj,
            companyName = companyName
        )
        return Pair(updated, "Baixa registrada! Restam ${updated?.quantity ?: 0} unidade(s) de ${wine.name}.")
    }

    // Company & Employee Auth Helpers
    suspend fun getCompanyByCnpj(cnpj: String): CompanyProfile? = companyDao.getCompanyByCnpj(cnpj)

    suspend fun saveCompany(company: CompanyProfile) = companyDao.insertCompany(company)

    fun getEmployeesForCompany(cnpj: String): Flow<List<EmployeeUser>> = employeeDao.getEmployeesForCompany(cnpj)

    suspend fun getEmployee(cnpj: String, username: String): EmployeeUser? = employeeDao.getEmployee(cnpj, username)

    suspend fun getEmployeeByIdentifierOrEmail(cnpj: String, identifier: String): EmployeeUser? =
        employeeDao.getEmployeeByIdentifierOrEmail(cnpj, identifier)

    suspend fun getEmployeeByEmail(email: String): EmployeeUser? =
        employeeDao.getEmployeeByEmail(email)

    suspend fun updateEmployeePassword(id: Long, newPassword: String) =
        employeeDao.updatePassword(id, newPassword)

    suspend fun saveEmployee(employee: EmployeeUser) = employeeDao.insertEmployee(employee)

    /**
     * Preenche dados de exemplo abrangendo todas as categorias na primeira inicialização.
     */
    suspend fun prepopulateSampleDataIfEmpty() {
        val existing = wineDao.getAllWines().first()
        if (existing.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayInMillis = 24 * 3600 * 1000L

            val samples = listOf(
                // VINHOS
                WineItem(
                    name = "Catena Zapata Malbec",
                    category = "Vinhos",
                    wineType = "Tinto",
                    vintage = 2020,
                    producer = "Catena Zapata",
                    region = "Mendoza, Argentina",
                    grape = "Malbec",
                    quantity = 4,
                    minQuantity = 2,
                    barcode = "7891234567890",
                    location = "Adega Climatizada",
                    rating = 4.8f,
                    drinkingWindowStart = 2023,
                    drinkingWindowEnd = 2030,
                    price = 280.00,
                    notes = "Taninos aveludados, aromas de ameixa negra e baunilha."
                ),
                WineItem(
                    name = "Château Margaux Premier Grand Cru",
                    category = "Vinhos",
                    wineType = "Tinto",
                    vintage = 2015,
                    producer = "Château Margaux",
                    region = "Bordeaux, França",
                    grape = "Cabernet Sauvignon",
                    quantity = 1,
                    minQuantity = 2,
                    barcode = "7898901234567",
                    location = "Nicho Reservado 01",
                    rating = 5.0f,
                    drinkingWindowStart = 2025,
                    drinkingWindowEnd = 2045,
                    price = 4500.00,
                    notes = "Notas profundas de cedro, cassis e especiarias."
                ),

                // CERVEJA (Alguns próximos à validade)
                WineItem(
                    name = "Cerveja Heineken Premium Long Neck 330ml",
                    category = "Cerveja",
                    wineType = "Pilsen / Lager",
                    vintage = 2026,
                    producer = "Heineken",
                    quantity = 12,
                    minQuantity = 6,
                    barcode = "7891050001201",
                    location = "Geladeira 01",
                    price = 7.50,
                    expirationDateMillis = now + (5 * dayInMillis) // Vence em 5 dias!
                ),
                WineItem(
                    name = "Cerveja Artesanal IPA Colorado Indica 600ml",
                    category = "Cerveja",
                    wineType = "IPA",
                    vintage = 2026,
                    producer = "Cervejaria Colorado",
                    quantity = 3,
                    minQuantity = 2,
                    barcode = "7898215430012",
                    location = "Prateleira Cervejas",
                    price = 18.90,
                    expirationDateMillis = now - (2 * dayInMillis) // VENCIDO há 2 dias!
                ),

                // DESTILADOS
                WineItem(
                    name = "Whisky Johnnie Walker Black Label 12 Anos 1L",
                    category = "Destilados",
                    wineType = "Scotch Whisky",
                    vintage = 2025,
                    producer = "Johnnie Walker",
                    quantity = 2,
                    minQuantity = 1,
                    barcode = "5000267014005",
                    location = "Bar Principal",
                    rating = 4.7f,
                    price = 169.90,
                    notes = "Rico, defumado e macio. Blend icônico de maltes."
                ),
                WineItem(
                    name = "Gin Tanqueray London Dry 750ml",
                    category = "Destilados",
                    wineType = "Gin",
                    vintage = 2025,
                    producer = "Tanqueray",
                    quantity = 1, // Baixo estoque
                    minQuantity = 2,
                    barcode = "5000267023816",
                    location = "Bar Principal",
                    rating = 4.6f,
                    price = 135.00
                ),

                // SUCOS E AGUA
                WineItem(
                    name = "Suco Integral de Laranja Prats 900ml",
                    category = "Sucos e Agua",
                    wineType = "Suco Natural",
                    quantity = 2,
                    minQuantity = 3,
                    barcode = "7898912345001",
                    location = "Geladeira 02",
                    price = 12.90,
                    expirationDateMillis = now + (3 * dayInMillis) // Vence em 3 dias!
                ),
                WineItem(
                    name = "Água Mineral com Gás Perrier 330ml",
                    category = "Sucos e Agua",
                    wineType = "Água Mineral",
                    quantity = 15,
                    minQuantity = 5,
                    barcode = "3074660000012",
                    location = "Despensa",
                    price = 9.90
                ),

                // MERCEARIA
                WineItem(
                    name = "Azeite de Oliva Extra Virgem Português 500ml",
                    category = "Mercearia",
                    wineType = "Azeite / Condimento",
                    quantity = 3,
                    minQuantity = 1,
                    barcode = "5601234567891",
                    location = "Despensa A",
                    price = 42.00,
                    expirationDateMillis = now + (18 * dayInMillis) // Vence em 18 dias!
                ),
                WineItem(
                    name = "Azeitona Preta Azapa Recheada 300g",
                    category = "Mercearia",
                    wineType = "Conserva",
                    quantity = 1,
                    minQuantity = 2,
                    barcode = "7896012345678",
                    location = "Despensa A",
                    price = 22.50,
                    expirationDateMillis = now + (10 * dayInMillis) // Vence em 10 dias!
                ),

                // CONGELADOS
                WineItem(
                    name = "Petybon Lasanha Bolonhesa 600g",
                    category = "Congelados",
                    wineType = "Prato Congelado",
                    quantity = 2,
                    minQuantity = 2,
                    barcode = "7891230004561",
                    location = "Freezer 01",
                    price = 24.90,
                    expirationDateMillis = now + (25 * dayInMillis) // Vence em 25 dias!
                ),

                // GELOS
                WineItem(
                    name = "Saco de Gelo em Cubos Filtrado 5kg",
                    category = "Gelos",
                    wineType = "Gelo em Cubos",
                    quantity = 4,
                    minQuantity = 2,
                    barcode = "7890000000111",
                    location = "Freezer Gelo",
                    price = 15.00
                ),
                WineItem(
                    name = "Gelo Saborizado Frutas Vermelhas p/ Gin",
                    category = "Gelos",
                    wineType = "Gelo Saborizado",
                    quantity = 6,
                    minQuantity = 3,
                    barcode = "7890000000222",
                    location = "Freezer Gelo",
                    price = 8.00,
                    expirationDateMillis = now + (45 * dayInMillis)
                )
            )

            for (w in samples) {
                insertWine(w)
            }
        }
    }
}
