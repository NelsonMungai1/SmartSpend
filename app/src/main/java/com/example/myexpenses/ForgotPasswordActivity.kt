package com.example.myexpenses

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var emailInput: EditText
    lateinit var newPasswordInput: EditText
    lateinit var confirmPasswordInput: EditText
    lateinit var resetPasswordBtn: Button
    lateinit var backToLoginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        emailInput = findViewById(R.id.reset_email_input)
        newPasswordInput = findViewById(R.id.reset_new_password_input)
        confirmPasswordInput = findViewById(R.id.reset_confirm_password_input)
        resetPasswordBtn = findViewById(R.id.reset_password_button)
        backToLoginText = findViewById(R.id.back_to_login_text)

        resetPasswordBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // 1. Validate inputs
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                newPasswordInput.error = "Password must be at least 6 characters"
                newPasswordInput.requestFocus()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }

            // 2. Check if the entered email matches the saved account in SharedPreferences
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("email", null)

            if (savedEmail == null || !savedEmail.equals(email, ignoreCase = true)) {
                emailInput.error = "No account found with this email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            // 3. Save the new password
            sharedPreferences.edit().putString("password", newPassword).apply()

            Toast.makeText(this, "Password reset successful! Please login.", Toast.LENGTH_SHORT).show()
            finish() // Return to Login screen
        }

        backToLoginText.setOnClickListener {
            finish() // Return to Login screen
        }
    }
}