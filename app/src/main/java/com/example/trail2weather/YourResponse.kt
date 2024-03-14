package com.example.trail2weather

data class YourResponse(
    val status: String,
    val message: String,
    val data: ImageData
)

data class ImageData(
    val imageUrl: String
)
