package com.example.trail2weather.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trail2weather.R

class HourlyForecastAdapter(
    private val timeList: List<String>,
    private val temperatureList: List<Double>
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>() {

    class HourlyForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val dateTime = timeList[position]
        val time = formatTime(dateTime)
        val temperature = "${temperatureList[position]}°C"

        holder.timeTextView.text = time
        holder.temperatureTextView.text = temperature
    }

    private fun formatTime(dateTime: String): String {
        // Assuming dateTime is in the format "yyyy-MM-ddTHH:mm"
        val parts = dateTime.split("T")
        val time = parts[1].substring(0, 5)  // Extracting only the time part (HH:mm)
        return time
    }

    override fun getItemCount(): Int {
        return timeList.size
    }

}
