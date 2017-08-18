package com.mhealthproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * Created by B on 2/12/2016.
 */
public class DataService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String SEND_TO_PHONE_LOG_PATH = "/send-to-phone-log-path";
    private static final String SEND_TO_PHONE_LOG_DATA = "send-to-phone-log-data";
    private static final String file_name = "/storage/emulated/legacy/mHealthData.txt";

    private boolean sendToServer = false;
    private boolean sentToServer = false;
    private boolean thisIsNew = true;
    private boolean serverIsSuccess = false;
    private DataInputStream[] dataInputStream;

    public static final String TAG = "DataService";
    private GoogleApiClient mGoogleApiClient;
    private final static boolean DBG = true;
    private long lastTimeDataCame = System.currentTimeMillis();
    private static final String LASTUPLOAD_SP_KEY = "LastUpload";

    final static public String ACTION_FILE_UPLOAD_RESULT = "com.mhealthproject.FILE_UPLOAD_RESULT";

    final static public String UPLOAD_URL = "http://valhalla.eecs.northwestern.edu/mHealthBegum.php";
    public static final String SHARED_PREFS_NAME = DataService.class.getPackage().getName();

    final private static Object mFileUploadLock = new Object();
    public static final long NO_LAST_UPLOAD = -1;

    public static final String INTENT_EXTRA_OUTCOME = "outcome";
    public static final String INTENT_EXTRA_TIMESTAMP = "timestamp";
    public static final String INTENT_EXTRA_NOTE = "note";
    public static final String INTENT_EXTRA_VERSION = "version";

    BluetoothAdapter mBTAdapter = null;

    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        if(mGoogleApiClient.isConnected()) Log.d(TAG, "mGoogleApiClient is connected");
        else Log.d(TAG, "mGoogleApiClient is not connected");

        dataInputStream = new DataInputStream[50];
        int i=0;
        while(i<50) {
            dataInputStream[i] = null;
            i++;
        }
        Log.d(TAG, "Wearable Listener Service created");
        showBluetoothDevices();

    }
    private void showBluetoothDevices(){
        try{
            Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    Log.d(TAG, "Paired " + device.getName() + "\n" + device.getAddress());
                }
            }
        }catch (Exception e) {
            Log.d(TAG, "Paired " + "null");
        }
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (null == mGoogleApiClient)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        if(mGoogleApiClient.isConnected()) Log.d(TAG, "mmGoogleApi is connected");
        return super.onStartCommand(intent, flags, startId);
    }
    public void onDataChanged(DataEventBuffer dataEvents) {

        lastTimeDataCame = System.currentTimeMillis();
        BufferedReader dataInputStreamLog = null;
        ByteArrayInputStream byteArrayInputStreamLog = null;
        Log.d(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if ((event.getType() == DataEvent.TYPE_CHANGED) && event.getDataItem().getUri().getPath().equalsIgnoreCase(SEND_TO_PHONE_LOG_PATH)) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                byteArrayInputStreamLog = new ByteArrayInputStream(dataMap.getByteArray(SEND_TO_PHONE_LOG_DATA));
                dataInputStreamLog = new BufferedReader(new InputStreamReader(byteArrayInputStreamLog));

                Log.d(TAG, "Data has been received " + dataMapItem.getDataMap().getByteArray(SEND_TO_PHONE_LOG_DATA).toString());
                sendToServer = true;

                if (dataMapItem.getDataMap().getByteArray(SEND_TO_PHONE_LOG_DATA) == null) {
                    Log.d(TAG, "Null log data has been received");

                }
            } else {
                Log.d(TAG, "unknown event type");
            }
        }

        if (sendToServer) {
            Log.d(TAG, "sendToServer is true");
            Log.d(TAG, "inside the run");
            if(thereIsNetwork()){
                sendToServer(dataInputStreamLog);
            }
            if(serverIsSuccess || sentToServer){
                NotifyWatch notifyWatch1 = new NotifyWatch(this);
                notifyWatch1.start();
            }
        }
    }
    private boolean thereIsNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo.DetailedState detailed_wifi = wifiNetInfo.getDetailedState();
        NetworkInfo.DetailedState detailed_mobile = mobileNetInfo.getDetailedState();
        if (!(detailed_wifi == NetworkInfo.DetailedState.CONNECTED ||
                detailed_mobile == NetworkInfo.DetailedState.CONNECTED)) {
            if (DBG) {
                Log.i(TAG, "  Network is not connected - can't upload file");
            }
            return false;
        }
        return true;
    }
    private boolean sendToServer(BufferedReader dataInputStreamLog){

        final BufferedReader finalDataInputStream =dataInputStreamLog;

        Intent intent;
        Boolean outcome = false;
        String note = "";
        String current_version = "";

        Log.d(TAG, "connectivity services checking");
        Log.i(TAG, " There is network");

        int counter = 0;
        writeFile(finalDataInputStream, counter);
        if(counter <=2){
            MainActivity.t_major = 0;
            MainActivity.t_minor = 0;
            MainActivity.t_size =0;
            MainActivity.t_time = 0;
            MainActivity.t_pressure = 0;
            MainActivity.t_x = 0;
            MainActivity.t_y = 0;
        }
        ML_model.is_stressed();

        new Task().execute(file_name, UPLOAD_URL);

        if (DBG) {
            Log.i(TAG, "Done with file upload.");
        }

        // Broadcast FILE_UPLOAD_RESULT intent of the outcome and time stamp if it worked
        Long time = java.lang.System.currentTimeMillis();
        if (outcome == true) {
            writeSuccessfulUploadFile(time);
        }
        intent = new Intent(ACTION_FILE_UPLOAD_RESULT);

        intent.putExtra(INTENT_EXTRA_OUTCOME, (Boolean) outcome);
        intent.putExtra(INTENT_EXTRA_TIMESTAMP, (Long) time);
        intent.putExtra(INTENT_EXTRA_NOTE, (String) note);
        intent.putExtra(INTENT_EXTRA_VERSION, (String) current_version);
        sendBroadcast(intent);

        return serverIsSuccess;


    }
    public static void writeFile(BufferedReader dis, int c){
        try{
            PrintWriter f = new PrintWriter(file_name);
            String line = dis.readLine();
            String[] elements;

            while (line != null){
                f.println(line);
                elements = line.split("\\s+");
                if(elements[0].equals("Accel_xyz:")){
                    MainActivity.accelerometer = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Heart_rate:")){
                    MainActivity.hr = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_major:")){
                    MainActivity.t_major = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_minor:")){
                    MainActivity.t_minor = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_time:")){
                    MainActivity.t_time = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_x:")){
                    MainActivity.t_x = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_y:")){
                    MainActivity.t_y = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_size:")){
                    MainActivity.t_size = Float.parseFloat(elements[1]);
                    c++;
                }
                else if(elements[0].equals("Touch_pressure:")){
                    MainActivity.t_pressure = Float.parseFloat(elements[1]);
                    c++;
                }
                line = dis.readLine();
            }
            dis.close();
            f.close();
        }catch(Exception e){
            Log.e(TAG, "IOException in writeFile");
        }
    }

    public void writeSuccessfulUploadFile(long time) {
        synchronized (mFileUploadLock) {
            SharedPreferences mSP = getSharedPreferences(
                    SHARED_PREFS_NAME,
                    Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor mEditor = mSP.edit();
            mEditor.putLong(LASTUPLOAD_SP_KEY, time);

            if (!mEditor.commit()) {
                Log.e(TAG, "Could not commit upload time!");
            }
        }
    }
    private void getConnection(){
        if(mGoogleApiClient!=null) return;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


    }
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }
    public void onPeerConnected(Node peer) {
        if (thisIsNew) {
            thisIsNew = false;
        }
        Log.d(TAG, "onPeerConnected: " + peer);
        getConnection();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        if(mGoogleApiClient.isConnected()) Log.d(TAG, "mGoogleApiClient is connected");
    }
    public void onPeerDisconnected(Node peer) {
        thisIsNew = true;
        Log.d(TAG, "onPeerDisconnected: " + peer);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }
}