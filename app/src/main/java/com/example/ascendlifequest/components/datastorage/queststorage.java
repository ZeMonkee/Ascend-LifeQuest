package com.example.ascendlifequest.components.datastorage;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ascendlifequest.fake_data.FakeCategorieKt;
import com.example.ascendlifequest.fake_data.FakeQuestKt;
import com.example.ascendlifequest.model.Categorie;
import com.example.ascendlifequest.model.Quest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class queststorage {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void uploadFakeData() {
        Log.d("queststorage", "🔥 MÉTHODE uploadFakeData() APPELÉE !");

        // Upload des quêtes
        for (Quest quest : FakeQuestKt.getF_Quests()) {
            Map<String, Object> questMap = getStringObjectMap(quest);

            db.collection("quest")
                    .document("quest_" + quest.getId())
                    .set(questMap)
                    .addOnSuccessListener(aVoid -> Log.d("QuestStorage", "✅ Quest '" + quest.getNom() + "' uploaded"))
                    .addOnFailureListener(e -> Log.e("QuestStorage", "❌ Failed to upload quest: " + quest.getNom(), e));
        }

        // Upload des catégories
        for (Categorie categorie : FakeCategorieKt.getF_Categorie()) {
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("id", categorie.getId());
            catMap.put("nom", categorie.getNom());
            catMap.put("icon", categorie.getIcon());

            // ✅ Stocker la couleur comme Long (0xAARRGGBB)
            catMap.put("color", categorie.getColorValue());

            db.collection("categories")
                    .document("categorie_" + categorie.getId())
                    .set(catMap)
                    .addOnSuccessListener(aVoid -> Log.d("QuestStorage", "✅ Categorie '" + categorie.getNom() + "' uploaded"))
                    .addOnFailureListener(e -> Log.e("QuestStorage", "❌ Failed to upload categorie: " + categorie.getNom(), e));
        }
    }

    @NonNull
    private static Map<String, Object> getStringObjectMap(Quest quest) {
        Map<String, Object> questMap = new HashMap<>();
        questMap.put("id", quest.getId());
        questMap.put("categorie", quest.getCategorie());
        questMap.put("nom", quest.getNom());
        questMap.put("description", quest.getDescription());
        questMap.put("preferenceRequis", quest.getPreferenceRequis());
        questMap.put("xpRapporte", quest.getXpRapporte());
        questMap.put("tempsNecessaire", quest.getTempsNecessaireMinutes());
        questMap.put("dependantMeteo", quest.getDependantMeteo());
        return questMap;
    }
}