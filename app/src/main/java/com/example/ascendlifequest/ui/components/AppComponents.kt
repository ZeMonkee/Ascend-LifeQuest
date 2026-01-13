package com.example.ascendlifequest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.ui.theme.AppColor

// Background
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Image(
        painter = painterResource(R.drawable.background),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    content()
}

// Header
@Composable
fun AppHeader(
    title: String,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppColor.MainTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// BottomNavBar
@Composable
fun AppBottomNavBar(
    navController: NavHostController,
    current: BottomNavItem,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            InitNavItem(current = current) { selected ->
                if (selected != current) {
                    when (selected) {
                        BottomNavItem.Quetes -> navController.navigate("quetes")
                        BottomNavItem.Classement -> navController.navigate("classement")
                        BottomNavItem.Amis -> navController.navigate("amis")
                        BottomNavItem.Parametres -> navController.navigate("parametres")
                        BottomNavItem.Profil -> navController.navigate("profil")
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding) // le contenu
    }
}

enum class BottomNavItem(val label: String, val icon: Int) {
    Quetes("Quêtes", R.drawable.icon_quetes),
    Classement("Classement", R.drawable.icon_classement),
    Amis("Amis", R.drawable.icon_amis),
    Profil("Profil", R.drawable.icon_profil),
    Parametres("Paramètres", R.drawable.icon_parametres)
}

@Composable
fun InitNavItem(
    current: BottomNavItem = BottomNavItem.Quetes,
    onItemSelected: (BottomNavItem) -> Unit = {}
) {
    NavigationBar(
        containerColor = AppColor.DarkBlueColor,
        contentColor = AppColor.MainTextColor
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = current == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        tint = if (current == item) AppColor.LightBlueColor else AppColor.MainTextColor
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (current == item) AppColor.LightBlueColor else AppColor.MainTextColor,
                        fontSize = 11.sp,
                    )
                }
            )
        }
    }
}