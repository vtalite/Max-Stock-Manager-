package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WineDao {
    @Query("SELECT * FROM wines ORDER BY name ASC")
    fun getAllWines(): Flow<List<WineItem>>

    @Query("SELECT * FROM wines WHERE id = :id")
    suspend fun getWineById(id: Int): WineItem?

    @Query("SELECT * FROM wines WHERE barcode = :barcode LIMIT 1")
    suspend fun getWineByBarcode(barcode: String): WineItem?

    @Query("SELECT * FROM wines WHERE barcode = :barcode LIMIT 1")
    fun getWineByBarcodeFlow(barcode: String): Flow<WineItem?>

    @Query("SELECT * FROM wines WHERE quantity <= minQuantity ORDER BY quantity ASC")
    fun getLowStockWines(): Flow<List<WineItem>>

    @Query("SELECT * FROM wines WHERE quantity = 0 ORDER BY name ASC")
    fun getOutOfStockWines(): Flow<List<WineItem>>

    @Query("SELECT * FROM wines WHERE name LIKE '%' || :query || '%' OR producer LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR grape LIKE '%' || :query || '%' OR region LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchWines(query: String): Flow<List<WineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWine(wine: WineItem): Long

    @Update
    suspend fun updateWine(wine: WineItem)

    @Delete
    suspend fun deleteWine(wine: WineItem)

    @Query("DELETE FROM wines")
    suspend fun deleteAllWines()

    @Query("UPDATE wines SET quantity = :newQuantity WHERE id = :wineId")
    suspend fun updateQuantity(wineId: Int, newQuantity: Int)

    @Query("DELETE FROM wines WHERE id = :id")
    suspend fun deleteWineById(id: Int)
}

@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE wineId = :wineId ORDER BY timestamp DESC")
    fun getTransactionsForWine(wineId: Int): Flow<List<StockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransaction)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE productName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR employeeName LIKE '%' || :query || '%' OR actionType LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): AuditLog?

    @Query("UPDATE audit_logs SET status = :status, reviewedBy = :reviewedBy, reviewedAt = :reviewedAt WHERE id = :logId")
    suspend fun updateLogStatus(logId: Long, status: String, reviewedBy: String, reviewedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog): Long

    @Update
    suspend fun updateLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAllLogs()
}

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY tradeName ASC")
    fun getAllCompanies(): Flow<List<CompanyProfile>>

    @Query("SELECT * FROM companies WHERE cnpj = :cnpj LIMIT 1")
    suspend fun getCompanyByCnpj(cnpj: String): CompanyProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyProfile)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE companyCnpj = :cnpj ORDER BY fullName ASC")
    fun getEmployeesForCompany(cnpj: String): Flow<List<EmployeeUser>>

    @Query("SELECT * FROM employees WHERE companyCnpj = :cnpj AND (username = :username OR fullName = :username) LIMIT 1")
    suspend fun getEmployee(cnpj: String, username: String): EmployeeUser?

    @Query("SELECT * FROM employees WHERE (companyCnpj = :cnpj OR :cnpj = '') AND (username = :identifier OR email = :identifier) LIMIT 1")
    suspend fun getEmployeeByIdentifierOrEmail(cnpj: String, identifier: String): EmployeeUser?

    @Query("SELECT * FROM employees WHERE email = :email LIMIT 1")
    suspend fun getEmployeeByEmail(email: String): EmployeeUser?

    @Query("UPDATE employees SET passwordHash = :newPassword WHERE id = :id")
    suspend fun updatePassword(id: Long, newPassword: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeUser): Long
}

