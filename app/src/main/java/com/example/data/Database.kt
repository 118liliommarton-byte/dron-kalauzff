package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val username: String,
    val displayName: String,
    val isCurrentLoggedIn: Boolean = false
)

@Entity(tableName = "marketplace_listings")
data class MarketplaceListing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "ELADÁS" or "VÉTEL"
    val category: String, // "DJI", "FPV", "Autel", "Tartozék", "Egyéb"
    val price: Int, // In HUF
    val condition: String, // "Bontatlan új", "Újszerű / Garanciális", "Kiváló állapot", "Használt", "Hibás / Alkatrész"
    val location: String, // "Budapest", "Debrecen", etc.
    val description: String,
    val batteryCycles: String? = null,
    val accessories: String? = null,
    val sellerName: String,
    val contactPhone: String,
    val contactEmail: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isUserCreated: Boolean = false,
    val imageUri: String? = null
) {
    fun getImageUris(): List<String> {
        if (imageUri.isNullOrBlank()) return emptyList()
        return imageUri.split("|||")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}

@Dao
interface MarketplaceDao {
    @Query("SELECT * FROM marketplace_listings ORDER BY timestamp DESC")
    fun getAllListings(): Flow<List<MarketplaceListing>>

    @Query("SELECT * FROM marketplace_listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: Int): MarketplaceListing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceListing): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllListings(listings: List<MarketplaceListing>)

    @Query("DELETE FROM marketplace_listings WHERE id = :id")
    suspend fun deleteListingById(id: Int)

    @Query("DELETE FROM marketplace_listings")
    suspend fun deleteAllListings()

    @Query("DELETE FROM marketplace_listings WHERE isUserCreated = 0 OR sellerName IN ('Kovács Balázs', 'Nagy Péter', 'Tóth Gábor', 'Varga Dániel', 'Molnár Zoltán', 'Farkas Ádám', 'Szabó Kristóf')")
    suspend fun deleteSampleListings()

    @Query("DELETE FROM marketplace_listings WHERE isUserCreated = 0")
    suspend fun deleteNonUserListings()

    @Query("SELECT COUNT(*) FROM marketplace_listings")
    suspend fun getListingsCount(): Int
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    @Query("UPDATE chat_messages SET imageUri = NULL WHERE id = :id")
    suspend fun removeImageFromMessage(id: Int)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    // Profile management
    @Query("SELECT * FROM user_profiles")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE isCurrentLoggedIn = 1 LIMIT 1")
    suspend fun getCurrentProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET isCurrentLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE user_profiles SET isCurrentLoggedIn = 1 WHERE username = :username")
    suspend fun loginUser(username: String)
}

@Database(entities = [ChatMessage::class, UserProfile::class, MarketplaceListing::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun marketplaceDao(): MarketplaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drone_guide_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
