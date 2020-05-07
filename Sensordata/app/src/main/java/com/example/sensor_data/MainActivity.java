package com.example.sensor_data;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{


    // device sensor manager
    private SensorManager mSensorManager;

    TextView TvSensor;

    private int sensor = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TextView that will tell the user what degree is he heading
        TvSensor = (TextView) findViewById(R.id.TvSensor);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Button button = findViewById(R.id.ChangeSensorBtn);
        setOnClick(button, this);

    }
    private void setOnClick(final Button btn, final MainActivity view){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sensor == 1)
                {
                    sensor = 0;
                    mSensorManager.unregisterListener(view);
                    mSensorManager.registerListener(view, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                            SensorManager.SENSOR_DELAY_GAME);
                }
                else
                {
                    sensor = 1;
                    mSensorManager.unregisterListener(view);
                    mSensorManager.registerListener(view, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_GAME);
                }
            }
        });
    }
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        if (sensor == 1)
        {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
            sensor = 0;
        }
        else
        {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
            sensor = 1;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);


        TvSensor.setText(event.sensor.getName()+": " + Float.toString(degree) + " degrees");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
