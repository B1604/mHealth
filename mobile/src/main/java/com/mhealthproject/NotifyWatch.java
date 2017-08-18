package com.mhealthproject;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by B on 2/12/2016.
 */
public class NotifyWatch extends Thread{
    final private static boolean DBG = true;
    final public static String TAG = "SmartWatchLogUpload";

    private static final String SEND_TO_WATCH_DEL_PATH = "/send-to-watch-del-path";
    private static final String SEND_TO_WATCH_DEL_DATA = "send-to-watch-del-data";

    final static public String ACTION_FILE_UPLOAD_RESULT = "edu.northwestern.Watchlogger.FILE_UPLOAD_RESULT";

    //DataService e = new DataService();
    private Service mContext;
    public BluetoothAdapter bluetoothAdapter;
    private GoogleApiClient googleApiClient;
    //private boolean connected;



    public NotifyWatch(Service c) {
        mContext = c;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Intent intent = new Intent(ACTION_FILE_UPLOAD_RESULT);
                intent.putExtra("data", (Boolean) false);
                intent.putExtra("timestamp", (Long) java.lang.System.currentTimeMillis());
                intent.putExtra("note", "Failed: Uncaught Exception!");
                mContext.sendBroadcast(intent);
                getGoogleClient();
            }
        });
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
    private boolean bluetoothConnection(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            if (DBG) {
                Log.i(TAG, "Bluetooth is not supported right now..");
                return false;
            }
        }
        else {
            if (bluetoothAdapter.isEnabled()) {
                if (DBG) {
                    Log.i(TAG, "Bluetooth Connected, Trying to send the file to phone..");
                    return true;
                }
            } else {
                if (DBG) {
                    Log.i(TAG, "Bluetooth is not Connected, will try later to send the file to phone..");
                    return false;
                }
            }
        }
        return false;
    }
    public void run() {
        if(bluetoothConnection()){
            getGoogleClient();

            List<Node> connectedNodes =
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
            if(!connectedNodes.isEmpty()) {
                if (DBG)
                    Log.i(TAG, "Watch is connected: " + connectedNodes + " size is: " + connectedNodes.size());
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                try {
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), SEND_TO_WATCH_DEL_PATH, SEND_TO_WATCH_DEL_DATA.getBytes()).await();
                        if (!result.getStatus().isSuccess()) {

                        } else {
                            if (DBG) Log.d(TAG, "Watch was Notified");
                        }
                    }

                } catch (Exception e) {}
            }
        }
    }
}
