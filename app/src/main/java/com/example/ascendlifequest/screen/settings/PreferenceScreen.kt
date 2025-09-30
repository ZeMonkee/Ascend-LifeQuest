package com.example.ascendlifequest.screen.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun PreferenceScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Parametres) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quetes")
                    BottomNavItem.Classement -> navController.navigate("classement")
                    BottomNavItem.Amis -> navController.navigate("amis")
                    BottomNavItem.Parametres -> navController.navigate("parametres")
                    BottomNavItem.Profil -> navController.navigate("profil")
                }
            }
        }
    ) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                AppHeader(title = "PRÉFÉRENCES")

                Spacer(Modifier.height(8.dp))

                PreferenceQuestion("A quel point aimez-vous le sport ?", AppColor.SportColor)
                PreferenceQuestion("A quel point aimez-vous la cuisine ?", AppColor.CuisineColor)
                PreferenceQuestion("A quel point aimez-vous les jeux vidéo ?", AppColor.JeuxVideoColor)
                PreferenceQuestion("A quel point aimez-vous la lecture ?", AppColor.EtudesColor)
                PreferenceQuestion("A quel point aimez-vous le dessin ?", Color(0xFF009688)) // turquoise exemple
            }
        }
    }
}

@Composable
fun PreferenceQuestion(question: String, color: Color) {
    var selected by remember { androidx.compose.runtime.mutableIntStateOf(3) } // valeur par défaut au milieu

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..5).forEach { value ->
                    Button(
                        onClick = { selected = value },
                        modifier = Modifier
                            .size(42.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected == value) color else Color.DarkGray
                        )
                    ) {
                        Text(
                            text = value.toString(),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
