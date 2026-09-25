package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "dev_changelogs")
data class DevChangeLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val versionTitle: String,
    val category: String, // "FEATURE", "FIX", "UI", "DATABASE", "PERFORMANCE", "REFACTOR"
    val summary: String,
    val details: String,
    val developer: String = "Dev Team / AI Assistant",
    val timestamp: Long = System.currentTimeMillis()
) {
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
}

@Dao
interface DevChangeLogDao {
    @Query("SELECT * FROM dev_changelogs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DevChangeLog>>

    @Query("SELECT * FROM dev_changelogs ORDER BY timestamp DESC")
    suspend fun getAllLogsSync(): List<DevChangeLog>

    @Query("SELECT * FROM dev_changelogs WHERE summary LIKE '%' || :query || '%' OR details LIKE '%' || :query || '%' OR versionTitle LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<DevChangeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DevChangeLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DevChangeLog>)

    @Query("DELETE FROM dev_changelogs")
    suspend fun clearAllLogs()
}
