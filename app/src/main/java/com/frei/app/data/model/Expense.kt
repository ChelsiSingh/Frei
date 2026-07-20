package com.frei.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class ExpenseCategory(val label: String) {
    FLIGHT("Flights"),
    HOTEL("Hotels"),
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    OTHER("Other")
}

enum class ExpenseSource { AUTO, MANUAL }

data class Expense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tripId: String? = null,
    val tripName: String? = null,
    val title: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: Double = 0.0,
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val timestamp: Timestamp = Timestamp.now()
)

data class TripExpenseGroup(
    val tripId: String,
    val tripName: String,
    val dateRange: String,
    val expenses: List<Expense>
) {
    val total: Double get() = expenses.sumOf { it.amount }
}

data class CategoryTotal(
    val category: ExpenseCategory,
    val amount: Double,
    val share: Float
)

data class TripOption(
    val id: String,
    val name: String
)