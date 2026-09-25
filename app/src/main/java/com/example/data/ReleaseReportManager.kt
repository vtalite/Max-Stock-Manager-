package com.example.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReleaseReport(
    val id: String,
    val title: String,
    val date: String,
    val items: List<String>
)

object ReleaseReportManager {

    private const val FILE_NAME = "release_reports.json"
    private const val PREFS_NAME = "release_report_prefs"
    private const val KEY_DONT_SHOW_ON_STARTUP = "dont_show_on_startup"

    fun shouldShowOnStartup(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_DONT_SHOW_ON_STARTUP, false)
    }

    fun setDontShowOnStartup(context: Context, dontShow: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DONT_SHOW_ON_STARTUP, dontShow).apply()
    }

    fun getOrInitReleaseReports(context: Context): List<ReleaseReport> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            val defaultReports = getInitialReports()
            saveReportsToFile(context, defaultReports)
            return defaultReports
        }

        return try {
            val jsonString = file.readText()
            val reports = parseReportsJson(jsonString)
            if (reports.isEmpty()) {
                val defaultReports = getInitialReports()
                saveReportsToFile(context, defaultReports)
                defaultReports
            } else {
                reports
            }
        } catch (e: Exception) {
            Log.e("ReleaseReportManager", "Error reading reports file: ${e.message}")
            val defaultReports = getInitialReports()
            saveReportsToFile(context, defaultReports)
            defaultReports
        }
    }

    fun saveNewReport(context: Context, title: String, items: List<String>) {
        val current = getOrInitReleaseReports(context).toMutableList()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val newReport = ReleaseReport(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            date = sdf.format(Date()),
            items = items
        )
        current.add(0, newReport)
        saveReportsToFile(context, current)
    }

    private fun saveReportsToFile(context: Context, reports: List<ReleaseReport>) {
        try {
            val jsonArray = JSONArray()
            for (report in reports) {
                val obj = JSONObject()
                obj.put("id", report.id)
                obj.put("title", report.title)
                obj.put("date", report.date)
                val itemsArray = JSONArray()
                report.items.forEach { itemsArray.put(it) }
                obj.put("items", itemsArray)
                jsonArray.put(obj)
            }
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            Log.e("ReleaseReportManager", "Error saving reports file: ${e.message}")
        }
    }

    private fun parseReportsJson(jsonString: String): List<ReleaseReport> {
        val list = mutableListOf<ReleaseReport>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.optString("id", java.util.UUID.randomUUID().toString())
            val title = obj.optString("title", "Atualização")
            val date = obj.optString("date", "")
            val itemsArray = obj.optJSONArray("items")
            val items = mutableListOf<String>()
            if (itemsArray != null) {
                for (j in 0 until itemsArray.length()) {
                    items.add(itemsArray.getString(j))
                }
            }
            list.add(ReleaseReport(id, title, date, items))
        }
        return list
    }

    private fun getInitialReports(): List<ReleaseReport> {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        return listOf(
            ReleaseReport(
                id = "v2.4",
                title = "Versão 2.4 - Sistema de Logs de Auditoria & Login Empresarial (CNPJ + Funcionário)",
                date = currentDate,
                items = listOf(
                    "📜 Armazenamento de Modificações em Logs: Todas as operações (cadastros, edições, baixas, exclusões, importações em lote e limpezas) agora são salvas automaticamente em logs com data/hora, funcionário e CNPJ da empresa.",
                    "🕒 Ícone de Logs no Canto Superior: Botão dedicado na barra superior com badge de contagem em tempo real, permitindo pesquisar e filtrar todo o histórico por ação, produto ou operador.",
                    "🏢 Login Empresarial por CNPJ: Autenticação estruturada em duas etapas - primeiro o CNPJ e Razão Social da Empresa, seguido pelo Login e Senha do Funcionário.",
                    "👤 Rastreabilidade Total: Cada alteração no estoque é vinculada ao funcionário logado, garantindo segurança e transparência corporativa.",
                    "🔎 Pesquisa Inteligente na Tabela: Continuidade da pesquisa em tempo real com sugestões de nomes encontradas no banco de dados."
                )
            ),
            ReleaseReport(
                id = "v2.3",
                title = "Versão 2.3 - Pesquisa Inteligente na Tabela de Dados",
                date = currentDate,
                items = listOf(
                    "🔎 Pesquisa Instantânea na Tabela de Dados: Ao digitar no campo de busca, o sistema pesquisa todos os produtos no banco de dados e exibe todos os nomes possíveis que contêm as letras digitadas.",
                    "🏷️ Sugestões Rápidas em Chips: Exibição visual de todos os nomes correspondentes para preenchimento ou filtragem com 1 toque.",
                    "🌐 Busca Abrangente e Sem Acentos: Localiza itens desconsiderando acentos e maiúsculas/minúsculas em todas as seções.",
                    "✨ Formulário Otimizado: Removidos campos redundantes do cadastro para maior agilidade.",
                    "✅ Confirmação e Validade: Suporte a data de validade com máscara e feedback de sucesso no cadastro."
                )
            )
        )
    }
}
