package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyProfile(
    @PrimaryKey val cnpj: String, // ex: "12.345.678/0001-90"
    val tradeName: String, // Nome Fantasia
    val legalName: String = tradeName, // Razão Social
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class EmployeeUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyCnpj: String,
    val username: String, // ex: "carlos.silva" ou "admin"
    val fullName: String, // ex: "Carlos Silva"
    val passwordHash: String, // ex: "123456"
    val role: String = "Operador de Estoque", // "Administrador", "Gerente", "Operador de Estoque"
    val email: String = "", // E-mail cadastrado do usuário para recuperação de acesso
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthSession(
    val isLoggedIn: Boolean = false,
    val companyCnpj: String = "",
    val companyName: String = "",
    val employeeUsername: String = "",
    val employeeName: String = "",
    val role: String = "",
    val loginTimestamp: Long = 0L
)

fun formatCnpj(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(14)
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i == 2 || i == 5) sb.append(".")
        if (i == 8) sb.append("/")
        if (i == 12) sb.append("-")
        sb.append(digits[i])
    }
    return sb.toString()
}
