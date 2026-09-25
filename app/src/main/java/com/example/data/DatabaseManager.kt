package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class DatabaseProfile(
    val id: String,
    val displayName: String,
    val description: String,
    val isCustom: Boolean = false
)

object DatabaseManager {
    private const val PREFS_NAME = "database_selection_prefs"
    private const val KEY_SELECTED_DB = "selected_database_id"
    private const val KEY_CUSTOM_DBS = "registered_custom_databases"

    val PREDEFINED_DATABASES = listOf(
        DatabaseProfile(
            id = AppDatabase.DEFAULT_DATABASE_NAME, // "adega_database"
            displayName = "Banco Principal (Matriz / Padrão)",
            description = "Banco padrão do sistema contendo os dados principais da matriz."
        ),
        DatabaseProfile(
            id = "adega_filial_db",
            displayName = "Banco Filial (Centro)",
            description = "Base de dados isolada para controle de estoque da filial."
        ),
        DatabaseProfile(
            id = "adega_deposito_db",
            displayName = "Banco Depósito (Almoxarifado)",
            description = "Base de dados dedicada ao centro de distribuição e reserva."
        ),
        DatabaseProfile(
            id = "adega_homologacao_db",
            displayName = "Banco de Testes (Homologação)",
            description = "Ambiente isolado para testes, treinamentos e simulações sem afetar a produção."
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedDatabaseName(context: Context): String {
        val selected = getPrefs(context).getString(KEY_SELECTED_DB, AppDatabase.DEFAULT_DATABASE_NAME)
        return if (selected.isNullOrBlank()) AppDatabase.DEFAULT_DATABASE_NAME else selected
    }

    fun setSelectedDatabaseName(context: Context, databaseName: String) {
        val sanitized = sanitizeDatabaseName(databaseName)
        getPrefs(context).edit().putString(KEY_SELECTED_DB, sanitized).apply()

        // If it's a custom DB not in predefined list, register it
        if (PREDEFINED_DATABASES.none { it.id == sanitized }) {
            addCustomDatabase(context, sanitized)
        }
    }

    fun getCustomDatabases(context: Context): List<String> {
        val raw = getPrefs(context).getStringSet(KEY_CUSTOM_DBS, emptySet()) ?: emptySet()
        return raw.toList().sorted()
    }

    fun addCustomDatabase(context: Context, databaseName: String) {
        val sanitized = sanitizeDatabaseName(databaseName)
        val currentSet = getPrefs(context).getStringSet(KEY_CUSTOM_DBS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(sanitized)
        getPrefs(context).edit().putStringSet(KEY_CUSTOM_DBS, currentSet).apply()
    }

    fun getAllAvailableDatabases(context: Context): List<DatabaseProfile> {
        val result = PREDEFINED_DATABASES.toMutableList()
        val customDbs = getCustomDatabases(context)
        for (custom in customDbs) {
            if (result.none { it.id == custom }) {
                result.add(
                    DatabaseProfile(
                        id = custom,
                        displayName = "Banco Personalizado: $custom",
                        description = "Banco de dados SQLite customizado criado pelo usuário.",
                        isCustom = true
                    )
                )
            }
        }
        return result
    }

    fun getDatabaseDisplayName(id: String): String {
        return PREDEFINED_DATABASES.find { it.id == id }?.displayName ?: "Personalizado ($id)"
    }

    fun sanitizeDatabaseName(rawName: String): String {
        var clean = rawName.trim().lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }

        if (clean.isBlank()) {
            clean = AppDatabase.DEFAULT_DATABASE_NAME
        }
        return clean
    }
}
