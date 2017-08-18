package com.mhealthproject;


import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by B on 2/14/2016.
 */
public class Task extends AsyncTask<String, String, String> {

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            FileInputStream fstrm = new FileInputStream(params[0]);
            HttpFileUpload hfu = new HttpFileUpload(params[1], "title", "desc");
            hfu.Send_Now(fstrm);

        } catch (FileNotFoundException e) {
            Log.e("File Not Found", e.getMessage());
        }
        return "";
    }
}
