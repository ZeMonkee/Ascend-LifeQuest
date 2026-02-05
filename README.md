# 🎮 Ascend LifeQuest

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

**Une application gamifiée de gestion de tâches quotidiennes propulsée par l'IA**

[Fonctionnalités](#-fonctionnalités) • [Architecture](#-architecture) • [Installation](#-installation) • [Technologies](#-technologies) • [Développeurs](#-développeurs)

</div>

---

## 📖 Description

**Ascend LifeQuest** transforme votre vie quotidienne en une aventure épique. Cette application Android innovante utilise l'intelligence artificielle (Ollama avec Llama 3.3) pour générer automatiquement des quêtes personnalisées basées sur vos préférences et les conditions météorologiques locales.

Gagnez de l'expérience, montez en niveau, défiiez vos amis sur le classement mondial et maintenez votre streak en complétant des tâches quotidiennes enrichissantes dans différentes catégories : sport, alimentation, loisirs, créativité, et bien plus encore !

---

## ✨ Fonctionnalités

### 🎯 Génération de Quêtes IA
- **Génération automatique** de 5 quêtes quotidiennes via Ollama (Llama 3.3)
- **Personnalisation** basée sur vos préférences de catégories
- **Adaptation météorologique** : les quêtes s'ajustent selon la météo locale
- **Renouvellement quotidien** : nouvelles quêtes chaque jour à minuit
- **Gestion multi-utilisateurs** : quêtes individualisées par compte

### 🏆 Système de Progression
- **Système d'XP** : gagnez de l'expérience en complétant des quêtes
- **Niveaux et rangs** : progressez et débloquez de nouveaux défis
- **Streak tracking** : maintenez votre série de jours consécutifs
- **Statistiques détaillées** : suivez vos performances au fil du temps

### 👥 Fonctionnalités Sociales
- **Système d'amis** : ajoutez et gérez vos contacts
- **Classement mondial** : comparez-vous aux autres joueurs
- **Messagerie instantanée** : discutez avec vos amis en temps réel
- **Notifications** : restez informé des demandes d'amis et messages

### ⚙️ Personnalisation
- **Préférences de catégories** : choisissez les types de quêtes que vous préférez
- **Gestion du profil** : personnalisez votre avatar et vos informations
- **Paramètres de compte** : contrôlez vos données et vos préférences

### 🌦️ Intégration Météo
- **Géolocalisation automatique** : détection de votre position
- **Quêtes contextuelles** : activités adaptées aux conditions météo
- **Widget météo** : affichage en temps réel dans l'interface

---

## 🏗️ Architecture

Le projet suit une architecture **MVVM (Model-View-ViewModel)** moderne et maintainable :

```
app/src/main/java/com/example/ascendlifequest/
├── data/
│   ├── auth/                    # Gestion de l'authentification
│   │   ├── AuthRepository.kt
│   │   └── AuthRepositoryImpl.kt
│   ├── model/                   # Modèles de données
│   │   ├── Quest.kt
│   │   ├── Categorie.kt
│   │   ├── UserProfile.kt
│   │   ├── Message.kt
│   │   ├── Friendship.kt
│   │   └── ...
│   └── repository/              # Repositories
│       ├── QuestRepository.kt
│       ├── QuestGeneratorRepository.kt
│       ├── ProfileRepository.kt
│       ├── FriendRepository.kt
│       └── MessageRepository.kt
├── database/                    # Room Database
│   ├── AppDatabase.kt
│   ├── QuestEntity.kt
│   └── QuestDao.kt
├── di/                          # Dependency Injection
├── ui/
│   ├── components/              # Composants réutilisables
│   │   ├── WeatherWidget.kt
│   │   ├── QuestCard.kt
│   │   └── ...
│   ├── features/                # Écrans et ViewModels
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   ├── RegisterScreen.kt
│   │   │   └── LoginViewModel.kt
│   │   ├── quest/
│   │   │   ├── QuestScreen.kt
│   │   │   └── QuestViewModel.kt
│   │   ├── profile/
│   │   ├── friends/
│   │   ├── chat/
│   │   ├── leaderboard/
│   │   └── settings/
│   └── theme/                   # Thème Material 3
├── util/                        # Utilitaires
│   ├── QuestHelper.kt
│   ├── CategorySelector.kt
│   └── ...
└── MainActivity.kt
```

### Principes architecturaux

- **Séparation des responsabilités** : UI, logique métier et données sont strictement séparées
- **Flux de données unidirectionnel** : utilisation de StateFlow pour la gestion d'état
- **Injection de dépendances** : facilite les tests et la maintenabilité
- **Repository Pattern** : abstraction de l'accès aux données
- **Coroutines Kotlin** : gestion asynchrone moderne et efficace

---

## 🚀 Installation

### Prérequis

- **Android Studio** : Hedgehog (2023.1.1) ou supérieur
- **JDK** : Version 17
- **Android SDK** : API 24 (Android 7.0) minimum, API 36 (Android 14) recommandé
- **Ollama** : Serveur local avec modèle Llama 3.3 (pour la génération de quêtes)
- **Firebase** : Projet configuré avec Authentication et Firestore

### Configuration

1. **Cloner le repository**
   ```bash
   git clone https://github.com/votre-repo/Ascend-LifeQuest.git
   cd Ascend-LifeQuest
   ```

2. **Configuration Firebase**
   - Créez un projet Firebase sur [console.firebase.google.com](https://console.firebase.google.com)
   - Activez **Firebase Authentication** (Email/Password)
   - Activez **Cloud Firestore**
   - Téléchargez `google-services.json` et placez-le dans `app/`

3. **Configuration Ollama** (pour la génération IA)
   
   **Option A : Serveur local**
   ```bash
   # Installez Ollama
   curl -fsSL https://ollama.ai/install.sh | sh
   
   # Téléchargez le modèle Llama 3.3
   ollama pull llama3.3:latest
   
   # Lancez le serveur
   ollama serve
   ```
   
   **Option B : Serveur distant via SSH**
   ```bash
   ssh -L 11434:localhost:11434 user@your-server.com
   ```
   
   Ensuite, configurez l'URL dans `QuestGeneratorRepository.kt` :
   - Émulateur Android : `http://10.0.2.2:11434`
   - Appareil physique : utilisez `adb reverse` ou l'IP locale

4. **Configuration locale**
   - Créez ou modifiez `local.properties` :
     ```properties
     sdk.dir=/path/to/your/Android/Sdk
     ```

5. **Build et exécution**
   ```bash
   # Via ligne de commande
   ./gradlew assembleDebug
   
   # Ou ouvrez le projet dans Android Studio et cliquez sur Run
   ```

---

## 🛠️ Technologies

### Frontend
- **Kotlin** 2.0.21 - Langage de programmation moderne
- **Jetpack Compose** - Framework UI déclaratif
- **Material Design 3** - Design system moderne
- **Compose Navigation** - Navigation entre écrans
- **Material Icons Extended** - Bibliothèque d'icônes complète

### Backend & Data
- **Firebase Authentication** - Gestion des utilisateurs
- **Cloud Firestore** - Base de données NoSQL en temps réel
- **Room Database** - Base de données locale SQLite
- **Retrofit** - Client HTTP pour les API REST
- **OkHttp** - Client HTTP performant

### IA & Services
- **Ollama** - Serveur LLM local
- **Llama 3.3** - Modèle de langage pour la génération de quêtes
- **OpenWeatherMap API** - Données météorologiques
- **Google Play Services Location** - Géolocalisation

### Architecture & Patterns
- **MVVM** - Architecture Model-View-ViewModel
- **StateFlow** - Gestion d'état réactive
- **Coroutines** - Programmation asynchrone
- **KSP** - Kotlin Symbol Processing (Room)
- **Dependency Injection** - Injection manuelle structurée

### Tests
- **JUnit 4** - Framework de tests unitaires
- **Kotlinx Coroutines Test** - Tests de coroutines
- **Turbine** - Tests de Flow
- **AndroidX Test** - Tests instrumentés
- **Espresso** - Tests UI

### CI/CD
- **GitHub Actions** - Intégration continue
- **Workflows personnalisés** :
  - Build & Tests automatiques
  - Vérification de la qualité du code (Lint)
  - Génération d'APK de release
  - Tests nocturnes programmés
  - Checks sur les Pull Requests

---

## 🧪 Tests

Le projet inclut une suite complète de tests unitaires et d'intégration :

```bash
# Exécuter tous les tests unitaires
./gradlew test

# Exécuter les tests instrumentés
./gradlew connectedAndroidTest

# Génerer un rapport de couverture
./gradlew jacocoTestReport
```

### Structure des tests
- **Tests unitaires** : `app/src/test/` - Tests des ViewModels, Repositories et logique métier
- **Tests instrumentés** : `app/src/androidTest/` - Tests UI et intégration Android
- **Fakes** : Implémentations mock pour les tests isolés

---

## 👨‍💻 Développeurs

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/identicons/ZeMonkee.png" width="100px;" alt="Léo Periou"/>
      <br />
      <sub><b>Léo Periou</b></sub>
      <br />
      <a href="https://github.com/ZeMonkee" title="GitHub">🔗 GitHub</a>
    </td>
    <td align="center">
      <img src="https://github.com/identicons/ArcLeDepart.png" width="100px;" alt="Corentin Gas"/>
      <br />
      <sub><b>Corentin Gas</b></sub>
      <br />
      <a href="https://github.com/ArcLeDepart" title="GitHub">🔗 GitHub</a>
    </td>
  </tr>
</table>

**Projet développé dans le cadre du cours de Lionel Banand.**

---

## 📞 Support

Pour toute question ou problème :
- 🐛 Ouvrez une [issue](https://github.com/votre-repo/Ascend-LifeQuest/issues)
- 💬 Contactez les développeurs
- 📧 Email : leo.periou@etu.univ-lyon.fr, corentin.gas@etu.univ-lyon1.fr

---

<div align="center">

**Fait avec ❤️ par Léo Periou et Corentin Gas**

⭐ Si vous aimez ce projet, n'hésitez pas à lui donner une étoile !

</div>
