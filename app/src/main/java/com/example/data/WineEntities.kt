package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wines")
data class WineItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String = "Vinhos", // "Vinhos", "Cerveja", "Destilados", "Sucos e Agua", "Mercearia", "Congelados", "Gelos"
    val wineType: String = "Tinto", // "Tinto", "Branco", "Pilsen", "Whisky", "Natural", etc.
    val vintage: Int = 2024, // e.g., 2018
    val producer: String = "",
    val region: String = "",
    val grape: String = "",
    val quantity: Int = 1,
    val minQuantity: Int = 2, // Limite para alerta de reposição
    val barcode: String = "",
    val location: String = "Adega Principal", // e.g. "Prateleira A1", "Freezer", "Despensa"
    val rating: Float = 4.0f,
    val drinkingWindowStart: Int? = null,
    val drinkingWindowEnd: Int? = null,
    val price: Double? = null,
    val notes: String = "",
    val imageUrl: String? = null,
    val expirationDateMillis: Long? = null, // Data de validade (Timestamp)
    val packagingType: String = "", // "Garrafa", "Caixa", "Pacote", "Lata", "Fardo", "Unidade"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity <= minQuantity && quantity > 0

    val isOutOfStock: Boolean
        get() = quantity == 0

    val hasExpirationDate: Boolean
        get() = expirationDateMillis != null

    fun getEffectivePackagingUnit(plural: Boolean = false): String {
        val custom = packagingType.trim()
        val base = if (custom.isNotBlank()) {
            custom.lowercase()
        } else {
            val lowerName = name.lowercase()
            when {
                lowerName.contains("caixa") || lowerName.contains(" cx") || lowerName.startsWith("cx ") -> "caixa"
                lowerName.contains("pacote") || lowerName.contains(" pct") || lowerName.startsWith("pct ") -> "pacote"
                lowerName.contains("fardo") -> "fardo"
                lowerName.contains("lata") -> "lata"
                lowerName.contains("garrafa") || lowerName.contains(" gf") -> "garrafa"
                lowerName.contains("saco") -> "pacote"
                lowerName.contains("pote") -> "pote"
                lowerName.contains("kit") -> "kit"
                category.equals("Vinhos", ignoreCase = true) -> "garrafa"
                category.equals("Destilados", ignoreCase = true) -> "garrafa"
                category.equals("Cerveja", ignoreCase = true) -> if (lowerName.contains("lata")) "lata" else "garrafa"
                category.equals("Gelos", ignoreCase = true) -> "pacote"
                category.equals("Mercearia", ignoreCase = true) -> if (lowerName.contains("caixa")) "caixa" else "pacote"
                category.equals("Congelados", ignoreCase = true) -> "pacote"
                else -> "unidade"
            }
        }

        if (!plural) return base

        return when (base) {
            "garrafa" -> "garrafas"
            "caixa" -> "caixas"
            "pacote" -> "pacotes"
            "lata" -> "latas"
            "fardo" -> "fardos"
            "saco" -> "sacos"
            "pote" -> "potes"
            "kit" -> "kits"
            "unidade" -> "unidades"
            else -> if (base.endsWith("s")) base else "${base}s"
        }
    }

    fun getPackagingTypeLabel(): String {
        return getEffectivePackagingUnit(plural = false).replaceFirstChar { it.uppercase() }
    }

    fun getDaysUntilExpiration(now: Long = System.currentTimeMillis()): Int? {
        val exp = expirationDateMillis ?: return null
        val diff = exp - now
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun isNearExpiration(daysThreshold: Int = 30, now: Long = System.currentTimeMillis()): Boolean {
        val days = getDaysUntilExpiration(now) ?: return false
        return days <= daysThreshold
    }

    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        val days = getDaysUntilExpiration(now) ?: return false
        return days < 0
    }

    fun getFormattedExpirationDate(): String {
        val millis = expirationDateMillis ?: return "Sem validade"
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }

    /**
     * Retorna o status de maturidade baseado na safra e no ano atual.
     */
    fun getVintageMaturityStatus(currentYear: Int = 2026): VintageStatus {
        val start = drinkingWindowStart ?: (vintage + 2)
        val end = drinkingWindowEnd ?: (vintage + 12)

        return when {
            currentYear < start -> VintageStatus.IN_CELLAR_GUARD // Em Guarda / Jovem
            currentYear in start..end -> VintageStatus.PEAK_DRINKING // No Apogeu / Pronto
            currentYear > end -> VintageStatus.PAST_PEAK // Passou do Apogeu
            else -> VintageStatus.PEAK_DRINKING
        }
    }
}

enum class VintageStatus(
    val label: String,
    val description: String
) {
    IN_CELLAR_GUARD("Em Guarda", "Ainda evoluindo. Pode guardar mais tempo."),
    PEAK_DRINKING("No Apogeu", "Momento ideal para consumo!"),
    PAST_PEAK("Consumir Rápido", "Passou do tempo ideal de guarda."),
}

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wineId: Int,
    val wineName: String,
    val type: String, // "ADD", "CONSUME", "ADJUST"
    val quantityChange: Int, // e.g. -1, +2
    val newQuantity: Int,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

fun WineItem.toJsonString(): String {
    val json = org.json.JSONObject()
    json.put("id", id)
    json.put("name", name)
    json.put("category", category)
    json.put("wineType", wineType)
    json.put("vintage", vintage)
    json.put("producer", producer)
    json.put("region", region)
    json.put("grape", grape)
    json.put("quantity", quantity)
    json.put("minQuantity", minQuantity)
    json.put("barcode", barcode)
    json.put("location", location)
    json.put("rating", rating.toDouble())
    if (price != null) json.put("price", price)
    json.put("notes", notes)
    if (imageUrl != null) json.put("imageUrl", imageUrl)
    if (expirationDateMillis != null) json.put("expirationDateMillis", expirationDateMillis)
    json.put("packagingType", packagingType)
    json.put("createdAt", createdAt)
    return json.toString()
}

fun wineItemFromJson(jsonStr: String): WineItem? {
    return try {
        val json = org.json.JSONObject(jsonStr)
        WineItem(
            id = json.optInt("id", 0),
            name = json.optString("name", "Produto"),
            category = json.optString("category", "Vinhos"),
            wineType = json.optString("wineType", "Padrão"),
            vintage = json.optInt("vintage", 2024),
            producer = json.optString("producer", ""),
            region = json.optString("region", ""),
            grape = json.optString("grape", ""),
            quantity = json.optInt("quantity", 0),
            minQuantity = json.optInt("minQuantity", 2),
            barcode = json.optString("barcode", ""),
            location = json.optString("location", "Adega Principal"),
            rating = json.optDouble("rating", 4.0).toFloat(),
            price = if (json.has("price")) json.optDouble("price", 0.0) else null,
            notes = json.optString("notes", ""),
            imageUrl = if (json.has("imageUrl")) json.optString("imageUrl").ifBlank { null } else null,
            expirationDateMillis = if (json.has("expirationDateMillis")) json.optLong("expirationDateMillis") else null,
            packagingType = json.optString("packagingType", ""),
            createdAt = json.optLong("createdAt", System.currentTimeMillis())
        )
    } catch (e: Exception) {
        null
    }
}

fun String.containsNormalized(query: String): Boolean {
    if (query.isBlank()) return true
    val normalizedSource = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
    val normalizedQuery = java.text.Normalizer.normalize(query, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
    return normalizedSource.contains(normalizedQuery)
}
