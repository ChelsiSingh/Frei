package com.frei.app.data.repository

import com.frei.app.data.model.Expense
import com.frei.app.data.model.TripOption
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ExpenseRepository {
    fun observeExpenses(userId: String): Flow<List<Expense>>
    fun observeExpensesForTrip(userId: String, tripId: String): Flow<List<Expense>>
    fun observeMonthlyBudget(userId: String): Flow<Double>
    fun observeUserTrips(userId: String): Flow<List<TripOption>>
    suspend fun addExpense(expense: Expense)
    suspend fun setMonthlyBudget(userId: String, amount: Double)
    suspend fun getTripName(tripId: String): String?
}

@Singleton
class FirestoreExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    private companion object {
        const val EXPENSES_COLLECTION = "expenses"
        const val BUDGETS_COLLECTION = "budgets"
        const val TRIPS_COLLECTION = "trips"
    }

    override fun observeExpenses(userId: String): Flow<List<Expense>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    override fun observeMonthlyBudget(userId: String): Flow<Double> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(0.0)
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection(BUDGETS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0.0)
                    return@addSnapshotListener
                }
                trySend(snapshot?.getDouble("monthlyLimit") ?: 0.0)
            }
        awaitClose { listener.remove() }
    }


    override fun observeUserTrips(userId: String): Flow<List<TripOption>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection(TRIPS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val trips = snapshot?.documents?.map { doc ->
                    TripOption(id = doc.id, name = doc.getString("name") ?: "Trip")
                } ?: emptyList()
                trySend(trips)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addExpense(expense: Expense) {
        firestore.collection(EXPENSES_COLLECTION)
            .add(expense)
            .await()
    }

    override suspend fun setMonthlyBudget(userId: String, amount: Double) {
        firestore.collection(BUDGETS_COLLECTION)
            .document(userId)
            .set(mapOf("monthlyLimit" to amount), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    // Same field-name assumption as observeUserTrips — see the comment there.
    override suspend fun getTripName(tripId: String): String? {
        if (tripId.isEmpty()) return null
        return runCatching {
            firestore.collection(TRIPS_COLLECTION).document(tripId).get().await().getString("name")
        }.getOrNull()
    }

    override fun observeExpensesForTrip(userId: String, tripId: String): Flow<List<Expense>> = callbackFlow {
        if (userId.isEmpty() || tripId.isEmpty()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("tripId", tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java)
                    ?.sortedByDescending { it.timestamp.seconds } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }
}