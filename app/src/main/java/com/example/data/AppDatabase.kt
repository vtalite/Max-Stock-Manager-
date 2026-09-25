package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WineItem::class,
        StockTransaction::class,
        AuditLog::class,
        CompanyProfile::class,
        EmployeeUser::class,
        CategoryConfig::class,
        DevChangeLog::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wineDao(): WineDao
    abstract fun transactionDao(): StockTransactionDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun companyDao(): CompanyDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun categoryConfigDao(): CategoryConfigDao
    abstract fun devChangeLogDao(): DevChangeLogDao

    companion object {
        const val DEFAULT_DATABASE_NAME = "adega_database"
        private val instances = java.util.concurrent.ConcurrentHashMap<String, AppDatabase>()

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE wines ADD COLUMN packagingType TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audit_logs ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDENTE'")
                db.execSQL("ALTER TABLE audit_logs ADD COLUMN reviewedBy TEXT")
                db.execSQL("ALTER TABLE audit_logs ADD COLUMN reviewedAt INTEGER")
                db.execSQL("ALTER TABLE audit_logs ADD COLUMN affectedWineId INTEGER")
                db.execSQL("ALTER TABLE audit_logs ADD COLUMN wineSnapshotJson TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE employees ADD COLUMN email TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context, databaseName: String = DEFAULT_DATABASE_NAME): AppDatabase {
            val safeName = if (databaseName.isBlank()) DEFAULT_DATABASE_NAME else databaseName.trim()
            return instances.computeIfAbsent(safeName) { name ->
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    name
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }
    }
}
