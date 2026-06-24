package com.imut.autoclicker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room database for storing macros.
 */
@Database(entities = [MacroEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
}

/**
 * Repository for managing macros.
 */
@Singleton
class MacroRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "autoclicker_database"
    ).build()

    private val dao: MacroDao = database.macroDao()

    fun getAllMacros() = dao.getAllMacros()

    suspend fun getMacroById(id: Long) = dao.getMacroById(id)

    suspend fun saveMacro(macro: MacroEntity): Long {
        return dao.insertMacro(macro)
    }

    suspend fun updateMacro(macro: MacroEntity) {
        dao.updateMacro(macro)
    }

    suspend fun deleteMacro(macro: MacroEntity) {
        dao.deleteMacro(macro)
    }

    suspend fun deleteMacroById(id: Long) {
        dao.deleteMacroById(id)
    }

    suspend fun markUsed(id: Long) {
        dao.markMacroUsed(id)
    }
}
