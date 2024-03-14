package com.example.trail2weather.ui.gallery

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.trail2weather.CityDatabaseHelper
import com.example.trail2weather.MainActivity
import com.example.trail2weather.R
import com.example.trail2weather.databinding.FragmentGalleryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val SPEECH_REQUEST_CODE = 101
    private lateinit var databaseHelper: CityDatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cityContainer: LinearLayout
    private lateinit var root: View // Define root at the class level
    private var savedCities: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        databaseHelper = CityDatabaseHelper(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val microphoneButton = root.findViewById<ImageButton>(R.id.microphoneId)

        // Handle the microphone button click
        microphoneButton.setOnClickListener {
            // Check and request microphone permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    SPEECH_REQUEST_CODE
                )
            } else {
                // Permission already granted, start speech recognition
                startSpeechRecognition()
            }
        }

        val searchCityEditText = root.findViewById<EditText>(R.id.searchCityEditText)
        val addCityButton = root.findViewById<Button>(R.id.addCityButton)
        val locationButton = root.findViewById<ImageButton>(R.id.locationButton)
        cityContainer = root.findViewById(R.id.cityRadioGroup)

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
            var syracuse = "Syracuse"
            if (syracuse.isNotEmpty() && !savedCities.contains(syracuse)) {
                // Create and add a new city (CardView) with a delete button
                addCityCardView(cityContainer, "Syracuse")

                // Save the city to the database
                databaseHelper.insertCity(syracuse)
                savedCities.add(syracuse)  // Add the new city to the savedCities list

                // Clear the search bar
                searchCityEditText.text.clear()
                getLocationAndAddToCityList()
            } else {
                displayErrorMessage("City already exists or name is empty")
            }
        }

        return root
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            SPEECH_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start speech recognition
                    startSpeechRecognition()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message)
                    Toast.makeText(requireContext(), "Microphone permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Speech recognition not supported on your device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.isNotEmpty()) {
                val recognizedText = result[0]
                updateEditTextWithSpeech(recognizedText)
            }
        }
    }

    private fun updateEditTextWithSpeech(text: String) {
        // Get reference to your EditText field
        val searchCityEditText = view?.findViewById<EditText>(R.id.searchCityEditText)

        // Update the EditText with the recognized speech text
        searchCityEditText?.setText(text)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            val cityName = cityName
            // Send the cityName to HomeFragment
            (activity as MainActivity).showHomeFragment(cityName)
            Log.d("cityName_1", "Name of the City transfered to other Activity: ${cityName}")

        }

        container.addView(cityItem)
    }

    private fun getLocationAndAddToCityList() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(requireActivity()) { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val geocoder = android.location.Geocoder(requireContext(), Locale.getDefault())
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
            }
    }

    private fun displayErrorMessage(message: String) {
        // You can use a Toast to display an error message to the user
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
