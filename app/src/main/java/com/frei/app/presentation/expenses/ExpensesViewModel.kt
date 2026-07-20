package com.frei.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frei.app.data.repository.ExpenseRepository
import com.frei.app.data.model.CategoryTotal
import com.frei.app.data.model.Expense
import com.frei.app.data.model.ExpenseCategory
import com.frei.app.data.model.ExpenseSource
import com.frei.app.data.model.TripExpenseGroup
import com.frei.app.data.model.TripOption
import com.frei.app.presentation.expenses.groupByDateSection
import com.frei.app.presentation.expenses.groupByTrip
import com.frei.app.presentation.expenses.isSameMonth
import com.frei.app.presentation.expenses.toCategoryTotals
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ExpenseViewMode { FLAT, BY_TRIP }

data class ExpensesUiState(
    val isLoading: Boolean = true,
    val viewMode: ExpenseViewMode = ExpenseViewMode.FLAT,
    val selectedCategory: ExpenseCategory? = null,
    val monthlyBudget: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val tripCount: Int = 0,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val groupedByDate: Map<String, List<Expense>> = emptyMap(),
    val groupedByTrip: List<TripExpenseGroup> = emptyList()
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val viewMode = MutableStateFlow(ExpenseViewMode.FLAT)
    private val selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    private val userId get() = auth.currentUser?.uid.orEmpty()

    val uiState: StateFlow<ExpensesUiState> = combine(
        repository.observeExpenses(userId),
        repository.observeMonthlyBudget(userId),
        viewMode,
        selectedCategory
    ) { expenses, budget, mode, category ->
        val thisMonth = expenses.filter { it.timestamp.toDate().isSameMonth(Date()) }
        val filtered = category?.let { cat -> thisMonth.filter { it.category == cat } } ?: thisMonth

        ExpensesUiState(
            isLoading = false,
            viewMode = mode,
            selectedCategory = category,
            monthlyBudget = budget,
            monthlySpent = thisMonth.sumOf { it.amount },
            tripCount = thisMonth.mapNotNull { it.tripId }.distinct().size,
            categoryTotals = thisMonth.toCategoryTotals(),
            groupedByDate = filtered.groupByDateSection(),
            groupedByTrip = filtered.groupByTrip()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpensesUiState())

    val tripOptions: StateFlow<List<TripOption>> = repository.observeUserTrips(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setViewMode(mode: ExpenseViewMode) {
        viewMode.value = mode
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        selectedCategory.value = category
    }

    fun addManualExpense(
        title: String,
        category: ExpenseCategory,
        amount: Double,
        tripId: String? = null,
        tripName: String? = null,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        if (userId.isEmpty()) {
            onResult(Result.failure(IllegalStateException("You need to be signed in to add an expense.")))
            return
        }
        viewModelScope.launch {
            val result = runCatching {
                repository.addExpense(
                    Expense(
                        userId = userId,
                        tripId = tripId,
                        tripName = tripName,
                        title = title,
                        category = category,
                        amount = amount,
                        source = ExpenseSource.MANUAL
                    )
                )
            }
            onResult(result)
        }
    }

    fun setMonthlyBudget(amount: Double, onResult: (Result<Unit>) -> Unit = {}) {
        if (userId.isEmpty()) {
            onResult(Result.failure(IllegalStateException("You need to be signed in to set a budget.")))
            return
        }
        viewModelScope.launch {
            val result = runCatching { repository.setMonthlyBudget(userId, amount) }
            onResult(result)
        }
    }
}