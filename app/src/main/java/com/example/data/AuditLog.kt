package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // "CADASTRO", "EDICAO", "EXCLUSAO", "ENTRADA_ESTOQUE", "SAIDA_ESTOQUE", "IMPORTACAO_LOTE", "LIMPEZA_ESTOQUE", "LEITOR_BARCODE", "SISTEMA", "REVERSAO_ADM"
    val productName: String,
    val category: String = "",
    val description: String,
    val employeeName: String = "Funcionário Padrão",
    val companyCnpj: String = "00.000.000/0001-00",
    val companyName: String = "Empresa",
    val oldValue: String? = null,
    val newValue: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDENTE", // "PENDENTE", "ACEITA", "REVERTIDA"
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val affectedWineId: Int? = null,
    val wineSnapshotJson: String? = null // Backup serializado para reversão precisa de dados
) {
    val isPending: Boolean
        get() = status.equals("PENDENTE", ignoreCase = true)

    val isAccepted: Boolean
        get() = status.equals("ACEITA", ignoreCase = true)

    val isReverted: Boolean
        get() = status.equals("REVERTIDA", ignoreCase = true)

    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getFormattedReviewDateTime(): String? {
        val rAt = reviewedAt ?: return null
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(rAt))
    }
}
