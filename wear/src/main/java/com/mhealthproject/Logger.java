package com.mhealthproject;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by begum and emir on 1/29/16.
 */
public class Logger  {

    final static public String TAG = "Logger";
    private static final String LOG_FILE_NAME = "mHealthProject";
    private static final String LOG_FILE_NAME_PHONE = "mHealthProjectPHONE";
    final static public String UPLOAD_FILE_NAME = "Upload.log";
    private static FileOutputStream mOutputStream = null;
    private static FileOutputStream mOutputStreamPhone = null;

    final private static Object mLogLock = new Object();

    private static final byte[] SPACE  = " ".getBytes();
    private static final byte[] NEWLINE= "\n".getBytes();



// this creates the log file with the name mHealthProject inside ../application name/files in the watch

    public static void createLogFile(Context c) {
        synchronized (mLogLock) {
            try {
                mOutputStream = c.openFileOutput(LOG_FILE_NAME, Context.MODE_APPEND);
            } catch (Exception e) {
                Log.e(TAG, "Can't open file " + LOG_FILE_NAME + ":" + e);

            }
        }
    }

    public static void createLogFileToUpload(Context c) {
        synchronized (mLogLock) {
            try {
                mOutputStreamPhone = c.openFileOutput(LOG_FILE_NAME_PHONE, Context.MODE_APPEND);
            } catch (Exception e) {
                Log.e(TAG, "Can't open file " + LOG_FILE_NAME_PHONE + ":" + e);

            }
        }
    }




    public void logEntry(String s){
        try {
            mOutputStream.write(s.getBytes());
            mOutputStream.write(NEWLINE);
        } catch (IOException ioe) {
            Log.e(TAG, "ERROR: Can't write string to file: " + ioe);
        }
    }

    public void logEntryPhone(String s){
        try {
            mOutputStreamPhone.write(s.getBytes());
            mOutputStreamPhone.write(NEWLINE);
        } catch (IOException ioe) {
            Log.e(TAG, "ERROR: Can't write string to file: " + ioe);
        }
    }



    public void getAverageData(Context c){
        synchronized(mLogLock) {
            Log.i(TAG, "Preparing for upload: append logfile to upload file");

            float accelerometer_x = 0;
            float accelerometer_y= 0;
            float accelerometer_z = 0;
            int counter_accel = 0;

            float heart_rate =0;
            int counter_heart=0;

            float touch_major =0;
            float touch_minor =0;
            float touch_time =0;
            float touch_x =0;
            float touch_y =0;
            float touch_size = 0;
            float touch_pressure =0;
            int counter_touch=0;

            Date date = new Date();

            // append the log file to the upload file
            try {
                FileInputStream in_stream = c.openFileInput(LOG_FILE_NAME);
                DataInputStream in = new DataInputStream(in_stream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                while((line = br.readLine()) != null){

                    if(line.contains("Accelerometer")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");
                        //  Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        //  Matcher m = p.matcher(line);
                        //  if (m.find()) {
                        //     accelerometer_x = accelerometer_x + Float.valueOf(m.group(1));
                        //      Log.d("Logger ", "accelerometer_x: " + accelerometer_x );
                        //  }
                        //  Log.d("Logger", "x,y,x "+ lineArray[1]+ " "+ lineArray[3]);
                        Log.d("Logger ", "x y z: " + Float.valueOf(lineArray[1]) + " " + Float.valueOf(lineArray[2])+ " " + Float.valueOf(lineArray[3]));
                        accelerometer_x = accelerometer_x  + Float.valueOf(lineArray[1]);
                        accelerometer_y = accelerometer_y  + Float.valueOf(lineArray[2]);
                        accelerometer_z = accelerometer_z  + Float.valueOf(lineArray[3]);
                        counter_accel++;
                        Log.d("Logger ", "x y z: " + accelerometer_x + " " + accelerometer_z);
                    }

                    else if(line.contains("Heart")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");
                        heart_rate = heart_rate  + Float.valueOf(lineArray[2]);
                        counter_heart++;
                        Log.d("Logger ", "heart rate: " + heart_rate );
                    }

                    else if(line.contains("Touch_major")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_major = touch_major + Float.valueOf(m.group(1));
                            Log.d("Logger ", "touch_major: " + touch_major );
                        }

                    }

                    else if(line.contains("Touch_minor")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_minor = touch_minor + Float.valueOf(m.group(1));
                        }


                    }

                    else if(line.contains("Touch_time")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_time = touch_time + Float.valueOf(m.group(1));
                        }


                    }

                    else if(line.contains("Touch_x")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_x = touch_x + Float.valueOf(m.group(1));
                        }


                    }

                    else if(line.contains("Touch_y")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_y = touch_y + Float.valueOf(m.group(1));
                        }


                    }

                    else if(line.contains("Touch_size")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_size = touch_size + Float.valueOf(m.group(1));
                        }


                    }

                    else if(line.contains("Touch_pressure")){
                        // line = textReader.readLine();
                        String[] lineArray = line.split("\\s+");

                        // if the next is a float, print found and the float

                        Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            touch_pressure = touch_pressure + Float.valueOf(m.group(1));
                            counter_touch++;
                            Log.d("Logger", "counter touch: " + counter_touch);
                        }


                    }


                }

                String logEntryToPhone = "Accel_xyz: "+ accelerometer_x/counter_accel+ " "+ accelerometer_y/counter_accel+ " "+ accelerometer_z/counter_accel + "\n"+
                        "Heart_rate: "+ heart_rate/counter_heart;


                String touches;
                if(counter_touch>0){
                    touches = "Touch_major: " + touch_major/counter_touch + "\n" +
                            "Touch_minor: " + touch_minor/counter_touch + "\n" +
                            "Touch_time: " + touch_time/counter_touch + "\n" +
                            "Touch_x: " + touch_x/counter_touch + "\n" +
                            "Touch_y: " + touch_y/counter_touch + "\n" +
                            "Touch_size: " + touch_size/counter_touch + "\n" +
                            "Touch_pressure: " + touch_pressure/counter_touch ;

                }else touches ="";

                logEntryPhone("Date: " + String.valueOf(date));
                logEntryPhone(logEntryToPhone);
                logEntryPhone(touches);


            } catch (Exception e) {
                Log.e(TAG, "Can't open input file stream: " + e);
            }


        }
    }




    public static void prepareForUpload(Context c) {
        synchronized(mLogLock) {
            Log.i(TAG, "Preparing for upload: append logfile to upload file");

            // append the log file to the upload file
            try {
                FileInputStream in_stream = c.openFileInput(LOG_FILE_NAME_PHONE);
                DataInputStream in = new DataInputStream(in_stream);

/*
                float accelerometer_x = 0;
                float accelerometer_y= 0;
                float accelerometer_z = 0;
                int counter_accel = 0;

                float heart_rate =0;
                int counter_heart=0;

                float touch_major =0;
                float touch_minor =0;
                float touch_time =0;
                float touch_x =0;
                float touch_y =0;
                float touch_size = 0;
                float touch_pressure =0;
                int counter_touch=0;

                Date date = new Date();

                String logToPhone="";
                // append the log file to the upload file
                try {

                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while((line = br.readLine()) != null){

                        if(line.contains("Accelerometer")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");
                            //  Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            //  Matcher m = p.matcher(line);
                            //  if (m.find()) {
                            //     accelerometer_x = accelerometer_x + Float.valueOf(m.group(1));
                            //      Log.d("Logger ", "accelerometer_x: " + accelerometer_x );
                            //  }
                            //  Log.d("Logger", "x,y,x "+ lineArray[1]+ " "+ lineArray[3]);
                            Log.d("Logger ", "x y z: " + Float.valueOf(lineArray[1]) + " " + Float.valueOf(lineArray[2])+ " " + Float.valueOf(lineArray[3]));
                            accelerometer_x = accelerometer_x  + Float.valueOf(lineArray[1]);
                            accelerometer_y = accelerometer_y  + Float.valueOf(lineArray[2]);
                            accelerometer_z = accelerometer_z  + Float.valueOf(lineArray[3]);
                            counter_accel++;
                            Log.d("Logger ", "x y z: " + accelerometer_x + " " + accelerometer_z);
                        }

                        else if(line.contains("Heart")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");
                            heart_rate = heart_rate  + Float.valueOf(lineArray[1]);
                            counter_heart++;
                            Log.d("Logger ", "heart rate: " + heart_rate );
                        }

                        else if(line.contains("Touch_major")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_major = touch_major + Float.valueOf(m.group(1));
                                Log.d("Logger ", "touch_major: " + touch_major );
                            }

                        }

                        else if(line.contains("Touch_minor")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_minor = touch_minor + Float.valueOf(m.group(1));
                            }


                        }

                        else if(line.contains("Touch_time")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_time = touch_time + Float.valueOf(m.group(1));
                            }


                        }

                        else if(line.contains("Touch_x")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_x = touch_x + Float.valueOf(m.group(1));
                            }


                        }

                        else if(line.contains("Touch_y")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_y = touch_y + Float.valueOf(m.group(1));
                            }


                        }

                        else if(line.contains("Touch_size")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_size = touch_size + Float.valueOf(m.group(1));
                            }


                        }

                        else if(line.contains("Touch_pressure")){
                            // line = textReader.readLine();
                            String[] lineArray = line.split("\\s+");

                            // if the next is a float, print found and the float

                            Pattern p = Pattern.compile(".+: (\\d+\\.\\d+)");
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                touch_pressure = touch_pressure + Float.valueOf(m.group(1));
                                counter_touch++;
                                Log.d("Logger", "counter touch: " + counter_touch);
                            }


                        }


                    }

                    String logEntryToPhone = "Accel_xyz: "+ accelerometer_x/counter_accel+ " "+ accelerometer_y/counter_accel+ " "+ accelerometer_z/counter_accel + "\n"+
                            "Heart_rate: "+ heart_rate/counter_heart;

                    String touches;
                    if(counter_touch>0){
                        touches = "Touch_major: " + touch_major/counter_touch + "\n" +
                                "Touch_minor: " + touch_minor/counter_touch + "\n" +
                                "Touch_time: " + touch_time/counter_touch + "\n" +
                                "Touch_x: " + touch_x/counter_touch + "\n" +
                                "Touch_y: " + touch_y/counter_touch + "\n" +
                                "Touch_size: " + touch_size/counter_touch + "\n" +
                                "Touch_pressure: " + touch_pressure/counter_touch ;

                    }else touches ="";

                    // logEntryPhone("Date: " + String.valueOf(date));
                    //  logEntryPhone(logEntryToPhone);
                    // logEntryPhone(touches);

                    logToPhone = "Date: " + String.valueOf(date) + "\n" +
                            logEntryToPhone + "\n"+
                            touches;

                } catch (Exception e) {
                    Log.e(TAG, "Can't open input file stream: " + e);
                }


*/

                FileOutputStream out_stream = c.openFileOutput(UPLOAD_FILE_NAME, Context.MODE_APPEND);
                DataOutputStream out = new DataOutputStream(out_stream);

                byte[] buffer = new byte[2048];
                int bytes_read;

                while (-1 != (bytes_read = in.read(buffer, 0, 2048))) {
                    out.write(buffer, 0, bytes_read);
                }
            } catch (Exception e) {
                Log.e(TAG, "Can't open input file stream: " + e);
            }


        }
    }

    public static void deleteUploadFile(Context c) {
        synchronized(mLogLock) {
            c.deleteFile(UPLOAD_FILE_NAME);
            c.deleteFile(LOG_FILE_NAME);
            c.deleteFile(LOG_FILE_NAME_PHONE);
        }
    }



}
