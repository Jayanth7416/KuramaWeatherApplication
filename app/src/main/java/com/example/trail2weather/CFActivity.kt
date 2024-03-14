package com.example.trail2weather

// CFActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trail2weather.ui.home.HomeFragment

class CFActivity : AppCompatActivity() {

    private lateinit var btnConvertCtoF: Button
    private lateinit var btnConvertFtoC: Button
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cf)

        btnConvertCtoF = findViewById(R.id.btnConvertCtoF)
        btnConvertFtoC = findViewById(R.id.btnConvertFtoC)
        tvResult = findViewById(R.id.tvResult)

        btnConvertCtoF.setOnClickListener {
            // Handle the click event for "Convert C to F" button
        }

        btnConvertFtoC.setOnClickListener {
            // Handle the click event for "Convert F to C" button
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
            Toast.makeText(this, "Changed to Celcius", Toast.LENGTH_SHORT).show()

        }
    }

}
