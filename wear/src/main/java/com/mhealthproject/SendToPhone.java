package com.mhealthproject;

/**
 * Created by emir on 9/15/15.
 */
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SendToPhone extends Thread {


    final public static String TAG = "SwatchLogUpload";

    final private static Object mFileUploadLock = new Object();

    public static final long NO_LAST_UPLOAD = -1;
    final static public String ACTION_FILE_UPLOAD_RESULT = "edu.northwestern.Watchlogger.FILE_UPLOAD_RESULT";
    public static final String SHARED_PREFS_NAME = SensorService.class.getPackage().getName();
    private static final String LOG_FILE_NAME = "mHealthProject";
    final static public String UPLOAD_FILE_NAME = "Upload.log";

    public static final String SEND_TO_PHONE_LOG_PATH = "/send-to-phone-log-path";
    public static final String SEND_TO_PHONE_LOG_DATA = "send-to-phone-log-data";

    private Object obj=null;
    private byte[] byteArray=null;
    final private static Object mLogLock = new Object();
    private String deviceName;


    //    public GoogleApiClient mGoogleApiClient;
    //  Serialization serialization = new Serialization();



	/*
	 * intent.putExtra("outcome", (Boolean) outcome);
        intent.putExtra("timestamp", (Long) time);
        intent.putExtra("note", (String) note);
        intent.putExtra("version", (String) current_version);
	 */

    private Service mContext;
    public BluetoothAdapter bluetoothAdapter;
    private GoogleApiClient googleApiClient;
    private boolean connected;



    public SendToPhone(Service c) {
        mContext = c;

        // Set a default uncaught exception handler to send outcome in case something goes wrong - SocketException and more?
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Intent intent = new Intent(ACTION_FILE_UPLOAD_RESULT);
                intent.putExtra("data", (Boolean) false);
                intent.putExtra("timestamp", (Long) java.lang.System.currentTimeMillis());
                intent.putExtra("note", new String("Failed: Uncaught Exception!"));
                mContext.sendBroadcast(intent);
                getGoogleClient();
            }
        });
    }

    private static final String USERID_SP_KEY = "UniqueUserId";
    private static final String LASTUPLOAD_SP_KEY = "LastUpload";

    public static String getUserId(Context ctext) {
        // first try shared preferences
        SharedPreferences mSP = ctext.getSharedPreferences(
                SHARED_PREFS_NAME,
                Context.MODE_WORLD_READABLE);
        String id = mSP.getString(USERID_SP_KEY, null);

        if (id == null) {
            // if this hasn't been entered yet,
            // just generate a unique ID
            UUID mUuid;// = UUID.randomUUID();


            // build a device-unique string
            String[] sources = new String[3];
            sources[0] = Settings.Secure.getString(
                    ctext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            TelephonyManager manager = (TelephonyManager) ctext.getSystemService(Context.TELEPHONY_SERVICE);

            sources[1] = manager.getDeviceId();

            sources[2] = manager.getLine1Number();

            String uuidSource = "";

            String prefix;

            for (int i = 0; i < sources.length; i++) {
                if (sources[i] != null) {
                    uuidSource = uuidSource + sources[i];
                }
            }

            Log.i(TAG, "Server uuidSource: " + uuidSource);

            if (uuidSource.length() < 1) {
                // balls... no valid id source
                mUuid = UUID.randomUUID();
                prefix = "RID"; // Randomly Generated ID

            } else {
                mUuid = UUID.nameUUIDFromBytes(uuidSource.getBytes());
                prefix = "UID";

            }

            id = prefix + Long.toHexString(mUuid.getMostSignificantBits()) + Long.toHexString(mUuid.getLeastSignificantBits());

            SharedPreferences.Editor mSPEditor = mSP.edit();
            mSPEditor.putString(USERID_SP_KEY, id);
            mSPEditor.commit();
        }

        return id;
    }

    private void getGoogleClient() {
        if (null != googleApiClient)
            return;

        Log.d(TAG, "getGoogleClient");
        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }



    private boolean bluetoothConnection() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.i(TAG, "Bluetooth is not supported right now..");
            return false;
        }
        else {
            if (bluetoothAdapter.isEnabled()) {
                Log.i(TAG, "Bluetooth Connected, Trying to send the file to phone..");
                return true;

            } else {
                Log.i(TAG, "Bluetooth is not Connected, will try later to send the file to phone..");
                return false;
            }
        }

    }


    private boolean wifiCommunication(){

        try {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo.DetailedState detailed_wifi = wifiNetInfo.getDetailedState();
            NetworkInfo.DetailedState detailed_mobile = mobileNetInfo.getDetailedState();

            Log.i(TAG, "Trying File Upload now with wifi to server..");

            long last_upload = getLastSuccessfulUploadTime(mContext);
            long current_time = java.lang.System.currentTimeMillis();


            // Don't upload if not connected to the internet
            if (!(detailed_wifi == NetworkInfo.DetailedState.CONNECTED ||
                    detailed_mobile == NetworkInfo.DetailedState.CONNECTED)) {
                Log.i(TAG, "  Network is not connected - can't upload file");

                return false;

            }
            else return true;

        } catch (Exception e) {
            return false;
        }

    }



    // TODO: Need to prevent timeout on the server side
    @Override
    public void run() {
        Intent intent;
        Boolean outcome = false;
        String note = "";
        String current_version = "";
        //boolean connected = true;

        if(wifiCommunication() || bluetoothConnection()){

            getGoogleClient();
            // checkIfPhoneConnected();


            List<com.google.android.gms.wearable.Node> connectedNodes =
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
            if(!connectedNodes.isEmpty()){
                Log.i(TAG, "Phone is connected: " + connectedNodes + " size is: " + connectedNodes.size());

                Logger.prepareForUpload(mContext);


                try {
                    googleApiClient = new GoogleApiClient.Builder(mContext)
                            .addApi(Wearable.API)
                            .addConnectionCallbacks(new ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    //we need to serialize our object
                                    synchronized (mLogLock) {
                                        try {

                                            ByteArrayOutputStream b = getByteArray(mContext);
                                            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SEND_TO_PHONE_LOG_PATH);
                                            if (b == null) {
                                                Log.i(TAG, "dos is null...");

                                            } else {
                                                Log.i(TAG, "dos is not null... ");

                                            }

                                            putDataMapRequest.getDataMap().putByteArray(SEND_TO_PHONE_LOG_DATA, b.toByteArray());

                                            PutDataRequest request = putDataMapRequest.asPutDataRequest();
                                            //put the data in the data layer
                                            Wearable.DataApi.putDataItem(googleApiClient, request);
                                            Log.i(TAG, "Data put  works...");
                                            //TODO : make sure it didnt reach to phone or phone couldnt recognize it!!!
                                            // tryMessageApi();
                                            //  deleteSentFiles();

                                        } catch (Exception e) {
                                            Log.e(TAG, "Can't open input file stream: " + e);
                                        }
                                        // }
                                    }
                                }

                                @Override
                                public void onConnectionSuspended(int i) {

                                }
                            })
                            .build();
                    googleApiClient.connect();

                } catch (Exception e) {

                }
            }}}


    private void checkIfPhoneConnected(){
        boolean found = false;
        if(googleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                    for(com.google.android.gms.wearable.Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), SEND_TO_PHONE_LOG_PATH, SEND_TO_PHONE_LOG_DATA.getBytes()).await();
                        if(!result.getStatus().isSuccess()){
                            Log.e("test", "error");
                            phoneIsConnected(false);
                        } else {
                            Log.i("test", "success!! sent to: " + node.getDisplayName());
                            phoneIsConnected(true);
                        }
                    }
                }
            }).start();

        } else {
            Log.e("test", "not connected");
        }

    }

    private boolean phoneIsConnected(boolean statu){
        connected = statu;
        return connected;
    }

    private void tryMessageApi(){
        if(googleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                    for(com.google.android.gms.wearable.Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), SEND_TO_PHONE_LOG_PATH, SEND_TO_PHONE_LOG_DATA.getBytes()).await();
                        if(!result.getStatus().isSuccess()){
                            Log.e("test", "error");
                        } else {
                            Log.i("test", "success!! sent to: " + node.getDisplayName());
                        }
                    }
                }
            }).start();

        } else {
            Log.e("test", "not connected");
        }

    }






    public static ByteArrayOutputStream getByteArray(Context mContext) {

        try {
            FileInputStream in_stream = mContext.openFileInput(UPLOAD_FILE_NAME);
            DataInputStream in = new DataInputStream(in_stream);
            //  FileOutputStream out_stream = new FileOutputStream("FileToSend.ser");
            ByteArrayOutputStream b = new ByteArrayOutputStream(2048);
            DataOutputStream out = new DataOutputStream(b);

            byte[] buffer = new byte[2048];
            int bytes_read;


            while (-1 != (bytes_read = in.read(buffer, 0, 2048))) {
                out.write(buffer, 0, bytes_read);
            }

            return b;

        } catch (IOException ex) {

        }

        return null;
    }



    public static void sendToPhone(FileInputStream serialization) throws IOException{


        //  int length = Array.getLength(serialization);
        //   for (int i = 0; i < length; i++) {
        //       obj = Array.get(serialization, i);
        //   }

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SEND_TO_PHONE_LOG_PATH);

        Log.i(TAG, "Creating path works...");

        Log.i(TAG, "Creating array works...");



        // FileOutputStream fileOutputStream = new FileOutputStream("Log_file.txt");

        ObjectInputStream ois = new ObjectInputStream(serialization);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        //    DataOutputStream w = new DataOutputStream(b);

        ObjectOutputStream o = new ObjectOutputStream(b);
        //   serialization.getFile(objectOutputStream);

        Log.i(TAG, "byte writing works...");

        // final static obj = serialization;

        ois.close();
        o.writeObject(ois);

        Log.i(TAG, "serialization works...");


        //  objectOutputStream.close();
        //   serializedFile.close();

        putDataMapRequest.getDataMap().putByteArray(SEND_TO_PHONE_LOG_PATH, b.toByteArray());
        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        Log.i(TAG, "put data request works...");


    }






    public static void writeSuccessfulUploadFile(Context c, long time) {
        synchronized (mFileUploadLock) {

            // SharedPrefs
            SharedPreferences mSP = c.getSharedPreferences(
                    SHARED_PREFS_NAME,
                    Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor mEditor = mSP.edit();
            mEditor.putLong(LASTUPLOAD_SP_KEY, time);

            if (!mEditor.commit()) {
                Log.e(TAG, "Could not commit upload time!");

            }
        }
    }

    public static long getLastSuccessfulUploadTime(Context c) {
        synchronized (mFileUploadLock) {
            long ret;


            // SharedPrefs
            SharedPreferences mSP = c.getSharedPreferences(
                    SHARED_PREFS_NAME,
                    Context.MODE_WORLD_READABLE);
            try {
                ret = mSP.getLong(LASTUPLOAD_SP_KEY, NO_LAST_UPLOAD);
            } catch (ClassCastException e) {
                ret = NO_LAST_UPLOAD;
            }
            return ret;
        }
    }
};
