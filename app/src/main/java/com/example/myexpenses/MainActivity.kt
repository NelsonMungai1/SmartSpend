package com.example.myexpenses

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var PasswordInput: EditText
    lateinit var loginBtn: Button
    lateinit var registertext: TextView
    lateinit var forgotPasswordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        usernameInput = findViewById(R.id.username_input)
        PasswordInput = findViewById(R.id.Password_input)
        loginBtn = findViewById(R.id.login_button)
        registertext = findViewById(R.id.register_text)
        forgotPasswordText = findViewById(R.id.forgot_password_text)

        loginBtn.setOnClickListener {
            // 1. Read and remove whitespaces
            val username = usernameInput.text.toString().trim()
            val password = PasswordInput.text.toString().trim()

            // 2. Check if the fields are empty before we login
            if (username.isEmpty()) {
                usernameInput.error = "Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                PasswordInput.error = "Password is required"
                PasswordInput.requestFocus()
                return@setOnClickListener
            }

            // 3. Read saved account from SharedPreferences
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val savedUsername = sharedPreferences.getString("username", null)
            val savedEmail = sharedPreferences.getString("email", null)
            val savedPassword = sharedPreferences.getString("password", null)

            // Check if any user has registered yet
            if (savedUsername == null || savedPassword == null) {
                Toast.makeText(
                    this,
                    "No account found! Please register first.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Check if the entered username (or email) and password match
            val isUserMatch = username.equals(savedUsername, ignoreCase = true) ||
                    (savedEmail != null && username.equals(savedEmail, ignoreCase = true))

            if (!isUserMatch) {
                usernameInput.error = "Invalid username or email"
                usernameInput.requestFocus()
                return@setOnClickListener
            }

            if (password != savedPassword) {
                PasswordInput.error = "Incorrect password"
                PasswordInput.requestFocus()
                return@setOnClickListener
            }

            // 4. Credentials match -> Login successful!
            Toast.makeText(this, "Welcome back, $savedUsername!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        // Click on "Forgot Password?"
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Click on "Don't have an account? Register"
        registertext.setOnClickListener {
            val intent = Intent(this, Registration_Activity::class.java)
            startActivity(intent)
        }
    }
}