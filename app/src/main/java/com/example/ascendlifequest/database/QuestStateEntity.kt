package com.example.ascendlifequest.database

import androidx.room.Entity

@Entity(tableName = "quest_states", primaryKeys = ["userId", "questId"])
data class QuestStateEntity(
    val userId: String,
    val questId: Int,
    val isDone: Boolean
)
