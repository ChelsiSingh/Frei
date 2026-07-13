package com.frei.app.data.repository

import com.frei.app.presentation.packing.PackingCategory
import com.frei.app.presentation.packing.PackingItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

object PackingRepository {

    // --- SAVE DATA TO FIREBASE ---
    fun savePackingListToFirestore(
        userId: String,
        tripId: String,
        categories: List<PackingCategory>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (tripId.isBlank()) {
            onFailure(IllegalArgumentException("Trip ID cannot be blank"))
            return
        }

        val db = FirebaseFirestore.getInstance()
        val dataToSave = hashMapOf(
            "lastUpdated" to Timestamp.now(),
            "categories" to categories.map { category ->
                hashMapOf(
                    "id" to category.id,
                    "name" to category.name,
                    "items" to category.items.map { item ->
                        hashMapOf(
                            "id" to item.id,
                            "name" to item.name,
                            "isPacked" to item.isPacked
                        )
                    }
                )
            }
        )

        // ✅ Point directly into your new flat root trips layout
        db.collection("trips").document(tripId)
            .collection("packing").document("checklist")
            .set(dataToSave)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // --- FETCH DATA FROM FIREBASE ---
    fun fetchPackingListFromFirestore(
        userId: String,
        tripId: String,
        onSuccess: (List<PackingCategory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (tripId.isBlank()) {
            onFailure(IllegalArgumentException("Trip ID cannot be blank"))
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("trips").document(tripId)
            .collection("packing").document("checklist")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        val rawCategories = document.get("categories") as? List<*>
                        if (rawCategories != null) {
                            val categoriesList = rawCategories.mapNotNull { catObj ->
                                val catMap = catObj as? Map<*, *> ?: return@mapNotNull null

                                val rawItems = catMap["items"] as? List<*>
                                val itemsList = rawItems?.mapNotNull { itemObj ->
                                    val itemMap = itemObj as? Map<*, *> ?: return@mapNotNull null

                                    PackingItem(
                                        id = itemMap["id"] as? String ?: "",
                                        name = itemMap["name"] as? String ?: "",
                                        isPacked = itemMap["isPacked"] as? Boolean ?: false
                                    )
                                } ?: emptyList()

                                PackingCategory(
                                    id = catMap["id"] as? String ?: "",
                                    name = catMap["name"] as? String ?: "",
                                    items = itemsList
                                )
                            }

                            onSuccess(categoriesList)
                        } else {
                            onSuccess(emptyList())
                        }
                    } catch (e: Exception) {
                        onFailure(e)
                    }
                } else {
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}