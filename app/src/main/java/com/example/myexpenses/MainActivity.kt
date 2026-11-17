package com.example.myexpenses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var usernameInput : EditText
    lateinit var PasswordInput : EditText
    lateinit var loginBtn : Button
    lateinit var registertext : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        usernameInput=findViewById(R.id.username_input)
        PasswordInput=findViewById(R.id.Password_input)
        loginBtn=findViewById(R.id.login_button)
        registertext=findViewById(R.id.register_text)

        loginBtn.setOnClickListener {
            //Read and remove Whitespaces
            val username=usernameInput.text.toString().trim()
            val password=PasswordInput.text.toString().trim()
            //Check if the fields are empty before we login
            if(username.isEmpty()){
                usernameInput.error="Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener//stop and don't go to next screen
            }
            if(password.isEmpty()){
                PasswordInput.error="Password is Required"
                PasswordInput.requestFocus()
                return@setOnClickListener
            }
            //3.If both are filled, navigate to Home
            Toast.makeText(this,"Login Successful!", Toast.LENGTH_SHORT).show()
            val intent= Intent(this, Home::class.java)
            startActivity(intent)
            //Log.i("Test Credentials", "Username : $username and Password: $password")
            finish()
        }
        //Click on the Registertext
        registertext.setOnClickListener {
            val intent= Intent(this, Registration_Activity::class.java)
            startActivity(intent)
        }


    }
}