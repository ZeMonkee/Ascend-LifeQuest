package com.example.ascendlifequest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ascendlifequest.database.dao.*
import com.example.ascendlifequest.database.entities.*

@Database(
    entities = [
        // Entités existantes
        QuestEntity::class,
        QuestStateEntity::class,
        CategoryPreferenceEntity::class,
        // Nouvelles entités
        UserProfileEntity::class,
        FriendshipEntity::class,
        ConversationEntity::class,
        ConversationParticipantEntity::class,
        MessageEntity::class,
        CustomThemeEntity::class,
        AppSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
/**
 * Room database for local data persistence.
 *
 * Relations:
 * - UserProfile 1:N Quest (un utilisateur a plusieurs quêtes)
 * - UserProfile N:M UserProfile via Friendship (relation d'amitié)
 * - UserProfile N:M Conversation via ConversationParticipant
 * - Conversation 1:N Message
 * - UserProfile 1:N CustomTheme
 * - UserProfile 1:1 AppSettings
 */
abstract class AppDatabase : RoomDatabase() {

    // DAOs existants
    abstract fun questDao(): QuestDao
    abstract fun questStateDao(): QuestStateDao
    abstract fun categoryPreferenceDao(): CategoryPreferenceDao

    // Nouveaux DAOs
    abstract fun userProfileDao(): UserProfileDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun themeDao(): ThemeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    val instance =
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "ascend_lifequest_database"
                        )
                        .fallbackToDestructiveMigration(false)
                        .build()
                    INSTANCE = instance
                    instance
                }
        }

        /**
         * Réinitialise l'instance (utile pour les tests ou le changement d'utilisateur)
         */
        fun resetInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
