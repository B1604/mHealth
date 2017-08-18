package com.mhealthproject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by begum and emir on 1/29/16.
 * This class is for collecting the sensors and saving them as logs.
 * This class collects 3 sensors step counter, accelerometer, heart rate
 * This class is a service class and implements SensorEventListener which provides us to listen sensors from SYSTEM_SERVICE
 */

public class SensorService extends Service implements SensorEventListener {

    final public static String TAG = "SensorService";
    final private long DURATION = 3600000;
    // This is sample rate 16Hz corresponding to 62.5 milliseconds
    // This is time interval represents the duration we write our data to the mHealth.txt
    final private long TIMEINTERVAL = 62;
    // This is the time interval represents the duration of sensors to update their value
    // we need to make sure the wirtting rate is no larger than the updating rate,
    // otherwise, we will have duplicate signal values
    final private int SENSOR_DELAY = 62500;

    private static Logger mLogger = new Logger();
    // sensor manager and sensor event listener objects
    private SensorManager mSensorManager;
    // wake lock to make this app runs even in screen off
    private PowerManager powerManager;
    public PowerManager.WakeLock wakeLock;

    private String accellerometerSensor = "";
    private String heartRateSensor = "";
    private String stepCounterSensor = "";
    private String gyroscopeSensor = "";
    private int accellerometerEvent = 0;
    private int heartRateEvent = 0;
    private int stepCounterEvent = 0;
    private int gyroscopeEvent = 0;

    private boolean justSent = false;


    Thread keyThread = new Thread();

    CatchColorView catchColorView;
    MovingBallView movingBallView;
    TouchMultipleView touchMultipleView;

    public Handler mHandler2= new Handler();

    private long lastTime = System.currentTimeMillis();

    private BroadcastReceiver mReceiver;
    public int oldLevel = -1;
    public boolean oldACCharging = false;
    public boolean oldUSBCharging = false;
    public boolean oldState = false;
    private final static String BATTERY_LEVEL = "level";


    //InputStreamReader reader;


    @Override
    public void onCreate() {

        Log.i(TAG, "SensorService");

        Toast.makeText(this, "Service is created...", Toast.LENGTH_LONG).show();
        String head = "Date,Time,Charging,Battery Level,AccelX,AccelY,AccelZ,AccelEvent,Step Counter,SCEvent,HR,HREvent,GyroX,GyroY,GyroZ,GyroEvent,Stress Rank";
        writeToFile_CA(head);

        try {


            Logger.createLogFile(this);
            Logger.createLogFileToUpload(this);
            mLogger.logEntry("Logger On");
            mLogger.logEntryPhone("Logger On");

            catchColorView = new CatchColorView(this);
            movingBallView = new MovingBallView(this);
            touchMultipleView = new TouchMultipleView(this);

            // record battery condition
            mReceiver = new BatteryBroadcastReceiver();
            registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyWakelockTag");
            wakeLock.acquire();

            IntentFilter filter = new IntentFilter();               // to listen system intents, if we need anything.
            registerReceiver(mBroadcastIntentReceiver, filter);

            startSensorListeners();
            mHandler.postDelayed(mRefresh, 500);

            mHandler2.postDelayed(notificationRunnable,DURATION);

        } catch (Exception e) {
            Toast.makeText(this, "An error occured in SensorService onCreate", Toast.LENGTH_LONG).show();
        }

    }


    private void startSensorListeners() {
        Log.d(TAG, "startSensorListeners");


        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SENSOR_DELAY);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SENSOR_DELAY);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SENSOR_DELAY);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SENSOR_DELAY);

        Log.d(TAG, "Sensor started: " + mSensorManager);

    }

    public String getState() {
        if (oldState) {
            if (oldUSBCharging) return new String("USB Charging");
            if (oldACCharging)  return new String("AC Charging");
        }
        return new String("notCharging");

    }

    public String getLevel() {
        String currentLevel = String.valueOf(oldLevel);
        return currentLevel;
    }


    private void stopSensorListeners() {
        Log.d(TAG, "stopSensorListeners");
        mSensorManager.unregisterListener(SensorService.this);
        Log.d(TAG, "Sensor stoppped: " + mSensorManager);
        wakeLock.release();

    }


    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL);
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            if (isCharging != oldState) {
                oldState = isCharging;
                if (usbCharge != oldUSBCharging) {
                    oldUSBCharging = usbCharge;
                }
                if (acCharge != oldACCharging) {
                    oldACCharging = acCharge;
                }
            }

            int level = intent.getIntExtra(BATTERY_LEVEL, 0);
            if (oldLevel != level) {
                oldLevel = level;
            }
        }
    }


    @Override
    // in each sensor changes this override function keeps the everything avout the sensor. And we get 2 of these info
    public void onSensorChanged(SensorEvent event) {
        String key = event.sensor.getName();
        float values = event.values[0];
        Sensor sensor = event.sensor;


        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // this is event counts when sensor changes,
            // this counts all the events changings.
            // It shouldnt be necessary though
            accellerometerEvent++;
            // This is the string for value and seen event count.
            accellerometerSensor = values + "," + event.values[1] + "," + event.values[2] + "," + accellerometerEvent + ",";

        }

        if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
            heartRateEvent++;
            heartRateSensor = values + "," + heartRateEvent + ",";

        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCounterEvent++;
            stepCounterSensor = values + "," + stepCounterEvent + ",";

        }

        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeEvent++;
            try {
                gyroscopeSensor = values + "," + event.values[1] + "," + event.values[2] + "," + gyroscopeEvent;
            }catch (Exception e){}

        }

    }

    @Override
    // since we will always capture the data whether it changes or not, we don't need this method
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        return;
    }

    @Nullable
    @Override     // its default method created by service class
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "service onDestroy", Toast.LENGTH_LONG).show();
        unregisterReceiver(mReceiver);
        keyThread.interrupt();
        mHandler.removeCallbacksAndMessages(null);
        mHandler2.removeCallbacksAndMessages(null);
        stopSelf();
    }


    private OutputStream getTouchEvents2() {

        try {
            Process mProcess = new ProcessBuilder()
                    .command("su")
                    .redirectErrorStream(true).start();

            OutputStream out = mProcess.getOutputStream();

            String cmd = "getevent /dev/input/event1 \n";
            Log.d(TAG, "Native command = " + cmd);
            out.write(cmd.getBytes());
            Log.d(TAG, out.toString());
            return out;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    BroadcastReceiver mBroadcastIntentReceiver = new BroadcastReceiver() {     // just captures the screen on and off. An example if we need to track actions
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG, ">>>>>>>>>> caught ACTION_SCREEN_OFF <<<<<<<<<<<");
                mLogger.logEntry("Screen Off");

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(TAG, ">>>>>>>>>> caught ACTION_SCREEN_ON <<<<<<<<<<<");
                mLogger.logEntry("Screen_On");

            }
        }
    };


    /* So far i have tried to write getevent -lt to /data/data/files/mhealth.. to be able to get all touch events and get them in syncronize way
    * But first of all it is so costly getting them from inputsteamreader and writing to files make the watch freeze and consume the vm memory
    * Other thing i have tried, i wrote them in sdcard and get them from there, but it is again the same as above
    * One thing we can do, we can write them to /sdcard and send this /sdcard file to server at the end and make computation offline
    * as shown below
    */
    class KeyLogger implements Runnable {
        @Override
        public void run() {
            try {
                final Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "getevent -lt /dev/input/event0 > /sdcard/geteventFile"});
                // final Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "getevent -lt /dev/input/event0" });
                final InputStreamReader reader = new InputStreamReader(process.getInputStream());
                Log.d("", "it is gonna get getevent1");
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                //  final Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "getevent -lt /dev/input/event0 > /sdcard/doubletab" });
                                //final Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "getevent -lt /dev/input/event0 > /sdcard/doubletab" });
                                //final InputStreamReader reader = new InputStreamReader(process.getInputStream());
                                // final Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "getevent -lt /dev/input/event0" });
                                // final InputStreamReader reader = new InputStreamReader(process.getInputStream());
                                while (reader.ready()) {

                                    //  Log.d("", "xyx Touch " + reader.read());
                                    Log.d("", "xyx Touch " + reader.read());
                                }
                                Thread.sleep(4000);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                run.run();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("", "There is a problem in avg logger");
            }
        }
    }

    private void writeToFile(String data) {

        Date date = new Date();
        File logFile = new File("sdcard/mHealthLogs.txt");
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

    // handler is more convenient for massage passing between objects and also UI friendly.
    Handler mHandler = new Handler();
    // so if we need to put some info or even in notifications we may need handler instead of thread.
    // Here we write the data into the mHealth.txt
    Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            try {
                String touch_events = "";
                try {
                    String result = "";
                    // record time
                    result = result + DateFormat.getDateTimeInstance().format(new Date()) + "," + System.currentTimeMillis() + ",";
                    // record battery
                    result = result + getState() + ",";
                    result = result + getLevel() + ",";
                    // record sensor data
                    result = result + accellerometerSensor + stepCounterSensor + heartRateSensor + gyroscopeSensor;
                    // get rank
                    result = result + "," + DataHolder.getInstance().getStressRank();
                    // write to file
                    writeToFile_CA(result);

                } catch (Exception e) {
                    Log.i(TAG, "Error occured while collecting sensors " + e);
                }

                // deletes 1 sec after sending and creates new log file
                if (justSent) {
                    justSent = false;
                }

                //Send to phone in every 10 sec
                if (System.currentTimeMillis() - lastTime >= 1000* 60*60) {
                    Log.d("Logger", "Get ave is called");
                    lastTime = System.currentTimeMillis();
                    justSent = true;
                }


                // stopSensorListeners();
                mHandler.postDelayed(mRefresh, TIMEINTERVAL);


            } catch (Exception e) {
                Log.i(TAG, "Error occured " + e);

            }
        }
    };

    private Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            notifyUserToReportStress();
            mHandler2.postDelayed(notificationRunnable, DURATION);
        }
    };

    private void notifyUserToReportStress() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.are_you_stressed);

        // Set the icon, scrolling text and timestamp
        Log.d(TAG, "It is in the notification");


        int notificationId = 001;
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.red)
                        .setContentTitle(getText(R.string.stress_test))
                        .setContentText(text)
                        .setContentIntent(viewPendingIntent)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000});


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notificationId, notificationBuilder.build());


    }
}

