package com.mhealthproject;

import android.content.Intent;
import android.os.FileObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    final public static String TAG = "mhealth Project";
    private static final String file_name = "/storage/emulated/legacy/mHealthData.txt";
    private File log;

    public static ImageView imv;

    public static float accelerometer=0;
    public static float hr=0;
    public static float t_major=0;
    public static float t_minor=0;
    public static float t_time=0;
    public static float t_x=0;
    public static float t_y=0;
    public static float t_size=0;
    public static float t_pressure=0;
    public static MyBoolean stress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        imv = (ImageView) findViewById(R.id.imageView);

        stress = new MyBoolean();

        try {
            log = new File(file_name);
            if (!log.exists()) {
                log.createNewFile();
            }
        } catch (Exception e) {
            Log.e(TAG, "File problem in MainActivity");
        }

        startService(new Intent(this, DataService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void changeImage(){
        if(stress.getter()){ //Stressed
            imv.setImageResource(R.drawable.dead);
        }
        else{ //Not Stressed
            imv.setImageResource(R.drawable.happy_alternative2);
        }
    }
}

