package com.example.trail2weather.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.transition.Fade
import androidx.fragment.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import com.google.android.youtube.player.YouTubePlayerView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.trail2weather.CityDatabaseHelper
import com.example.trail2weather.R
import com.example.trail2weather.databinding.FragmentHomeBinding
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.Gravity
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Method
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment(), PopupMenu.OnMenuItemClickListener, OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var cityList: List<String>
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost:3000") // Change this to your server URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private lateinit var refreshButton: Button
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var loadingDialog: Dialog
    private lateinit var currentLocation: ImageButton
    private lateinit var viewModel: HomeViewModel
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private var latitude: Double = 43.088947
    private var longitude: Double = -76.154480
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        if (!isNetworkAvailable()) {
            // No internet connection, inflate the broken_internet.xml layout
            val brokenInternetView = inflater.inflate(R.layout.broken_internet, container, false)
            refreshButton = brokenInternetView.findViewById(R.id.refreshButton)
            refreshButton.setOnClickListener{ onRefreshButtonClick() }

            return brokenInternetView
        }

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


//        fetchCurrentLocation()
        getLatLngFromCityName("Syracuse")

        Log.d("StartLocation", "Went through")

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                // Handle the result, e.g., process the captured image
                val imageBitmap = data?.extras?.get("data") as Bitmap
                // Now you can upload the image to your server
                uploadImageToServer(imageBitmap)
            }
        }

        val fab: FloatingActionButton = binding.root.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            // Create an AlertDialog with options for the user
            val options = arrayOf("Take Photo", "Choose from Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Option")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> takePhoto()
                        1 -> chooseFromGallery()
                    }
                }
                .show()
        }

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        // Fetch list of cities in our database
        cityList = getCityListFromDatabase()

        currentLocation = binding.root.findViewById(R.id.yourImageButtonId)
        currentLocation.setOnClickListener { getLatLngFromCityName("Syracuse")  }

        // Display the city list when cityListView is clicked
        binding.cityListView.setOnClickListener { showCityListPopup() }

        // Check if a city was previously selected
        val selectedCity = sharedPreferences.getString("selectedCity", null)

        if (selectedCity != null) {
            // A city was previously selected, load its data
            getLatLngFromCityName(selectedCity)
            sharedPreferences.edit().clear().apply()
        } else {
            // No city was previously selected, fetch current location
//            fetchCurrentLocation()
            getLatLngFromCityName("Syracuse")
            sharedPreferences.edit().clear().apply()
        }

        val arguments = arguments
        if (arguments != null && arguments.containsKey("cityName")) {
            val cityName = arguments.getString("cityName")
            if (cityName != null) {
                getLatLngFromCityName(cityName)
                Log.d("updating", "updating: $cityName")
            }
        }

        // Initialize MapView
        mapView = binding.mapView.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Call onMapReady if the map is already initialized
        if (::googleMap.isInitialized) {
            onMapReady(googleMap)
        }

        return root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Add a marker at the current location
        val currentLocation = LatLng(latitude, longitude)
        googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12f), 2000, null)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private interface YourApi {
        // Adjust the API endpoint and parameters based on your server's requirements
        @Multipart
        @POST("upload")
        fun uploadImage(@Part file: MultipartBody.Part): Call<YourResponse>
    }

    data class YourResponse(val success: Boolean, val message: String, val imageUrl: String?)

    private fun takePhoto() {
        // In your click listener
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private fun chooseFromGallery() {
        // Create an Intent to pick an image from the gallery
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE)
    }

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Photo captured from camera
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // Now you can upload the image to your server
                    uploadImageToServer(imageBitmap)
                }
                REQUEST_PICK_IMAGE -> {
                    // Photo picked from gallery
                    val imageUri = data?.data
                    // Now you can upload the image to your server
                    uploadImageToServer(imageUri)
                }
            }
        }
    }

    private fun uploadImageToServer(image: Bitmap) {
        val file = bitmapToFile(requireContext(), image)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val yourApi = retrofit.create(YourApi::class.java)
        val call = yourApi.uploadImage(part)

        call.enqueue(object : Callback<YourResponse> {
            override fun onResponse(call: Call<YourResponse>, response: Response<YourResponse>) {
                if (response.isSuccessful) {
                    val yourResponse = response.body()

                    if (yourResponse?.success == true) {
                        // Image uploaded successfully
                        val imageUrl = yourResponse.imageUrl

                        // Print the imageUrl to the console
                        imageUrl?.let {
                            Log.d("UploadedImageUrl", "Uploaded image URL: $it")
                            openBrowserWithUrl(it)

                        }


                    } else {
                        // Handle an unsuccessful response
                        // TODO: Handle the error
                    }
                } else {
                    // Handle an unsuccessful response
                    // TODO: Handle the error
                }
            }

            override fun onFailure(call: Call<YourResponse>, t: Throwable) {
                // Handle failure
                // TODO: Handle the error
            }
        })
    }

    private fun openBrowserWithUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun uploadImageToServer(imageUri: Uri?) {
        val file = File(getRealPathFromUri(requireContext(), imageUri))
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val yourApi = retrofit.create(YourApi::class.java)
        val call = yourApi.uploadImage(part)

        call.enqueue(object : Callback<YourResponse> {
            override fun onResponse(call: Call<YourResponse>, response: Response<YourResponse>) {
                if (response.isSuccessful) {
                    val yourResponse = response.body()

                    if (yourResponse?.success == true) {
                        // Image uploaded successfully
                        val imageUrl = yourResponse.imageUrl

                        // Print the imageUrl to the console
                        imageUrl?.let {
                            Log.d("UploadedImageUrl", "Uploaded image URL: $it")

                            // Open the browser to view the uploaded image
                            openBrowserWithUrl(it)
                        }
                    } else {
                        // Handle an unsuccessful response
                        // TODO: Handle the error
                    }
                } else {
                    // Handle an unsuccessful response
                    // TODO: Handle the error
                }
            }

            override fun onFailure(call: Call<YourResponse>, t: Throwable) {
                // Handle failure
                // TODO: Handle the error
            }
        })

    }

    private fun getRealPathFromUri(context: Context, uri: Uri?): String {
        val cursor = context.contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val filePath = cursor.getString(columnIndex)
        cursor.close()
        return filePath
    }

    private fun bitmapToFile(context: Context, bitmap: Bitmap): File {
        // Create a temporary file to save the bitmap
        val file = File(context.cacheDir, "temp_image.jpg")
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val fos = FileOutputStream(file)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {

        Toast.makeText(requireContext(), "Selected city: ${item.title}", Toast.LENGTH_SHORT).show()

        val cityName1 = item.title.toString()
        saveSelectedCity(cityName1)

        // Set the flag to true to indicate that the location has been fetched
        viewModel.locationFetched = true
        getLatLngFromCityName(cityName1)

        // Call the function to update the map
        updateMapWithCityLocation(latitude, longitude)
        return true
    }

    private fun updateMapWithCityLocation(latitude: Double, longitude: Double) {
        // Clear existing markers on the map

        mapView.getMapAsync { map ->
            map.clear() // Clear existing markers on the map

            // Add a marker at the new location
            val newLocation = LatLng(latitude, longitude)
            map.addMarker(MarkerOptions().position(newLocation).title("New Location"))
            map.moveCamera(CameraUpdateFactory.newLatLng(newLocation))
            map.animateCamera(CameraUpdateFactory.zoomTo(12f), 2000, null)
        }
    }

    private fun saveSelectedCity(cityName: String) {
        // Save the selected city to SharedPreferences
        sharedPreferences.edit().putString("selectedCity", cityName).apply()
        viewModel.locationFetched = true
        Log.d("selected_city", "selected_city: $cityName")
    }

    private fun onRefreshButtonClick() {
        if (isNetworkAvailable()){
            getLatLngFromCityName("Syracuse")
            _binding = FragmentHomeBinding.inflate(layoutInflater)
        }
        else{
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    private fun showCityListPopup() {
        val popup = PopupMenu(requireContext(), binding.cityListView, Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, R.style.PopupMenuStyle)
        popup.menuInflater.inflate(R.menu.city_list_menu, popup.menu)

        // Add cities to the popup menu dynamically
        for (city in cityList) {
            popup.menu.add(city)
        }

        // Set a click listener for the menu items
        popup.setOnMenuItemClickListener(this)

        // Show the popup menu
        popup.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save your fragment's state here
        outState.putDouble("latitude", latitude)
        outState.putDouble("longitude", longitude)
        Log.d("onSaveInstance", "Latitude: $latitude, Longitude: $longitude")
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // Restore your fragment's state here
        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("latitude", 0.0)
            longitude = savedInstanceState.getDouble("longitude", 0.0)
            // Restore any other necessary data
        }
    }

    private fun getLatLngFromCityName(cityName: String) {
        val apiKey = "AIzaSyA4Z8xADCS6hTsOBhMeMHF9pWrc-dczRQ4"  // Replace with your actual API key
        val geocodingApiUrl = "https://maps.googleapis.com/maps/api/geocode/"

        val retrofit = Retrofit.Builder()
            .baseUrl(geocodingApiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        Log.d("BeforeFetch", "Before fetching Location")
        val geocodingService = retrofit.create(GeocodingService::class.java)

        val call = geocodingService.getLatLng(cityName, apiKey)

        call.enqueue(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                if (response.isSuccessful) {
                    val geocodingResponse = response.body()


                    // Check if the response contains results
                    if (!geocodingResponse?.results.isNullOrEmpty()) {
                        val location = geocodingResponse!!.results[0].geometry.location

                        // Use the obtained latitude and longitude
                        val latitude = location.lat
                        val longitude = location.lng
                        Log.d("AfterFetch", "After fetching Location")


                        Log.d("Geocoding", "Latitude: $latitude, Longitude: $longitude")

                        fetchWeatherData(latitude, longitude)
                        fetchOpenMeteoHourlyForecast(latitude, longitude)
                        fetchOpenMeteoWeeklyForecast(latitude, longitude)
                        updateMapWithCityLocation(latitude, longitude)

                    } else {
                        // Handle no results found for the given city
                    }
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun getCityListFromDatabase(): List<String> {
        val dbHelper = CityDatabaseHelper(requireContext())
        return dbHelper.getAllCities()
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        sharedPreferences.edit().clear()
        _binding = null
    }

    private fun fetchCurrentLocation() {
        val locationManager = context?.let { ContextCompat.getSystemService(it, LocationManager::class.java) }
        Log.d("current_location", "current location accessed")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION) } ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        // Use the obtained latitude and longitude
                        latitude = it.latitude
                        longitude = it.longitude
                        Log.d("current_location_after", "current location accessed")

                        // Fetch weather data using the obtained location
                        fetchWeatherData(latitude, longitude)
                        // Fetch Hourly data and update UI
                        fetchOpenMeteoHourlyForecast(latitude, longitude)
                        fetchOpenMeteoWeeklyForecast(latitude, longitude)
                        updateMapWithCityLocation(latitude, longitude)

                    }
                }
            }
        } else {
            // For devices with SDK < 23, use the deprecated method
            val location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let {
                // Use the obtained latitude and longitude
                latitude = 43.0481
                longitude = -76.154480

                // Fetch weather data using the obtained location
                fetchWeatherData(latitude, longitude)
                // Fetch Hourly data and update UI
                fetchOpenMeteoHourlyForecast(latitude, longitude)
                fetchOpenMeteoWeeklyForecast(latitude, longitude)
                updateMapWithCityLocation(latitude, longitude)
            }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        // Use the obtained latitude and longitude
        val call = weatherService.getWeather(latitude, longitude, "4d0969d72fa8f025af927d2125067037")
        Log.d("FetchWeather", "Latitude: $latitude, Longitude: $longitude")

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()

//                    // Save latitude and longitude
//                    latitude = weatherResponse?.coord?.lat ?: 0.0
//                    longitude = weatherResponse?.coord?.lon ?: 0.0

                    updateUI(weatherResponse)
                    fetchOpenMeteoHourlyForecast(latitude, longitude)
                    fetchOpenMeteoWeeklyForecast(latitude, longitude)
                    updateMapWithCityLocation(latitude, longitude)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun fetchOpenMeteoHourlyForecast(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")  // Open Meteo API endpoint
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        // Fetch hourly forecast from Open Meteo API
        Log.d("WeatherHourly", "Latitude: ${latitude}, Longitude: ${longitude}")

        val hourlyCall = weatherService.getOpenMeteoHourlyForecast(latitude, longitude, "temperature_2m", 1)

        hourlyCall.enqueue(object : Callback<HourlyForecastResponse> {
            override fun onResponse(
                call: Call<HourlyForecastResponse>,
                response: Response<HourlyForecastResponse>
            ) {
                if (response.isSuccessful) {
                    val hourlyResponse = response.body()
                    // Process and display Open Meteo hourly forecast data
                    displayOpenMeteoHourlyForecast(hourlyResponse)
                }
            }

            override fun onFailure(call: Call<HourlyForecastResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun fetchOpenMeteoWeeklyForecast(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        Log.d("WeatherWeekly", "Latitude: ${latitude}, Longitude: ${longitude}")

        val weeklyCall = weatherService.getOpenMeteoWeeklyForecast(latitude, longitude)

        weeklyCall.enqueue(object : Callback<WeeklyForecastResponse> {
            override fun onResponse(
                call: Call<WeeklyForecastResponse>,
                response: Response<WeeklyForecastResponse>
            ) {
                if (response.isSuccessful) {
                    val weeklyResponse = response.body()
                    displayOpenMeteoWeeklyForecast(weeklyResponse)
                }
            }

            override fun onFailure(call: Call<WeeklyForecastResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun displayOpenMeteoWeeklyForecast(weeklyResponse: WeeklyForecastResponse?) {
        if (weeklyResponse != null) {
            val dateList = weeklyResponse.daily.time
            val maxTempList = weeklyResponse.daily.temperature_2m_max
            val minTempList = weeklyResponse.daily.temperature_2m_min

            Log.d("WeatherWeeklyDisplay", "Date: ${dateList}, MaxTemp: ${maxTempList}, MainTemp: ${minTempList}")

            val weeklyAdapter = WeeklyForecastAdapter(dateList, maxTempList, minTempList)

            binding.weeklyForecastRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.weeklyForecastRecyclerView.adapter = weeklyAdapter
        }
    }

    private fun displayOpenMeteoHourlyForecast(hourlyResponse: HourlyForecastResponse?) {
        if (hourlyResponse != null) {
            // Extract temperature data from the hourly object
            val timeList = hourlyResponse.hourly.time
            val temperatureList = hourlyResponse.hourly.temperature_2m

            // Create RecyclerView adapter
            val adapter = HourlyForecastAdapter(timeList, temperatureList)

            // Attach the adapter to the RecyclerView
            binding.hourlyForecastRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.hourlyForecastRecyclerView.adapter = adapter
        }
    }

    private fun updateUI(weatherResponse: WeatherResponse?) {
        if (weatherResponse != null) {
            binding.cityNameTextView.text = weatherResponse.name

            Log.d("UpdateNormal", "Latitude: ${latitude}, Longitude: ${longitude}")

            // Convert temperature from Kelvin to Celsius
            val temperatureCelsius = weatherResponse.main.temp - 273.15
            binding.temperatureTextView.text = "${String.format("%.1f", temperatureCelsius)}°C"

            binding.weatherConditionTextView.text = "Weather: ${weatherResponse.weather[0].description}"

            // Convert temperatures from Kelvin to Celsius
            val maxTempCelsius = weatherResponse.main.temp_max - 273.15
            val minTempCelsius = weatherResponse.main.temp_min - 273.15
            binding.highTempTextView.text = "High: ${String.format("%.1f", maxTempCelsius)}°C"
            binding.lowTempTextView.text = "Low: ${String.format("%.1f", minTempCelsius)}°C"

            // Convert feels like temperature from Kelvin to Celsius
            val feelsLikeCelsius = weatherResponse.main.feels_like - 273.15
            binding.feelsLikeTextView.text = "Feels like: ${String.format("%.1f", feelsLikeCelsius)}°C"

            binding.humidityTextView.text = "Humidity: ${weatherResponse.main.humidity}%"
            binding.pressureTextView.text = "Pressure: ${weatherResponse.main.pressure} hPa"
            binding.windTextView.text = "Wind: ${weatherResponse.wind.speed} km/h"

            binding.sunsetTextView.text = "Sunset: ${weatherResponse.sys.sunset}"
            binding.sunriseTextView.text = "Sunrise: ${weatherResponse.sys.sunrise}"

            val sunriseTime = calculateLocalTime(weatherResponse.sys.sunrise, weatherResponse.timezone)
            val sunsetTime = calculateLocalTime(weatherResponse.sys.sunset, weatherResponse.timezone)

            binding.sunriseTextView.text = "Sunrise: $sunriseTime"
            binding.sunsetTextView.text = "Sunset: $sunsetTime"

            // Load weather icon using Glide or any other image loading library
            Glide.with(this)
                .load("https://openweathermap.org/img/w/${weatherResponse.weather[0].icon}.png")
                .into(binding.weatherIconImageView)
        }
    }

    private fun calculateLocalTime(utcTime: Long, timezoneOffset: Int): String {
        val utcMillis = utcTime * 1000L
        val instant = Instant.ofEpochMilli(utcMillis)
        val zoneId = ZoneId.ofOffset("GMT", ZoneOffset.ofTotalSeconds(timezoneOffset))

        val localDateTime = LocalDateTime.ofInstant(instant, zoneId)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        return localDateTime.format(formatter)
    }
}