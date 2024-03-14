package com.example.trail2weather


// LoginActivity.kt
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var togglePasswordButton: ImageButton
    private lateinit var editTextPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userRepository = UserRepository(this)
        editTextPassword = findViewById(R.id.editTextPassword)
        togglePasswordButton = findViewById(R.id.togglePassword)

        togglePasswordButton.setOnClickListener {
            togglePasswordVisibility()
        }

        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            val user = userRepository.getUser(username, password)

            if (user != null) {
                showToast("Login successful")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                // Finish LoginActivity
                // Perform actions for successful login
            } else {
                showToast("Login failed. Please check your credentials.")
                // Perform actions for failed login
            }
        }

        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            val userId = userRepository.insertUser(username, password)

            if (userId != -1L) {
                showToast("Registration successful")
                // Clear input fields after successful registration
                editTextUsername.text.clear()
                editTextPassword.text.clear()
            } else {
                showToast("Registration failed. Please provide a non-empty username and password.")
                // Perform actions for failed registration
            }
        }
    }

    private fun togglePasswordVisibility() {
        val inputType = if (editTextPassword.inputType == 129) {
            145 // Set to 'text' type
        } else {
            129 // Set to 'textPassword' type
        }

        editTextPassword.inputType = inputType

        // Change the visibility icon
        val icon = if (inputType == 129) {
            R.drawable.baseline_visibility_off_24
        } else {
            R.drawable.baseline_visibility_24
        }

        togglePasswordButton.setImageDrawable(ContextCompat.getDrawable(this, icon))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
