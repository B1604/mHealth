package com.mhealthproject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

/**
 * Created by emir on 2/4/16.
 */
public class CatchColorActivity extends Activity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        DataHolder.getInstance().setRunningAppInt(1);
        View CatchColorView = new CatchColorView(this);
        setContentView(CatchColorView);
        CatchColorView.setBackgroundColor(Color.WHITE);
    } //B1604 was here :D
}