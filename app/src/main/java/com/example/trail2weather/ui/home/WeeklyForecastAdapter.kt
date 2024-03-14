package com.example.trail2weather.ui.home


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trail2weather.R  // Replace with your actual package name

class WeeklyForecastAdapter(
    private val dateList: List<String>,
    private val maxTempList: List<Double>,
    private val minTempList: List<Double>
) : RecyclerView.Adapter<WeeklyForecastAdapter.WeeklyForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weekly_forecast, parent, false)
        return WeeklyForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyForecastViewHolder, position: Int) {
        val date = dateList[position]
        val maxTemp = maxTempList[position]
        val minTemp = minTempList[position]

        holder.bind(date, maxTemp, minTemp)
    }

    override fun getItemCount(): Int = dateList.size

    class WeeklyForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        private val maxTempTextView: TextView = view.findViewById(R.id.maxTempTextView)
        private val minTempTextView: TextView = view.findViewById(R.id.minTempTextView)

        fun bind(date: String, maxTemp: Double, minTemp: Double) {
            dateTextView.text = date
            maxTempTextView.text = "${maxTemp}°C"
            minTempTextView.text = "${minTemp}°C"
        }
    }
}
