package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Universal Cloud Synchronization Manager for Live Chat and Marketplace.
 * Coordinates real-time sync with strict deletion propagation and timestamp filtering.
 */
class FirestoreSyncManager(
    private val chatDao: ChatDao,
    private val marketplaceDao: MarketplaceDao
) {
    private val TAG = "FirestoreSyncManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isListening = false

    // Clean channels for fresh, bug-free communication
    private val CHAT_CHANNEL = "dronkalauz_f1b57_chat_v2"
    private val MARKETPLACE_CHANNEL = "dronkalauz_f1b57_marketplace_v2"

    // In-memory tombstone sets to prevent re-inserting deleted items
    private val deletedMessageIds = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())
    private val deletedListingIds = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())

    @Volatile
    private var chatClearedTimestamp: Long = 0L

    @Volatile
    private var marketplaceClearedTimestamp: Long = 0L

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore SDK not available: ${e.message}")
            null
        }
    }

    fun startListening() {
        if (isListening) return
        isListening = true
        Log.i(TAG, "Starting clean real-time cloud listeners...")

        // 1. Try Native Firebase Firestore (if enabled by user in console)
        val firestore = db
        if (firestore != null) {
            try {
                firestore.collection("chat_messages")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.d(TAG, "Firestore chat snapshot status: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            scope.launch {
                                for (doc in snapshot.documents) {
                                    val id = doc.getLong("id")?.toInt() ?: doc.id.hashCode()
                                    if (deletedMessageIds.contains(id)) continue

                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    if (timestamp <= chatClearedTimestamp) continue

                                    val senderName = doc.getString("senderName") ?: "Pilóta"
                                    val msgText = doc.getString("message") ?: ""
                                    val imageUri = doc.getString("imageUri")

                                    val chatMessage = ChatMessage(
                                        id = id,
                                        senderName = senderName,
                                        message = msgText,
                                        timestamp = timestamp,
                                        imageUri = imageUri
                                    )
                                    chatDao.insertMessage(chatMessage)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.d(TAG, "Firestore native chat listener notice: ${e.message}")
            }

            try {
                firestore.collection("marketplace_listings")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.d(TAG, "Firestore marketplace snapshot status: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            scope.launch {
                                for (doc in snapshot.documents) {
                                    val id = doc.getLong("id")?.toInt() ?: doc.id.hashCode()
                                    if (deletedListingIds.contains(id)) continue

                                    val title = doc.getString("title") ?: ""
                                    val type = doc.getString("type") ?: "ELADÁS"
                                    val category = doc.getString("category") ?: "DJI"
                                    val price = doc.getLong("price")?.toInt() ?: 0
                                    val condition = doc.getString("condition") ?: "Használt"
                                    val location = doc.getString("location") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val batteryCycles = doc.getString("batteryCycles") ?: ""
                                    val accessories = doc.getString("accessories") ?: ""
                                    val sellerName = doc.getString("sellerName") ?: "Drón Pilóta"
                                    val contactPhone = doc.getString("contactPhone") ?: ""
                                    val contactEmail = doc.getString("contactEmail") ?: ""
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    if (timestamp <= marketplaceClearedTimestamp) continue
                                    val isUserCreated = doc.getBoolean("isUserCreated") ?: false
                                    val sampleSellers = setOf("Kovács Balázs", "Nagy Péter", "Tóth Gábor", "Varga Dániel", "Molnár Zoltán", "Farkas Ádám", "Szabó Kristóf")
                                    if (!isUserCreated || sampleSellers.contains(sellerName) || title.contains("Nazgul5") || title.contains("DJI Mini 4 Pro Fly More") || title.contains("DJI Avata 2") || title.contains("Autel EVO Lite+")) {
                                        try {
                                            doc.reference.delete()
                                        } catch (_: Exception) {}
                                        marketplaceDao.deleteListingById(id)
                                        continue
                                    }
                                    val imageUri = doc.getString("imageUri")

                                    val listing = MarketplaceListing(
                                        id = id,
                                        title = title,
                                        type = type,
                                        category = category,
                                        price = price,
                                        condition = condition,
                                        location = location,
                                        description = description,
                                        batteryCycles = batteryCycles,
                                        accessories = accessories,
                                        sellerName = sellerName,
                                        contactPhone = contactPhone,
                                        contactEmail = contactEmail,
                                        timestamp = timestamp,
                                        isUserCreated = isUserCreated,
                                        imageUri = imageUri
                                    )
                                    marketplaceDao.insertListing(listing)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.d(TAG, "Firestore native marketplace listener notice: ${e.message}")
            }
        }

        // 2. Fetch history ONCE on startup (NO infinite polling loop!)
        scope.launch {
            fetchInitialCloudData(CHAT_CHANNEL) { line -> handleIncomingChatMessageJson(line) }
            fetchInitialCloudData(MARKETPLACE_CHANNEL) { line -> handleIncomingMarketplaceJson(line) }
        }

        // 3. Persistent real-time event stream
        scope.launch {
            listenStreamLoop(CHAT_CHANNEL) { line -> handleIncomingChatMessageJson(line) }
        }
        scope.launch {
            listenStreamLoop(MARKETPLACE_CHANNEL) { line -> handleIncomingMarketplaceJson(line) }
        }
    }

    // --- Message Broadcasting & Deletion ---

    fun publishMessage(chatMsg: ChatMessage) {
        deletedMessageIds.remove(chatMsg.id)

        // Native Firestore
        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    val data = hashMapOf(
                        "id" to chatMsg.id,
                        "senderName" to chatMsg.senderName,
                        "message" to chatMsg.message,
                        "imageUri" to chatMsg.imageUri,
                        "timestamp" to chatMsg.timestamp
                    )
                    firestore.collection("chat_messages").document(chatMsg.id.toString()).set(data)
                } catch (e: Exception) {
                    Log.d(TAG, "Firestore publish notice: ${e.message}")
                }
            }
        }

        // Universal Realtime Relay
        scope.launch {
            val json = JSONObject().apply {
                put("action", "upsert")
                put("id", chatMsg.id)
                put("senderName", chatMsg.senderName)
                put("message", chatMsg.message)
                put("timestamp", chatMsg.timestamp)
                if (chatMsg.imageUri != null) put("imageUri", chatMsg.imageUri)
            }
            postToCloud(CHAT_CHANNEL, json.toString())
        }
    }

    fun deleteMessage(id: Int) {
        deletedMessageIds.add(id)

        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    firestore.collection("chat_messages").document(id.toString()).delete()
                } catch (_: Exception) {}
            }
        }

        scope.launch {
            val json = JSONObject().apply {
                put("action", "delete")
                put("id", id)
            }
            postToCloud(CHAT_CHANNEL, json.toString())
        }
    }

    fun removeImageFromMessage(id: Int) {
        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    firestore.collection("chat_messages").document(id.toString()).update("imageUri", null)
                } catch (_: Exception) {}
            }
        }

        scope.launch {
            val json = JSONObject().apply {
                put("action", "remove_image")
                put("id", id)
            }
            postToCloud(CHAT_CHANNEL, json.toString())
        }
    }

    fun clearAllMessages() {
        val now = System.currentTimeMillis()
        chatClearedTimestamp = now

        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    firestore.collection("chat_messages").get().addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        scope.launch {
            val json = JSONObject().apply {
                put("action", "clear_all")
                put("timestamp", now)
            }
            postToCloud(CHAT_CHANNEL, json.toString())
        }
    }

    fun publishMarketplaceListing(listing: MarketplaceListing) {
        deletedListingIds.remove(listing.id)

        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    val data = hashMapOf(
                        "id" to listing.id,
                        "title" to listing.title,
                        "type" to listing.type,
                        "category" to listing.category,
                        "price" to listing.price,
                        "condition" to listing.condition,
                        "location" to listing.location,
                        "description" to listing.description,
                        "batteryCycles" to listing.batteryCycles,
                        "accessories" to listing.accessories,
                        "sellerName" to listing.sellerName,
                        "contactPhone" to listing.contactPhone,
                        "contactEmail" to listing.contactEmail,
                        "timestamp" to listing.timestamp,
                        "isUserCreated" to listing.isUserCreated,
                        "imageUri" to listing.imageUri
                    )
                    firestore.collection("marketplace_listings").document(listing.id.toString()).set(data)
                } catch (e: Exception) {
                    Log.d(TAG, "Firestore marketplace notice: ${e.message}")
                }
            }
        }

        scope.launch {
            val json = JSONObject().apply {
                put("action", "upsert")
                put("id", listing.id)
                put("title", listing.title)
                put("type", listing.type)
                put("category", listing.category)
                put("price", listing.price)
                put("condition", listing.condition)
                put("location", listing.location)
                put("description", listing.description)
                put("batteryCycles", listing.batteryCycles ?: "")
                put("accessories", listing.accessories ?: "")
                put("sellerName", listing.sellerName)
                put("contactPhone", listing.contactPhone)
                put("contactEmail", listing.contactEmail ?: "")
                put("timestamp", listing.timestamp)
                put("isUserCreated", listing.isUserCreated)
                if (listing.imageUri != null) put("imageUri", listing.imageUri)
            }
            postToCloud(MARKETPLACE_CHANNEL, json.toString())
        }
    }

    fun deleteMarketplaceListing(id: Int) {
        deletedListingIds.add(id)

        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    firestore.collection("marketplace_listings").document(id.toString()).delete()
                } catch (_: Exception) {}
            }
        }

        scope.launch {
            val json = JSONObject().apply {
                put("action", "delete")
                put("id", id)
            }
            postToCloud(MARKETPLACE_CHANNEL, json.toString())
        }
    }

    fun cleanUpSampleListingsFromCloud() {
        val sampleSellers = setOf("Kovács Balázs", "Nagy Péter", "Tóth Gábor", "Varga Dániel", "Molnár Zoltán", "Farkas Ádám", "Szabó Kristóf")
        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    firestore.collection("marketplace_listings").get().addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            val sellerName = doc.getString("sellerName") ?: ""
                            val title = doc.getString("title") ?: ""
                            val isUserCreated = doc.getBoolean("isUserCreated") ?: false
                            val id = doc.getLong("id")?.toInt() ?: doc.id.hashCode()
                            if (!isUserCreated || sampleSellers.contains(sellerName) || title.contains("Nazgul5") || title.contains("DJI Mini 4 Pro") || title.contains("DJI Avata 2") || title.contains("Autel EVO")) {
                                doc.reference.delete()
                                scope.launch { marketplaceDao.deleteListingById(id) }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun clearAllMarketplaceListings() {
        val now = System.currentTimeMillis()
        marketplaceClearedTimestamp = now

        val firestore = db
        if (firestore != null) {
            scope.launch {
                try {
                    firestore.collection("marketplace_listings").get().addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            doc.reference.delete()
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        scope.launch {
            val json = JSONObject().apply {
                put("action", "clear_all")
                put("timestamp", now)
            }
            postToCloud(MARKETPLACE_CHANNEL, json.toString())
        }
    }

    // --- Ingest Handlers ---

    private suspend fun handleIncomingChatMessageJson(payloadStr: String) {
        try {
            val trimmed = payloadStr.trim()
            if (!trimmed.startsWith("{")) return
            val json = JSONObject(trimmed)
            val action = json.optString("action", "upsert")

            if (action == "clear_all") {
                val ts = json.optLong("timestamp", System.currentTimeMillis())
                if (ts > chatClearedTimestamp) {
                    chatClearedTimestamp = ts
                }
                chatDao.clearAllMessages()
                Log.d(TAG, "Chat cleared via remote command")
                return
            }

            val id = json.getInt("id")

            if (action == "delete") {
                deletedMessageIds.add(id)
                chatDao.deleteMessageById(id)
                Log.d(TAG, "Chat message $id deleted via remote command")
                return
            }

            if (action == "remove_image") {
                chatDao.removeImageFromMessage(id)
                Log.d(TAG, "Chat message $id image removed via remote command")
                return
            }

            // Upsert check: ignore if previously deleted or before clear timestamp
            if (deletedMessageIds.contains(id)) {
                return
            }

            val timestamp = json.optLong("timestamp", System.currentTimeMillis())
            if (timestamp <= chatClearedTimestamp) {
                return
            }

            val senderName = json.optString("senderName", "Pilóta")
            val message = json.optString("message", "")
            val imageUri = if (json.isNull("imageUri")) null else json.optString("imageUri").ifBlank { null }

            val chatMsg = ChatMessage(
                id = id,
                senderName = senderName,
                message = message,
                timestamp = timestamp,
                imageUri = imageUri
            )
            chatDao.insertMessage(chatMsg)
            Log.d(TAG, "Synced chat message: $senderName: $message")
        } catch (e: Exception) {
            Log.d(TAG, "Parse chat error: ${e.message}")
        }
    }

    private suspend fun handleIncomingMarketplaceJson(payloadStr: String) {
        try {
            val trimmed = payloadStr.trim()
            if (!trimmed.startsWith("{")) return
            val json = JSONObject(trimmed)
            val action = json.optString("action", "upsert")

            if (action == "clear_all") {
                val ts = json.optLong("timestamp", System.currentTimeMillis())
                if (ts > marketplaceClearedTimestamp) {
                    marketplaceClearedTimestamp = ts
                }
                marketplaceDao.deleteAllListings()
                Log.d(TAG, "Marketplace cleared via remote command")
                return
            }

            val id = json.getInt("id")

            if (action == "delete") {
                deletedListingIds.add(id)
                marketplaceDao.deleteListingById(id)
                return
            }

            if (deletedListingIds.contains(id)) {
                return
            }

            val timestamp = json.optLong("timestamp", System.currentTimeMillis())
            if (timestamp <= marketplaceClearedTimestamp) {
                return
            }

            val title = json.optString("title", "")
            val type = json.optString("type", "ELADÁS")
            val category = json.optString("category", "DJI")
            val price = json.optInt("price", 0)
            val condition = json.optString("condition", "Használt")
            val location = json.optString("location", "")
            val description = json.optString("description", "")
            val batteryCycles = json.optString("batteryCycles", "")
            val accessories = json.optString("accessories", "")
            val sellerName = json.optString("sellerName", "Drón Pilóta")
            val contactPhone = json.optString("contactPhone", "")
            val contactEmail = json.optString("contactEmail", "")
            val isUserCreated = json.optBoolean("isUserCreated", true)
            val imageUri = if (json.isNull("imageUri")) null else json.optString("imageUri").ifBlank { null }

            val listing = MarketplaceListing(
                id = id,
                title = title,
                type = type,
                category = category,
                price = price,
                condition = condition,
                location = location,
                description = description,
                batteryCycles = batteryCycles.ifBlank { null },
                accessories = accessories.ifBlank { null },
                sellerName = sellerName,
                contactPhone = contactPhone,
                contactEmail = contactEmail.ifBlank { null },
                timestamp = timestamp,
                isUserCreated = isUserCreated,
                imageUri = imageUri
            )
            marketplaceDao.insertListing(listing)
            Log.d(TAG, "Synced listing: $title")
        } catch (e: Exception) {
            Log.d(TAG, "Parse marketplace error: ${e.message}")
        }
    }

    // --- HTTP Networking ---

    private fun postToCloud(channel: String, body: String) {
        try {
            val url = URL("https://ntfy.sh/$channel")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            conn.setRequestProperty("Title", "dronkalauz_sync")

            conn.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }
            val responseCode = conn.responseCode
            Log.d(TAG, "Published to cloud [$channel] -> HTTP $responseCode")
            conn.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Cloud post failed [$channel]: ${e.message}")
        }
    }

    private suspend fun fetchInitialCloudData(channel: String, onMessage: suspend (String) -> Unit) {
        try {
            val url = URL("https://ntfy.sh/$channel/json?poll=1&since=24h")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                var line: String? = reader.readLine()
                while (line != null) {
                    try {
                        val obj = JSONObject(line)
                        if (obj.optString("event") == "message") {
                            val innerMessage = obj.optString("message")
                            if (innerMessage.isNotBlank()) {
                                onMessage(innerMessage)
                            }
                        }
                    } catch (_: Exception) {}
                    line = reader.readLine()
                }
                reader.close()
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.d(TAG, "Fetch initial cloud data notice [$channel]: ${e.message}")
        }
    }

    private suspend fun listenStreamLoop(channel: String, onMessage: suspend (String) -> Unit) {
        while (isListening) {
            var conn: HttpURLConnection? = null
            var reader: BufferedReader? = null
            try {
                val url = URL("https://ntfy.sh/$channel/raw")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 0 // Stream read

                if (conn.responseCode == 200) {
                    reader = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8))
                    var line: String? = reader.readLine()
                    while (isListening && line != null) {
                        if (line.isNotBlank()) {
                            onMessage(line)
                        }
                        line = reader.readLine()
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Stream notice [$channel]: ${e.message}, reconnecting in 3s...")
            } finally {
                try { reader?.close() } catch (_: Exception) {}
                try { conn?.disconnect() } catch (_: Exception) {}
            }
            delay(3000)
        }
    }
}
