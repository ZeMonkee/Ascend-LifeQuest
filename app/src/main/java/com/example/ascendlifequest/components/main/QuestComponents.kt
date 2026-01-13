package com.example.ascendlifequest.components.main

import android.content.Context
import androidx.compose.foundation.background
// Import pour le Modifier.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
// Imports pour la gestion d'état
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.helpers.QuestHelper
import com.example.ascendlifequest.model.Categorie
import com.example.ascendlifequest.model.Quest
import com.example.ascendlifequest.service.AuthService
import com.example.ascendlifequest.ui.theme.AppColor

// Catégorie des quêtes
@Composable
fun QuestCategory(
    categorie: Categorie, // Utilise le modèle Categorie
    quests: List<Quest>,  // Utilise la liste de Quest
    context: Context, // Passer le contexte pour accéder aux SharedPreferences
    onQuestStateChanged: (questId: Int, isDone: Boolean) -> Unit = { _, _ -> } // Callback pour notifier le changement
) {
    // Recuperer userId
    val authService = remember { AuthService(context) }
    val userId = authService.getUserId()
    // Initialiser les états des quêtes à partir des SharedPreferences
    val questDoneStates = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            quests.forEach {
                put(it.id, QuestHelper.getQuestState(context, userId, it.id))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                AppColor.DarkBlueColor,
                shape = RoundedCornerShape(
                    topStart = 30.dp,
                    topEnd = 30.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                )
            )
    ) {
        // Header avec icône
        Card(shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(categorie.couleur)
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = categorie.logo),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    categorie.nom,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Liste des quêtes
        quests.forEach { quest ->
            val isDone = questDoneStates[quest.id] ?: false

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Inverser l'état de la quête
                        val newState = !isDone
                        questDoneStates[quest.id] = newState

                        // Sauvegarder l'état dans SharedPreferences
                        QuestHelper.saveQuestState(context, userId, quest.id, newState)

                        // Notifier le parent du changement
                        onQuestStateChanged(quest.id, newState)
                    }
                    .background(AppColor.DarkBlueColor, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "» ${quest.nom}",
                    color = if (isDone) AppColor.MinusTextColor else AppColor.MainTextColor,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${quest.xpRapporte} XP",
                    color = if (isDone) AppColor.MinusTextColor else AppColor.MainTextColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

