package com.frei.app.presentation.expenses

import com.frei.app.data.model.CategoryTotal
import com.frei.app.data.model.Expense
import com.frei.app.data.model.TripExpenseGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun Calendar.resetToStartOfDay(): Calendar = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Date.isSameMonth(other: Date): Boolean {
    val a = Calendar.getInstance().apply { time = this@isSameMonth }
    val b = Calendar.getInstance().apply { time = other }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
}

fun List<Expense>.groupByDateSection(now: Date = Date()): Map<String, List<Expense>> {
    val today = Calendar.getInstance().apply { time = now }.resetToStartOfDay()
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DATE, -1) }
    val weekStart = (today.clone() as Calendar).apply { add(Calendar.DATE, -7) }

    val sorted = sortedByDescending { it.timestamp.toDate() }
    val buckets = linkedMapOf<String, MutableList<Expense>>()

    for (expense in sorted) {
        val expDay = Calendar.getInstance().apply { time = expense.timestamp.toDate() }.resetToStartOfDay()
        val key = when {
            expDay.timeInMillis == today.timeInMillis -> "Today"
            expDay.timeInMillis == yesterday.timeInMillis -> "Yesterday"
            expDay.after(weekStart) -> "This week"
            else -> "Earlier"
        }
        buckets.getOrPut(key) { mutableListOf() }.add(expense)
    }
    return listOf("Today", "Yesterday", "This week", "Earlier")
        .mapNotNull { key -> buckets[key]?.let { key to it } }
        .toMap()
}

/** Groups expenses by trip, newest trip first, sorted by most recent expense in each trip. */
fun List<Expense>.groupByTrip(): List<TripExpenseGroup> {
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return filter { it.tripId != null }
        .groupBy { it.tripId!! }
        .map { (tripId, expenses) ->
            val sortedExpenses = expenses.sortedByDescending { it.timestamp.toDate() }
            val dates = expenses.map { it.timestamp.toDate() }.sorted()
            val range = if (dates.isNotEmpty()) {
                "${dateFormat.format(dates.first())} – ${dateFormat.format(dates.last())}"
            } else ""
            TripExpenseGroup(
                tripId = tripId,
                tripName = expenses.first().tripName ?: "Trip",
                dateRange = range,
                expenses = sortedExpenses
            )
        }
        .sortedByDescending { group -> group.expenses.maxOf { it.timestamp.toDate() } }
}


fun List<Expense>.toCategoryTotals(): List<CategoryTotal> {
    val total = sumOf { it.amount }
    if (total <= 0.0) return emptyList()
    return groupBy { it.category }
        .map { (category, expenses) ->
            val amount = expenses.sumOf { it.amount }
            CategoryTotal(category, amount, (amount / total).toFloat())
        }
        .sortedByDescending { it.amount }
}