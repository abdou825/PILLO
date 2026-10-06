package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_wallet")
data class CoinWallet(
    @PrimaryKey val profileId: Long = 1L,
    val balance: Int = 20
)

@Entity(tableName = "coin_transactions")
data class CoinTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val amount: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "unlocked_items", primaryKeys = ["profileId", "itemId"])
data class UnlockedItem(
    val profileId: Long,
    val itemId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
