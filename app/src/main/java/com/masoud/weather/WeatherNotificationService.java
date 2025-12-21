package com.masoud.weather;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class WeatherNotificationService extends Service {
    public static final String CHANNEL_ID = "WeatherServiceChannel";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_REFRESH = "ACTION_REFRESH";

    // Model that were gonna showcase its temp in notification
    private WeatherModel currentModel;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    // After android sdk 26 we need to make a notification channel first
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Weather Notification Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We get the action that was passed to the intent and handle it
        if (intent != null) {
            String action = intent.getAction();

            // Remove the notification
            if (ACTION_STOP.equals(action)) {
                stopForeground(true);
                stopSelf();
                return START_NOT_STICKY;
            }
            // Get the model passed from main activity
            if (intent.hasExtra("weather_model")) currentModel = (WeatherModel) intent.getSerializableExtra("weather_model");

            if (currentModel != null) {
                // In case of refresh action call api other than that show the current data inside notification
                if (ACTION_REFRESH.equals(action)) fetchWeatherData();
                else showNotification(currentModel.getTemp(), currentModel.getWeather_emoji());
            }
        }
        return START_STICKY;
    }

    // Builds the actual notification
    private void showNotification(int temp, String weatherEmoji) {

        // Set PendingIntents that we can use later on for this specific action
        // Refresh action
        Intent refreshIntent = new Intent(this, WeatherNotificationService.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra("weather_model", currentModel);
        PendingIntent pendingRefreshIntent = PendingIntent.getService(this, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Stop Action
        Intent stopIntent = new Intent(this, WeatherNotificationService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Click on notification action
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingOpenAppIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        // The notification itself
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Current Weather")
                .setContentText(weatherEmoji + " " + temp + "℃")
                .setSmallIcon(R.drawable.weather)
                .setContentIntent(pendingOpenAppIntent)
                .addAction(R.drawable.rotate, "Refresh", pendingRefreshIntent)
                .addAction(R.drawable.close, "Remove", pendingStopIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    // On Refresh action we fetch the data again from the api
    private void fetchWeatherData() {
        if (currentModel == null) return;

        String url = String.format(Locale.ENGLISH, "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s",
                currentModel.getLat(), currentModel.getLon(), BuildConfig.API_KEY);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        int temp = (int) main.getDouble("temp");

                        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                        String iconCode = weather.getString("icon");
                        String emoji = MainActivity.getWeatherEmoji(iconCode);

                        showNotification(temp, emoji);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed Parsing Weather", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed To Refresh", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
