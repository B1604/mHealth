package com.mhealthproject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

/**
 * Created by emir on 2/3/16.
 */
public class MovingBallActivity extends Activity {


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        DataHolder.getInstance().setRunningAppInt(2);
        View MovingBallView = new MovingBallView(this);
        setContentView(MovingBallView);
        MovingBallView.setBackgroundColor(Color.WHITE);

    }




}
