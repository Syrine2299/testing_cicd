package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.db.WeatherDatabase;
import com.example.weatherapp.db.entities.cityName;
import com.example.weatherapp.db.entities.cityWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

public class Screen2 extends AppCompatActivity {


    private WeatherDatabase db;
    private String name, url;
    private TextView cityTextView, countryTextView, temperatureTextView, descriptionTextView;
    private TextView windSpeedTextView, windDirectionTextView, pressureTextView, humdidityTextView;
    private JSONObject find;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        setContentView(R.layout.activity_screen2);

        cityTextView = (TextView) findViewById(R.id.city_name_textView);
        countryTextView = (TextView) findViewById(R.id.country_name_textView);
        temperatureTextView = (TextView) findViewById(R.id.temperature_textView);
        descriptionTextView = (TextView) findViewById(R.id.description_textView);
        windSpeedTextView = (TextView) findViewById(R.id.wind_speed_textView);
        windDirectionTextView = (TextView) findViewById(R.id.wind_direction_textView);
        pressureTextView = (TextView) findViewById(R.id.pressure_textView);
        humdidityTextView = (TextView) findViewById(R.id.humidity_textView);

        db = Room.databaseBuilder(this, WeatherDatabase.class, "student-db").allowMainThreadQueries().build();


        name = getIntent().getStringExtra("name");
        url = "https://api.weatherbit.io/v2.0/current?&city=" + name + "&country=TN&key=30da7e2cda51424a87b5ada86df2535b";


        Search();


    }


    private void Search() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                // Get the city from the database or add it if it doesn't exist
                cityName city = db.CityDAO().getCity(name.toLowerCase());
                if (city == null) {
                    cityName c = new cityName();
                    c.cityName = name;
                    db.CityDAO().insert(c);
                    Toast.makeText(this, "City added to database.", Toast.LENGTH_SHORT).show();
                }

                cityName city1 = db.CityDAO().getCity(name);
                Toast.makeText(this, "City name is: " + city1.cityName, Toast.LENGTH_SHORT).show();

                // Get the data array from the API response
                JSONArray details = response.getJSONArray("data");

                // Check if the data array is empty
                if (details.length() == 0) {
                    Toast.makeText(this, "No weather data found for this city.", Toast.LENGTH_SHORT).show();
                    resetUI();
                    return; // Exit early if no data is found
                }

                // Get the first city's details (assuming there is at least one city in the data array)
                find = details.getJSONObject(0);

                // Check if the city is in Tunisia
                if (!find.getString("country_code").equals("TN")) {
                    Toast.makeText(this, "City not found in Tunisia. Please enter a Tunisian city.", Toast.LENGTH_SHORT).show();
                    resetUI();
                    return; // Exit early if the city is not in Tunisia
                }

                // Update the UI with weather details for cities in Tunisia
                cityTextView.setText(find.getString("city_name"));
                countryTextView.setText("Tunisia");
                temperatureTextView.setText(find.getString("temp") + " °C");
                JSONObject weather = find.getJSONObject("weather");
                String desc = weather.getString("description");
                descriptionTextView.setText(desc);
                windSpeedTextView.setText(find.getString("wind_spd"));
                windDirectionTextView.setText(find.getString("wind_dir"));
                pressureTextView.setText(find.getString("pres"));
                humdidityTextView.setText(find.getString("rh"));

                // Save weather data to the database
                cityWeather cityWeathers = new cityWeather();
                cityWeathers.cityid = city1.cityid;
                cityWeathers.description = weather.getString("description");
                cityWeathers.windSpeed = find.getString("wind_spd");
                cityWeathers.humidity = find.getString("rh");
                cityWeathers.percip = find.getString("pres");
                cityWeathers.windDir = find.getString("wind_dir");
                cityWeathers.recordDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                cityWeathers.temperature = find.getString("temp") + " °C";

                db.CityDAO().insert(cityWeathers);

            } catch (JSONException e) {
                Toast.makeText(this, "Something went wrong, please try again and check the city name.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(this, "Failed to fetch data. Check your internet connection.", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(request);
    }

    // Reset UI elements when the city is not found in Tunisia
    private void resetUI() {
        cityTextView.setText("");
        countryTextView.setText("");
        temperatureTextView.setText("");
        descriptionTextView.setText("");
        windSpeedTextView.setText("");
        windDirectionTextView.setText("");
        pressureTextView.setText("");
        humdidityTextView.setText("");
    }
}