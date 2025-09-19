import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R

@Composable
fun QuestScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF101828))
        ) {
            // --- Haut (static) ---
            HeaderSection(
                xpText = "950/2400 XP",
                progress = 0.39f
            )

            // --- Centre scrollable ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                QuestCategory(
                    title = "Sport",
                    color = Color(0xFFE74C3C),
                    quests = listOf(
                        QuestItem("Faire 100 pompes", 400, done = true),
                        QuestItem("Faire 100 squats", 300, done = false),
                        QuestItem("Courir 10 km", 350, done = false),
                    )
                )

                QuestCategory(
                    title = "Cuisine",
                    color = Color(0xFFF39C12),
                    quests = listOf(
                        QuestItem("Faire une salade", 150, done = false),
                        QuestItem("Faire un plat italien", 250, done = true),
                    )
                )

                QuestCategory(
                    title = "Jeux Vidéo",
                    color = Color(0xFF9B59B6),
                    quests = listOf(
                        QuestItem("Faire une partie classée", 200, done = true),
                        QuestItem("Jouer 30 minutes", 100, done = false),
                    )
                )

                QuestCategory(
                    title = "Etudes",
                    color = Color(0xFF27AE60),
                    quests = listOf(
                        QuestItem("Faire ses devoirs", 350, done = false),
                        QuestItem("Réviser 30 minutes", 200, done = false),
                        QuestItem("Préparer ses affaires", 100, done = false),
                    )
                )
            }
        }
    }
}

@Composable
fun HeaderSection(xpText: String, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("QUÊTES", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(xpText, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            color = Color(0xFF3B82F6),
            trackColor = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

data class QuestItem(val title: String, val xp: Int, val done: Boolean)

@Composable
fun QuestCategory(title: String, color: Color, quests: List<QuestItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1E293B))
            .padding(12.dp)
    ) {
        // Titre category
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(8.dp)
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Liste des quêtes
        quests.forEach {
            val questColor = if (it.done) Color.Gray else Color.White
            Text(
                text = "› ${it.title}  ${it.xp} XP",
                color = questColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar(
        containerColor = Color(0xFF1E293B)
    ) {
        // QUETES
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(
                painter = painterResource(R.drawable.icon_quetes),
                contentDescription = "Quêtes"
            ) },
            label = { Text("Quêtes") }
        )
        // CLASSEMENT
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(
                painter = painterResource(R.drawable.icon_classement),
                contentDescription = "Classement"
            ) },
            label = { Text("Classement") }
        )
        // AMIS
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(
                painter = painterResource(R.drawable.icon_amis),
                contentDescription = "Amis") },
            label = { Text("Amis") }
        )
        // PARAMETRES
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(
                painter = painterResource(R.drawable.icon_parametres),
                contentDescription = "Paramètres") },
            label = { Text("Paramètres") }
        )
    }
}
