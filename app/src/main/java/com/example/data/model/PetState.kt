package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PetSpecies(val id: String, val labelArabic: String, val unlockCost: Int) {
    BUNNY("bunny", "أرنوب بيلّو 🐰", 0),
    KITTY("kitty", "قطقوط بيلّو 🐱", 50),
    FALCON("falcon", "صقر بيلّو 🦅", 100)
}

enum class PetGrowthStage(val level: Int, val titleArabic: String, val minHappiness: Int) {
    BABY(1, "بيبي (بداية الالتزام)", 0),
    CHILD(2, "صغير (مواظب)", 30),
    YOUTH(3, "نشيط (ملتزم بقوة)", 70),
    CHAMPION(4, "بطل بيلّو الخارق", 120)
}

@Entity(tableName = "pet_state")
data class PetState(
    @PrimaryKey val profileId: Long = 1L,
    val species: String = PetSpecies.BUNNY.id,
    val stage: Int = 1,
    val happiness: Int = 50,
    val lastFedAt: Long = System.currentTimeMillis(),
    val equippedItem: String = "none"
)
