package com.example.myexpenses

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class Home : AppCompatActivity() {

    lateinit var welcomeText: TextView
    lateinit var logoutBtn: Button
    lateinit var netBalanceText: TextView
    lateinit var totalIncomeText: TextView
    lateinit var totalExpenseText: TextView
    lateinit var totalFeesText: TextView
    lateinit var simulateSmsBtn: Button
    lateinit var transactionsListText: TextView

    // Variables to hold our running totals
    var totalIncome = 0.0
    var totalExpense = 0.0
    var totalFees = 0.0
    var transactionHistory = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Connect XML views to Kotlin variables
        welcomeText = findViewById(R.id.welcome_text)
        logoutBtn = findViewById(R.id.logout_button)
        netBalanceText = findViewById(R.id.net_balance_text)
        totalIncomeText = findViewById(R.id.total_income_text)
        totalExpenseText = findViewById(R.id.total_expense_text)
        totalFeesText = findViewById(R.id.total_fees_text)
        simulateSmsBtn = findViewById(R.id.simulate_sms_button)
        transactionsListText = findViewById(R.id.transactions_list_text)

        // 2. Load saved user and saved transaction totals
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")
        welcomeText.text = "Welcome, $username!"

        loadSavedData()
        updateDashboardUI()

        // 3. Click listener for "Paste / Test M-Pesa SMS"
        simulateSmsBtn.setOnClickListener {
            showPasteSmsDialog()
        }

        // 4. Logout button
        logoutBtn.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Dialog to paste or test a sample M-Pesa SMS
    private fun showPasteSmsDialog() {
        val sampleSms = "SCD45XYZ12 Confirmed. Ksh1,500.00 sent to JOHN DOE 0712345678 on 14/4/26 at 2:15 PM. New M-PESA balance is Ksh4,200.00. Transaction cost, Ksh23.00."

        val inputBox = EditText(this)
        inputBox.setText(sampleSms)
        inputBox.setPadding(40, 30, 40, 30)

        AlertDialog.Builder(this)
            .setTitle("Paste or Test M-Pesa SMS")
            .setMessage("Edit or paste an M-Pesa message below:")
            .setView(inputBox)
            .setPositiveButton("Read SMS") { _, _ ->
                val smsText = inputBox.text.toString().trim()
                if (smsText.isNotEmpty()) {
                    parseAndPromptSms(smsText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Parses the M-Pesa SMS and FORCES the user to explain what the money was for
    private fun parseAndPromptSms(sms: String) {
        // 1. Determine if Money In or Money Out
        val isIncome = sms.contains("You have received", ignoreCase = true)

        // 2. Extract Transaction Cost first (e.g. "Transaction cost, Ksh23.00")
        val costRegex = Regex("""Transaction cost,?\s*Ksh\s*([\d,]+\.\d{2})""", RegexOption.IGNORE_CASE)
        val costMatch = costRegex.find(sms)
        val fee = costMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

        // 3. Extract the first Ksh Amount in the message (the transaction amount)
        val amountRegex = Regex("""Ksh\s*([\d,]+\.\d{2})""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(sms)
        val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

        if (amount == 0.0) {
            Toast.makeText(this, "Could not find a valid Ksh amount in SMS", Toast.LENGTH_LONG).show()
            return
        }

        // 4. Force popup asking: "What was this transaction for?"
        val reasonInput = EditText(this)
        reasonInput.hint = if (isIncome) "e.g., Client payment, Salary, Refund" else "e.g., KPLC tokens, Naivas groceries, Matatu"

        val typeLabel = if (isIncome) "MONEY IN (Income)" else "MONEY OUT (Expense)"

        val dialog = AlertDialog.Builder(this)
            .setTitle("New M-Pesa Transaction!")
            .setMessage(
                "$typeLabel\n" +
                        "Amount: Ksh ${formatMoney(amount)}\n" +
                        "M-Pesa Fee: Ksh ${formatMoney(fee)}\n\n" +
                        "What was this transaction for?"
            )
            .setView(reasonInput)
            .setCancelable(false) // Forces the user to answer before closing!
            .setPositiveButton("Save & Classify", null)
            .create()

        dialog.show()

        // Make sure the user doesn't leave the reason blank
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val reason = reasonInput.text.toString().trim()
            if (reason.isEmpty()) {
                reasonInput.error = "Please enter what this money was for!"
                return@setOnClickListener
            }

            // Classify the transaction based on the description
            val category = classifyDescription(reason, isIncome)

            // Update totals
            if (isIncome) {
                totalIncome += amount
            } else {
                totalExpense += amount
            }
            totalFees += fee

            // Add entry to recent transactions list
            val sign = if (isIncome) "+" else "-"
            val newEntry = "• [$category] $reason\n   $sign Ksh ${formatMoney(amount)} (Fee: Ksh ${formatMoney(fee)})"
            transactionHistory = if (transactionHistory.isEmpty()) {
                newEntry
            } else {
                "$newEntry\n\n$transactionHistory"
            }

            saveData()
            updateDashboardUI()
            Toast.makeText(this, "Categorized as: $category", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    // Smart Classifier: Classifies the user's description into a financial category
    private fun classifyDescription(description: String, isIncome: Boolean): String {
        val text = description.lowercase()
        if (isIncome) {
            return when {
                text.contains("salary") || text.contains("pay") -> "Salary & Wages"
                text.contains("client") || text.contains("business") || text.contains("sale") -> "Business Income"
                text.contains("gift") || text.contains("friend") || text.contains("mum") || text.contains("dad") -> "Family & Gifts"
                else -> "Other Income"
            }
        } else {
            return when {
                text.contains("kplc") || text.contains("token") || text.contains("water") || text.contains("wifi") || text.contains("zuku") || text.contains("safaricom") -> "Utilities & Bills"
                text.contains("naivas") || text.contains("quickmart") || text.contains("food") || text.contains("lunch") || text.contains("supper") || text.contains("mboga") || text.contains("milk") -> "Food & Groceries"
                text.contains("matatu") || text.contains("fare") || text.contains("uber") || text.contains("bolt") || text.contains("fuel") || text.contains("stage") -> "Transport"
                text.contains("rent") || text.contains("house") -> "Rent & Housing"
                text.contains("hospital") || text.contains("chemist") || text.contains("medicine") -> "Health"
                else -> "General Expense"
            }
        }
    }

    private fun updateDashboardUI() {
        val netBalance = totalIncome - totalExpense - totalFees
        netBalanceText.text = "Ksh ${formatMoney(netBalance)}"
        totalIncomeText.text = "Ksh ${formatMoney(totalIncome)}"
        totalExpenseText.text = "Ksh ${formatMoney(totalExpense)}"
        totalFeesText.text = "Ksh ${formatMoney(totalFees)}"

        if (transactionHistory.isNotEmpty()) {
            transactionsListText.text = transactionHistory
        }
    }

    private fun saveData() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        prefs.edit()
            .putFloat("totalIncome", totalIncome.toFloat())
            .putFloat("totalExpense", totalExpense.toFloat())
            .putFloat("totalFees", totalFees.toFloat())
            .putString("transactionHistory", transactionHistory)
            .apply()
    }

    private fun loadSavedData() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        totalIncome = prefs.getFloat("totalIncome", 0f).toDouble()
        totalExpense = prefs.getFloat("totalExpense", 0f).toDouble()
        totalFees = prefs.getFloat("totalFees", 0f).toDouble()
        transactionHistory = prefs.getString("transactionHistory", "") ?: ""
    }

    private fun formatMoney(amount: Double): String {
        return String.format(Locale.US, "%,.2f", amount)
    }
}