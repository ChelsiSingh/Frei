package com.frei.app.presentation.packing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frei.app.data.repository.PackingRepository

// FIXED: Remove random UUID generators from default constructors
data class PackingItem(
    val id: String = "",
    val name: String,
    val isPacked: Boolean = false
)

data class PackingCategory(
    val id: String = "",
    val name: String,
    val items: List<PackingItem> = emptyList()
)

val FreiLightBg = Color(0xFFF7F6FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingScreen(
    tripId: String,
    onBackClick: () -> Unit,
    onSaveClick: (List<PackingCategory>) -> Unit
) {
    var categories by remember { mutableStateOf<List<PackingCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var activeCategoryForNewItem by remember { mutableStateOf<PackingCategory?>(null) }

    LaunchedEffect(tripId) {
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        if (currentUid.isNotEmpty()) {
            // FIXED: Passing real dynamic currentUid instead of a hardcoded string
            PackingRepository.fetchPackingListFromFirestore(
                userId = currentUid,
                tripId = tripId,
                onSuccess = { fetchedList ->
                    categories = fetchedList
                    isLoading = false
                },
                onFailure = {
                    isLoading = false
                }
            )
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FreiPurple)
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Packing List", color = FreiInk, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = FreiInk
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onSaveClick(categories) }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save packing list",
                                tint = FreiPurple
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Add Category", fontWeight = FontWeight.Bold) },
                    containerColor = FreiPurple,
                    contentColor = Color.White
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(FreiLightBg)
            ) {
                if (categories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No categories yet. Tap + to start packing!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(categories, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                onAddItemClick = { activeCategoryForNewItem = category },
                                onItemToggle = { itemId: String ->
                                    categories = categories.map { cat: PackingCategory ->
                                        if (cat.id == category.id) {
                                            cat.copy(items = cat.items.map { item: PackingItem ->
                                                if (item.id == itemId) item.copy(isPacked = !item.isPacked) else item
                                            })
                                        } else cat
                                    }
                                }
                            )
                        }
                    }
                }

                if (showAddCategoryDialog) {
                    InputDialog(
                        title = "New Category",
                        placeholder = "e.g., Clothes, Toiletries",
                        onDismiss = { showAddCategoryDialog = false },
                        onConfirm = { name: String ->
                            if (name.isNotBlank()) {
                                // Explicitly generate the random ID here ONLY on manual user creation
                                val generatedId = java.util.UUID.randomUUID().toString()
                                categories = categories + PackingCategory(id = generatedId, name = name)
                            }
                            showAddCategoryDialog = false
                        }
                    )
                }

                activeCategoryForNewItem?.let { category ->
                    InputDialog(
                        title = "Add to ${category.name}",
                        placeholder = "e.g., T-shirts, Toothbrush",
                        onDismiss = { activeCategoryForNewItem = null },
                        onConfirm = { itemName: String ->
                            if (itemName.isNotBlank()) {
                                // Explicitly generate item ID here ONLY on manual user creation
                                val generatedItemId = java.util.UUID.randomUUID().toString()
                                categories = categories.map { cat: PackingCategory ->
                                    if (cat.id == category.id) {
                                        cat.copy(items = cat.items + PackingItem(id = generatedItemId, name = itemName))
                                    } else cat
                                }
                            }
                            activeCategoryForNewItem = null
                        }
                    )
                }
            }
        }
    }
}