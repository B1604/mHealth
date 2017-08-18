package com.mhealthproject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

/**
 * Created by emir on 2/16/16.
 */
public class TouchMultipleActivity extends Activity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        DataHolder.getInstance().setRunningAppInt(3);
        View TouchMultipleView = new TouchMultipleView(this);
        setContentView(TouchMultipleView);
        TouchMultipleView.setBackgroundColor(Color.WHITE);

    }





}
