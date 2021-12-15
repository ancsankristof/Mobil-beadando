package hu.unideb.mobilbeadando;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener {

    private TextView TempText;
    private TextView timezonedata;
    private TextView weatherdata;
    private TextView weatherlike;
    private TextView TempFaren;
    private ProgressBar tempBar;
    private ConstraintLayout applayout;

    private SensorManager SensorManager;
    private Sensor TempSensor;
    private Boolean SensorActive;

    private LocationManager LocationManager;

    private double longitude;
    private double latitude;
    private float apitemp;
    private int RunCount=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        TempText = findViewById(R.id.TempText);
        TempFaren = findViewById(R.id.TempFaren);
        applayout = findViewById(R.id.applayout);
        weatherdata = findViewById(R.id.weatherdata);
        timezonedata = findViewById(R.id.timezonedata);
        weatherlike = findViewById(R.id.weatherlike);
        tempBar = (ProgressBar) findViewById(R.id.tempBar);

        tempBar.setScaleX(10f);
        tempBar.setScaleY(7f);
        tempBar.getProgressDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);

        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);


        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
        ) {
            LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }


        if(SensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)!=null){
            TempSensor= SensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            SensorActive=true;
        }
        else{
            TempText.setText("Homero szenzor nem elerheto!");
            TempFaren.setText("Homero szenzor nem elerheto!");
            SensorActive = false;

            if (apitemp <15){
                applayout.setBackgroundResource(R.drawable.backgroundcold);
            }
            else {
                applayout.setBackgroundResource(R.drawable.backgroundhot);
            }
        }



        //Az app ettől úgy érzékeli mintha nem lenne hőmérő szenzor a telefonban.
        if(TempSensor.getName().contains("Goldfish")){
            TempText.setText("Homero szenzor nem elerheto!");
            TempFaren.setText("Homero szenzor nem elerheto!");
            SensorActive = false;

            if (apitemp <15){
                applayout.setBackgroundResource(R.drawable.backgroundcold);
            }
            else {
                applayout.setBackgroundResource(R.drawable.backgroundhot);
            }
        }
        

    }

    public void ApiHandler(){
        String latistr = String.valueOf(latitude);
        String longistr = String.valueOf(longitude);
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat="+latistr+"&lon="+longistr+"&exclude=minutely,hourly,daily,alerts&units=metric&appid=3b052b2606e4f71fcdd1b140bdd63254";

        StringRequest myRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try{

                        JSONObject myJsonObject = new JSONObject(response);
                        String apitempstr = (myJsonObject.getJSONObject("current")).getString("temp");
                        apitemp = (float) (Float.parseFloat(apitempstr));

                        String timezoneapi = myJsonObject.getString("timezone");

                        String weatherlikeout = (((myJsonObject.getJSONObject("current")).getJSONArray("weather")).getJSONObject(0)).getString("description");

                        System.out.println(response);
                        weatherdata.setText(String.format("%.2f °C", apitemp));
                        timezonedata.setText(timezoneapi);
                        weatherlike.setText(weatherlikeout);

                        if(!SensorActive){
                            tempBar.setProgress((int)apitemp);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> Toast.makeText(MainActivity.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show()
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(myRequest);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float currentTemp = sensorEvent.values[0];
        float tempinF = (currentTemp * 9/5) + 32;

        TempText.setText(currentTemp+" °C");
        TempFaren.setText(String.format("%.1f", tempinF) + " °F");

        int current_temp= (int)sensorEvent.values[0];

        tempBar.setProgress(current_temp);

        if (current_temp <15){
            applayout.setBackgroundResource(R.drawable.backgroundcold);
        }
        else {
            applayout.setBackgroundResource(R.drawable.backgroundhot);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //TODO
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SensorActive){
            SensorManager.registerListener(this,TempSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(SensorActive){
            SensorManager.unregisterListener(this);
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        double oldlati = latitude;
        double oldlongi = longitude;

        latitude= location.getLatitude();
        longitude = location.getLongitude();
        if (oldlati != latitude && oldlongi != longitude){
            ApiHandler();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }


}