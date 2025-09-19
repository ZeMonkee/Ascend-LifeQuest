package com.example.ascendlifequest.model

import java.util.Date

data class User(
    val accountId: Int,
    val pseudo: String,
    val photoUrl: Int,
    val xp: Int = 0,
    val online: Boolean = false,
    val quetesRealisees: Int = 0,
    val streak: Int = 0,
    val dateDeCreation: Date = Date(),
    val rang: Int = 1
) {
    constructor() : this(0, "", 0, 0, false, 0, 0, Date(), 1)
}