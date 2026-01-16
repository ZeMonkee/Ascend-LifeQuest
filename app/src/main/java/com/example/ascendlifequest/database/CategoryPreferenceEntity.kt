package com.example.ascendlifequest.database

import androidx.room.Entity

@Entity(tableName = "category_preferences", primaryKeys = ["userId", "categoryId"])
data class CategoryPreferenceEntity(
    val userId: String,
    val categoryId: Int,
    val preference: Int
)
