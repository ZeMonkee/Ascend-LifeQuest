package com.example.ascendlifequest.ui.features.quest

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.features.quest.components.QuestCategory
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.theme.AppColor
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ascendlifequest.di.AppViewModelFactory
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import kotlinx.coroutines.launch

@Composable
fun QuestScreen(navController: NavHostController) {
    val repository = QuestRepository()
    val factory = AppViewModelFactory(questRepository = repository)
    val viewModel: QuestViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val authService = remember { AuthService() }
    val authRepository = AuthRepositoryImpl(authService)

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    AppBottomNavBar(navController, BottomNavItem.Quetes) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppHeader(title = "QUÊTES")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.LightGray,
                        strokeCap = StrokeCap.Butt,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("60%", fontSize = 16.sp, color = AppColor.MinusTextColor)

                    // 🔘 BOUTON CRÉER UNE QUÊTE
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val ok = viewModel.generateQuestForRandomCategory()
                                if (ok) {
                                    viewModel.refreshData()
                                } else {
                                    Log.e("QuestScreen", "❌ Échec génération quête")
                                }
                            }
                        }
                    ) {
                        Text("Créer une quête")
                    }
                }

                when (uiState) {
                    is QuestUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is QuestUiState.Error -> {
                        Text((uiState as QuestUiState.Error).message)
                    }
                    is QuestUiState.Success -> {
                        val data = uiState as QuestUiState.Success
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(data.categories) { categorie ->
                                val questsForCategory = data.quests.filter { it.categorie == categorie.id }

                                if (questsForCategory.isNotEmpty()) {
                                    val ctx = LocalContext.current
                                    val uid = authRepository.getCurrentUserId()
                                    QuestCategory(
                                        categorie = categorie,
                                        quests = questsForCategory,
                                        getQuestState = { questId -> viewModel.getQuestState(ctx, uid, questId) },
                                        onToggleQuestState = { questId, newState -> viewModel.saveQuestState(ctx, uid, questId, newState) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
