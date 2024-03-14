package com.example.trail2weather

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)

        // Initialize the RadioGroup
        val themeRadioGroup: RadioGroup = findViewById(R.id.themeRadioGroup)

        // Set the selected radio button based on the saved theme
        when (sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_YES -> themeRadioGroup.check(R.id.radioDarkTheme)
            AppCompatDelegate.MODE_NIGHT_NO -> themeRadioGroup.check(R.id.radioLightTheme)
            else -> themeRadioGroup.clearCheck()
        }

        // Set a listener to handle radio button changes
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioLightTheme -> setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.id.radioDarkTheme -> setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun setThemeMode(mode: Int) {
        // Save the selected theme mode
        sharedPreferences.edit().putInt("theme", mode).apply()

        // Set the theme mode for the current session
        AppCompatDelegate.setDefaultNightMode(mode)

        // Restart the activity to apply the new theme
        recreate()
    }

    override fun onPause() {
        // Save the selected theme mode when the activity is paused
        sharedPreferences.edit().putInt("theme", AppCompatDelegate.getDefaultNightMode()).apply()

        super.onPause()
    }
}
