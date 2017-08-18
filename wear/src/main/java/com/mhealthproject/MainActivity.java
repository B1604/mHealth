package com.mhealthproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

//import android.support.wearable.activity.Wea;

public class MainActivity extends Activity {


    private Button startService;
    private Button stopService;
    private Button rankStress;
    private Button catchCollor;
    private Button touchMultiple;

    private Button stressYes;
    private Button stressNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // start the smartwatch app and load the layout activity_main.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check the permission of using storage
        // we write out data to the file named mHealth.txt in the smartwatch sdcard
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

        }

        // check the permission of using sensors
        // we use heart rate, accelerometer, gyroscope, step counter
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BODY_SENSORS},
                    1);
        }

        // Button functions
        // start button control the start of the application
        startService = (Button) this.findViewById(R.id.startServiceId);
        if (startService != null) {
            startService.setOnClickListener(startServiceListener);
        }
        // stop button control the stop of the application
        stopService = (Button) this.findViewById(R.id.stopServiceId);
        if (stopService != null) {
            stopService.setOnClickListener(stopServiceListener);
        }
        // yes button is clicked when the user feels stress
        // it navigates to RankStressActivity, and let user rank their stress level
        stressYes = (Button) this.findViewById(R.id.stressYes);
        if (stressYes != null) {
            stressYes.setOnClickListener(stressYesListener);
        }
        // no button is clicked when the user does not feel stress
        // it records stress level -1 into the file (mHealth.txt)
        stressNo = (Button) this.findViewById(R.id.stressNo);
        if (stressNo != null) {
            stressNo.setOnClickListener(stressNoListener);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Grant has been given",Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    private View.OnClickListener startServiceListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this,SensorService.class);
            MainActivity.this.startService(intent);
        }
    };

    private View.OnClickListener stopServiceListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this,SensorService.class);
            MainActivity.this.stopService(intent);
        }
    };

    private View.OnClickListener stressYesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, RankStressActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener stressNoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DataHolder.getInstance().setStressRank(-1);
            Toast.makeText(MainActivity.this,"No stress",Toast.LENGTH_SHORT).show();
        }
    };

    // if you want to write some thing to the mHealth.txt file, you can use the following method
    // here we did not call this function in this class, because all the data recording are done in the SensorService class
    // if you call the wirteToFile_CA() function here, it only execute when you call it, which means it will not write to the
    // file with the same sample rate as the other signals do.
    private void writeToFile_CA(String data) {

        Date date = new Date();
        File logFile = new File("sdcard/mHealth2.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(data);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
