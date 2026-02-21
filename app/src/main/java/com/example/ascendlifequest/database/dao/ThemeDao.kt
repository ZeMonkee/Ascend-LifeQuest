package com.example.ascendlifequest.database.dao

import androidx.room.*
import com.example.ascendlifequest.database.entities.AppSettingsEntity
import com.example.ascendlifequest.database.entities.CustomThemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {

    // ===== Custom Themes =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTheme(theme: CustomThemeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomThemes(themes: List<CustomThemeEntity>)

    @Update
    suspend fun updateCustomTheme(theme: CustomThemeEntity)

    @Delete
    suspend fun deleteCustomTheme(theme: CustomThemeEntity)

    @Query("DELETE FROM custom_themes WHERE id = :themeId")
    suspend fun deleteCustomThemeById(themeId: String)

    @Query("DELETE FROM custom_themes WHERE userId = :userId")
    suspend fun deleteAllCustomThemesForUser(userId: String)

    @Query("SELECT * FROM custom_themes WHERE id = :themeId")
    suspend fun getCustomThemeById(themeId: String): CustomThemeEntity?

    @Query("SELECT * FROM custom_themes WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    suspend fun getCustomThemesForUser(userId: String): List<CustomThemeEntity>

    @Query("SELECT * FROM custom_themes WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    fun observeCustomThemesForUser(userId: String): Flow<List<CustomThemeEntity>>

    @Query("SELECT * FROM custom_themes WHERE userId = :userId AND isSelected = 1 LIMIT 1")
    suspend fun getSelectedCustomTheme(userId: String): CustomThemeEntity?

    @Query("UPDATE custom_themes SET isSelected = 0 WHERE userId = :userId")
    suspend fun deselectAllThemesForUser(userId: String)

    @Query("UPDATE custom_themes SET isSelected = 1 WHERE id = :themeId")
    suspend fun selectTheme(themeId: String)

    // ===== App Settings =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)

    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    suspend fun getSettingsForUser(userId: String): AppSettingsEntity?

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    fun observeSettingsForUser(userId: String): Flow<AppSettingsEntity?>

    @Query("DELETE FROM app_settings WHERE userId = :userId")
    suspend fun deleteSettingsForUser(userId: String)

    @Transaction
    suspend fun selectCustomTheme(userId: String, themeId: String) {
        deselectAllThemesForUser(userId)
        selectTheme(themeId)
        val settings = getSettingsForUser(userId)
        if (settings != null) {
            updateSettings(
                settings.copy(
                    selectedCustomThemeId = themeId,
                    isCustomTheme = true
                )
            )
        }
    }

    @Transaction
    suspend fun selectPredefinedTheme(userId: String, themeType: String) {
        deselectAllThemesForUser(userId)
        val settings = getSettingsForUser(userId)
        if (settings != null) {
            updateSettings(
                settings.copy(
                    selectedThemeType = themeType,
                    selectedCustomThemeId = null,
                    isCustomTheme = false
                )
            )
        } else {
            insertSettings(
                AppSettingsEntity(
                    userId = userId,
                    selectedThemeType = themeType,
                    isCustomTheme = false
                )
            )
        }
    }
}
