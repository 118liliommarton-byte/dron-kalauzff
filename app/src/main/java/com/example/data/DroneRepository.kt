package com.example.data

import kotlinx.coroutines.flow.Flow

class DroneRepository(
    private val chatDao: ChatDao,
    private val marketplaceDao: MarketplaceDao
) {
    private val firestoreSyncManager = FirestoreSyncManager(chatDao, marketplaceDao)

    init {
        firestoreSyncManager.startListening()
    }

    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    val allProfiles: Flow<List<UserProfile>> = chatDao.getAllProfiles()
    val allMarketplaceListings: Flow<List<MarketplaceListing>> = marketplaceDao.getAllListings()

    private var hasSeededOrCleared = false

    suspend fun seedMarketplaceIfEmpty() {
        marketplaceDao.deleteSampleListings()
        firestoreSyncManager.cleanUpSampleListingsFromCloud()
    }

    suspend fun insertMarketplaceListing(listing: MarketplaceListing): Long {
        val listingToInsert = if (listing.id == 0) {
            val uniqueId = (System.currentTimeMillis() % 1000000000L).toInt()
            listing.copy(id = uniqueId)
        } else {
            listing
        }
        val rowId = marketplaceDao.insertListing(listingToInsert)
        firestoreSyncManager.publishMarketplaceListing(listingToInsert)
        return rowId
    }

    suspend fun deleteMarketplaceListing(id: Int) {
        marketplaceDao.deleteListingById(id)
        firestoreSyncManager.deleteMarketplaceListing(id)
    }

    suspend fun insertMessage(message: ChatMessage) {
        val messageToInsert = if (message.id == 0) {
            val uniqueId = (System.currentTimeMillis() % 1000000000L).toInt()
            message.copy(id = uniqueId)
        } else {
            message
        }
        chatDao.insertMessage(messageToInsert)
        firestoreSyncManager.publishMessage(messageToInsert)
    }

    suspend fun deleteMessage(id: Int) {
        chatDao.deleteMessageById(id)
        firestoreSyncManager.deleteMessage(id)
    }

    suspend fun removeImageFromMessage(id: Int) {
        chatDao.removeImageFromMessage(id)
        firestoreSyncManager.removeImageFromMessage(id)
    }

    suspend fun clearMessages() {
        chatDao.clearAllMessages()
        firestoreSyncManager.clearAllMessages()
    }

    suspend fun clearMarketplaceListings() {
        hasSeededOrCleared = true
        marketplaceDao.deleteAllListings()
        firestoreSyncManager.clearAllMarketplaceListings()
    }

    suspend fun resetMarketplaceToDefault() {
        marketplaceDao.deleteSampleListings()
        firestoreSyncManager.cleanUpSampleListingsFromCloud()
    }

    suspend fun getCurrentProfile(): UserProfile? {
        return chatDao.getCurrentProfile()
    }

    suspend fun registerAndLoginProfile(username: String, displayName: String) {
        chatDao.logoutAll()
        val newProfile = UserProfile(username = username, displayName = displayName, isCurrentLoggedIn = true)
        chatDao.insertProfile(newProfile)
    }

    suspend fun loginExistingUser(username: String) {
        chatDao.logoutAll()
        chatDao.loginUser(username)
    }

    suspend fun logout() {
        chatDao.logoutAll()
    }

    private fun getInitialSampleListings(): List<MarketplaceListing> {
        return emptyList()
    }
}

