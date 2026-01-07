package com.masoud.weather;

public class WeatherUtils {

    // Helper method to convert OpenWeatherMap icon codes to Emojis
    public static String getWeatherEmoji(String iconCode) {
        switch (iconCode) {
            // Clear Sky
            case "01d": return "☀️"; // Day
            case "01n": return "🌙"; // Night

            // Few Clouds
            case "02d": return "⛅"; // Day
            case "02n": return "☁️"; // Night

            // Scattered Clouds (Same emoji for day/night)
            case "03d":
            case "03n": return "☁️";

            // Broken Clouds (Same emoji for day/night)
            case "04d":
            case "04n": return "☁️";

            // Shower Rain (Same emoji for day/night)
            case "09d":
            case "09n": return "🌧️";

            // Rain
            case "10d": return "🌦️"; // Day sun/rain
            case "10n": return "🌧️"; // Night rain

            // Thunderstorm (Same emoji for day/night)
            case "11d":
            case "11n": return "⛈️";

            // Snow (Same emoji for day/night)
            case "13d":
            case "13n": return "❄️";

            // Mist/Fog (Same emoji for day/night)
            case "50d":
            case "50n": return "🌫️";

            default: return "❓"; // Unknown weather
        }
    }
}
