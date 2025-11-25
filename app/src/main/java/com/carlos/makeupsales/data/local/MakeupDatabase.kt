package com.carlos.makeupsales.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 3,               // 👈 ANTES: 2  (subimos a 3)
    exportSchema = false
)
@TypeConverters(MakeupTypeConverters::class)
abstract class MakeupDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao

    companion object {
        @Volatile
        private var INSTANCE: MakeupDatabase? = null

        // 👇 Migración NO-DESTRUCTIVA de 2 → 3
        // No cambiamos nada del esquema, solo indicamos a Room
        // que la BD versión 2 es compatible con la versión 3.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No-op: aquí irían ALTER TABLE si hubieras cambiado columnas
            }
        }

        fun getInstance(context: Context): MakeupDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MakeupDatabase::class.java,
                    "makeup_sales_db"
                )
                    // 👇 QUITAMOS fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_2_3)  // usamos migración en su lugar
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
