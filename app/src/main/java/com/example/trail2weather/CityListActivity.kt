package com.example.trail2weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.trail2weather.CityDatabaseHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import java.io.IOException
import java.util.*

class CityListActivity : AppCompatActivity() {
    private lateinit var databaseHelper: CityDatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cityContainer: LinearLayout
    private var savedCities: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_gallery)

        databaseHelper = CityDatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val searchCityEditText = findViewById<EditText>(R.id.searchCityEditText)
        val addCityButton = findViewById<Button>(R.id.addCityButton)
        val locationButton = findViewById<ImageButton>(R.id.locationButton)
        cityContainer = findViewById(R.id.cityRadioGroup)

        // Load saved cities from the database
        savedCities = databaseHelper.getAllCities().toMutableList()
        for (cityName in savedCities) {
            addCityCardView(cityContainer, cityName)
        }

        // Add a city when the "Add City" button is clicked
        addCityButton.setOnClickListener {
            val cityName = searchCityEditText.text.toString()
            if (cityName.isNotEmpty() && !savedCities.contains(cityName)) {
                // Create and add a new city (CardView) with a delete button
                addCityCardView(cityContainer, cityName)

                // Save the city to the database
                databaseHelper.insertCity(cityName)
                savedCities.add(cityName)  // Add the new city to the savedCities list

                // Clear the search bar
                searchCityEditText.text.clear()
            } else {
                displayErrorMessage("City already exists or name is empty")
            }
        }

        // Handle the location button click
        locationButton.setOnClickListener {
            getLocationAndAddToCityList()
        }
    }

    private fun addCityCardView(container: LinearLayout, cityName: String) {
        val cityItem = layoutInflater.inflate(R.layout.city_item, container, false) as CardView

        val cityNameTextView = cityItem.findViewById<TextView>(R.id.cityNameTextView)
        val deleteButton = cityItem.findViewById<ImageButton>(R.id.deleteButton)

        cityNameTextView.text = cityName

        deleteButton.setOnClickListener {
            // Remove the city when the delete button is clicked
            container.removeView(cityItem)
            databaseHelper.deleteCity(cityName)
            savedCities.remove(cityName)  // Remove the city from the savedCities list
        }

        cityItem.setOnClickListener {
            // Send the cityName to MainActivity
            val intent = Intent()
            intent.putExtra("cityName", cityName)
            setResult(RESULT_OK, intent)
            finish() // Close CityListActivity
        }


        container.addView(cityItem)
    }


    private fun getLocationAndAddToCityList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val geocoder = android.location.Geocoder(this, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        Log.d("Location", "After geocoder.getFromLocation")

                        if (addresses != null && addresses.isNotEmpty()) {
                            val cityName = addresses[0].locality
                            if (!cityName.isNullOrBlank() && !savedCities.contains(cityName)) {
                                // Create and add the city (CardView) with a delete button
                                addCityCardView(cityContainer, cityName)

                                // Save the city to the database
                                databaseHelper.insertCity(cityName)
                                savedCities.add(cityName)  // Add the new city to the savedCities list
                            } else if (cityName.isNullOrBlank()) {
                                // Handle the case where the city name is not found
                                displayErrorMessage("City name not found")
                            } else {
                                displayErrorMessage("City already exists")
                            }
                        } else {
                            // Handle the case where no address is found
                            displayErrorMessage("No address found")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        // Handle any exceptions
                        Log.e("Location", "IOException: " + e.message)
                        displayErrorMessage("An error occurred while getting location data")
                    }
                }
            })
    }

    private fun displayErrorMessage(message: String) {
        // You can use a Toast to display an error message to the user
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
