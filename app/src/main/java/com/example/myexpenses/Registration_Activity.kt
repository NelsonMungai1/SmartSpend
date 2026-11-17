package com.example.myexpenses

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registration_Activity : AppCompatActivity() {
    lateinit var usernameInput: EditText
    lateinit var emailInput: EditText
    lateinit var passwordInput : EditText
    lateinit var confirmPasswordInput: EditText
    lateinit var registerBtn : Button
    lateinit var loginRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        usernameInput=findViewById(R.id.reg_username_input)
        emailInput=findViewById(R.id.reg_email_input)
        passwordInput=findViewById(R.id.reg_password_input)
        confirmPasswordInput=findViewById(R.id.reg_confirm_password_input)
        registerBtn=findViewById(R.id.register_button)
        loginRedirectText=findViewById(R.id.login_redirect_text)

        registerBtn.setOnClickListener {
            val username=usernameInput.text.toString().trim()
            val email=emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword=confirmPasswordInput.text.toString().trim()

            //1. Validate inputs
            if(username.isEmpty()){
                usernameInput.error="Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener
            }
            if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInput.error="Enter a valid email"
                emailInput.requestFocus()
                return@setOnClickListener
            }
            if(password.length<6){
                passwordInput.error="Password must be atleast 6 characters"
                passwordInput.requestFocus()
                return@setOnClickListener
            }
            //Check if both passwords match
            if(password!=confirmPassword){
                confirmPasswordInput.error="Passwords do not match"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }

            //2. Save user Credentials locally in SharedPreferences
            val sharedPreferences=getSharedPreferences("UserPrefs",MODE_PRIVATE)
            val editor=sharedPreferences.edit()
            editor.putString("username",username)
            editor.putString("email",email)
            editor.putString("password",password)
            Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_SHORT).show()
            editor.apply()

            //Display pop up using Toast and go back to Login
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        //if user Clicks "Already have an account? Login "
        loginRedirectText.setOnClickListener {
            finish()//closes the RegisterActivity and goes back to mainActivity(Login Page)
        }

    }
}