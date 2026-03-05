package com.example.kalyansarathi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String = "",
    val gender: Gender = Gender.MALE,
    val customGender: String = "", // For storing custom gender text
    val livingArea: LivingArea = LivingArea.RURAL,
    val customLivingArea: String = "", // For storing custom living area text
    val occupation: Occupation = Occupation.DAILY_WAGE_LABOR,
    val customOccupation: String = "", // For storing custom occupation text
    val age: Int = 0,
    val caste: Caste = Caste.OTHER_CASTE,
    val customCaste: String = "", // For storing custom caste text
    val state: String = "",
    val isMinorityReligion: Boolean = false,
    val customMinorityReligion: String = "", // For storing custom minority religion details
    val monthlyIncome: IncomeRange = IncomeRange.LESS_THAN_5000,
    val customIncome: String = "", // For storing custom income details
    val preferredLanguage: Language = Language.ENGLISH,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class LivingArea {
    RURAL, URBAN
}

enum class Occupation {
    DAILY_WAGE_LABOR,
    ENTREPRENEUR,
    STUDENT,
    EMPLOYED,
    INFORMAL_SECTOR,
    OTHER
}

enum class Caste {
    SCHEDULED_CASTE,
    SCHEDULED_TRIBE,
    BACKWARD_CASTE,
    OTHER_BACKWARD_CASTE,
    GENERAL_CATEGORY,
    OTHER_CASTE
}

enum class IncomeRange {
    LESS_THAN_5000,
    BETWEEN_5000_10000,
    BETWEEN_10000_25000,
    BETWEEN_25000_50000,
    MORE_THAN_50000
}

enum class Language {
    ENGLISH, HINDI
}








