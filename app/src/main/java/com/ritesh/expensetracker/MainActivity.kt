package com.ritesh.expensetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Expense(
    val name: String,
    val amount: Double,
    val category: String,
    val date: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ExpenseTrackerApp(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(context: Context) {

    val preferences = remember {
        context.getSharedPreferences("expense_data", Context.MODE_PRIVATE)
    }

    var expenses by remember {
        mutableStateOf(loadExpenses(preferences))
    }

    var showAddExpense by remember {
        mutableStateOf(false)
    }

    if (showAddExpense) {

        AddExpenseScreen(
            onBack = {
                showAddExpense = false
            },
            onSave = { name, amount, category ->

                val newExpense = Expense(
                    name = name,
                    amount = amount,
                    category = category,
                    date = SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(Date())
                )

                expenses = expenses + newExpense

                saveExpenses(
                    preferences,
                    expenses
                )

                showAddExpense = false
            }
        )

    } else {

        HomeScreen(
            expenses = expenses,
            onAddExpense = {
                showAddExpense = true
            },
            onDelete = { index ->

                expenses = expenses
                    .filterIndexed { i, _ -> i != index }

                saveExpenses(
                    preferences,
                    expenses
                )
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenses: List<Expense>,
    onAddExpense: () -> Unit,
    onDelete: (Int) -> Unit
) {

    val totalSpent = expenses.sumOf {
        it.amount
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("💰 Expense Tracker")
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Total Spent",
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "₹%.2f".format(totalSpent),
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "➕ Add Expense",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Recent Expenses",
                fontSize = 21.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            if (expenses.isEmpty()) {

                Text(
                    text = "No expenses yet.\nTap Add Expense to get started."
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    itemsIndexed(
                        expenses.reversed()
                    ) { reversedIndex, expense ->

                        val originalIndex =
                            expenses.size - 1 - reversedIndex

                        ExpenseCard(
                            expense = expense,
                            onDelete = {
                                onDelete(originalIndex)
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onSave: (String, Double, String) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("Food")
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Food",
        "Travel",
        "Shopping",
        "Bills",
        "Entertainment",
        "Education",
        "Health",
        "Other"
    )

    val amountValue = amount.toDoubleOrNull()

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("➕ Add Expense")
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = {
                    Text("Expense name")
                },
                placeholder = {
                    Text("Example: Lunch")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                },
                label = {
                    Text("Amount (₹)")
                },
                placeholder = {
                    Text("Example: 150")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = {
                    categoryExpanded = !categoryExpanded
                }
            ) {

                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Category")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = categoryExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = {
                        categoryExpanded = false
                    }
                ) {

                    categories.forEach { item ->

                        DropdownMenuItem(
                            text = {
                                Text(item)
                            },
                            onClick = {

                                category = item

                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {

                    if (
                        name.isNotBlank() &&
                        amountValue != null &&
                        amountValue > 0
                    ) {

                        onSave(
                            name.trim(),
                            amountValue,
                            category
                        )
                    }
                },
                enabled = name.isNotBlank()
                        && amountValue != null
                        && amountValue > 0,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "💾 Save Expense",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Cancel")
            }
        }
    }
}


@Composable
fun ExpenseCard(
    expense: Expense,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = expense.name,
                    fontSize = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "${expense.category} • ${expense.date}",
                    fontSize = 13.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "₹%.2f".format(expense.amount),
                    fontSize = 18.sp
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                OutlinedButton(
                    onClick = onDelete,
                ) {

                    Text("Delete")
                }
            }
        }
    }
}


fun saveExpenses(
    preferences: android.content.SharedPreferences,
    expenses: List<Expense>
) {

    val jsonArray = JSONArray()

    expenses.forEach { expense ->

        val objectJson = JSONObject()

        objectJson.put("name", expense.name)
        objectJson.put("amount", expense.amount)
        objectJson.put("category", expense.category)
        objectJson.put("date", expense.date)

        jsonArray.put(objectJson)
    }

    preferences.edit()
        .putString(
            "expenses",
            jsonArray.toString()
        )
        .apply()
}


fun loadExpenses(
    preferences: android.content.SharedPreferences
): List<Expense> {

    val savedData =
        preferences.getString(
            "expenses",
            null
        ) ?: return emptyList()

    return try {

        val jsonArray = JSONArray(savedData)

        val result = mutableListOf<Expense>()

        for (i in 0 until jsonArray.length()) {

            val item =
                jsonArray.getJSONObject(i)

            result.add(
                Expense(
                    name = item.getString("name"),
                    amount = item.getDouble("amount"),
                    category = item.getString("category"),
                    date = item.getString("date")
                )
            )
        }

        result

    } catch (e: Exception) {

        emptyList()
    }
}