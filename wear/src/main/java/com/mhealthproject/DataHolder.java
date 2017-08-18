package com.mhealthproject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by emir on 9/15/15.
 */
public class DataHolder {

    final private static String TAG = "DataHolder";
    private String[] app;
    private long[] user;
    private String[] appList;
    private int[] pid;
    private String touchEvents;
    private String[] powerData;

    private boolean safe;
    private int appInt;
    private int rank;


    private Service mContext;
    public BluetoothAdapter bluetoothAdapter;
    private GoogleApiClient googleApiClient;
    final private static Object mLogLock = new Object();





    public String getTouchEvents() {return touchEvents;}
    public void setTouchEvents(String data) {this.touchEvents = data;
        Log.i(TAG, "DataHolder touchEvents data: " + touchEvents);}


    public boolean getSendTouchEvents() {return safe;}
    public void setSendToucEvents(boolean safe) {this.safe = safe;
        Log.i(TAG, "DataHolder safe: " + safe);}

    // This function is used to clear the rank after it is written to the file
    // if you need to use the getStressRank() some where other than writting to the file
    // please modify this function, otherwise you may not get what you want!
    public int getStressRank() {
        if (rank != 0) {
            int temp = rank;
            rank = 0;
            return temp;
        }
        return rank;
    }

    public void setStressRank(int rank) {
        this.rank = rank;
        Log.i(TAG, "DataHolder stress rank: " + rank);
    }

    public int getRunningAppInt() {return appInt;}
    public void setRunningAppInt(int appInt) {this.appInt = appInt;
        Log.i(TAG, "Running app is: " + appInt);}


    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}

