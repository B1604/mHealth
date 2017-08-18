package com.mhealthproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by emir on 2/4/16.
 */
public class CatchColorView extends View {

    final public static String TAG = "CatchColorView";
    private int xMin=0;
    private int xMax;
    private int yMin=0;
    private int yMax;

    private float ballRadius = 40;
    private float ballX = ballRadius + 120;
    private float ballY = ballRadius + 120;
    private RectF ballBounds;
    private Paint paint;


    private int counter;
    private int sleepTime = 2000;
    private boolean itIsBlue =false;
    private AtomicReference<Vibrator> vibratorRef =
            new AtomicReference<Vibrator>();

    float touch_major ;
    float touch_minor ;
    float touch_time ;
    float touch_x ;
    float touch_y ;
    float touch_size ;
    float touch_pressure;
    short blueHit;
    String touch_events;

    Context context;

    Logger mLogger;


    public CatchColorView(Context context) {
        super(context);
        this.context= context;
        ballBounds = new RectF();
        paint = new Paint();

        mLogger = new Logger();

        //  this. setFocusable(true);
        //   this.requestFocus();
        this.setFocusableInTouchMode(true);

    }



    public boolean isInShape(float x, float y){

        return x >= Math.min(ballX-ballRadius, ballX+ballRadius) && x <= Math.max(ballX-ballRadius, ballX+ballRadius)
                && y >= Math.min(ballY-ballRadius, ballY+ballRadius) && y <= Math.max(ballY-ballRadius, ballY+ballRadius);
    }

    @Override
    public boolean onTouchEvent( MotionEvent event) {
        Log.d(TAG, "Touch event: " + event.getAction() + " X and ballX: " + event.getX() + " " + ballX +
                " Y and ballY: " + event.getY() + " " + ballY);


        if(isInShape(event.getX(), event.getY()) & itIsBlue){
            counter++;
            //  Toast.makeText(this, counter,Toast.LENGTH_SHORT).show();
            Log.d(TAG, "In shape " + counter);
            sleepTime = sleepTime - 100;
            ballRadius = ballRadius - 3;
            itIsBlue= false;
            blueHit =1;
        }
        else {
            Log.d(TAG, "Try again ");
            // counter--;
            Vibrator v = vibratorRef.get();
            if (v != null) {
                v.vibrate(0L);
            }
        }
        // all the features can be got

        touch_major =touch_major + event.getTouchMajor();
        touch_minor =touch_minor + event.getTouchMinor();
        touch_time = touch_time + event.getEventTime();
        touch_x = touch_x + event.getX();
        touch_y = touch_y + event.getY();
        touch_size = touch_size + event.getSize();
        touch_pressure = touch_pressure + event.getPressure();


        Log.d(TAG, "Touch events -1: \n"    + "Touch_major: " + touch_major + " \n"
                + "Touch_minor: " + touch_minor + "\n"
                + "Touch_time: " + touch_time + "\n"
                + "Touch_x: " + touch_x + "\n"
                + "Touch_y: " + touch_y + "\n"
                + "Touch_size: " + touch_size + "\n"
                + "Touch_pressure: " + touch_pressure + "\n"
                + "Is it in target: " + blueHit);

        touch_events = "Touch events: \n"    + "Touch_major: " + touch_major + " \n"
                + "Touch_minor: " + touch_minor + "\n"
                + "Touch_time: " + touch_time + "\n"
                + "Touch_x: " + touch_x + "\n"
                + "Touch_y: " + touch_y + "\n"
                + "Touch_size: " + touch_size + "\n"
                + "Touch_pressure: " + touch_pressure + "\n"
                + "Is it in target: " + blueHit;

        mLogger.logEntry("Touch Events-1: " + touch_events);
        writeToFile("Touch Events-1: " + touch_events);



/*
        /*
        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:{
                if(isInShape(event.getX(), event.getY())){
                    pressed = true;
                 //   v.setPressed(true);
                    Toast.makeText(getContext(), "No1",Toast.LENGTH_SHORT).show();
                    return false;
                }
                else{
                    pressed = false;
                  //  v.setPressed(false);
                }
            }
            case MotionEvent.ACTION_MOVE:{
                if(isInShape(event.getX(), event.getY())){
                    Toast.makeText(getContext(), "No2",Toast.LENGTH_SHORT).show();
                    return false;
                }
                else{
                    pressed = false;
                   // v.setPressed(false);
                }
            }
            case MotionEvent.ACTION_UP:{
                if(pressed && isInShape(event.getX(), event.getY())){
                  //  v.performClick();
                    counter++;
                    Toast.makeText(getContext(), counter,Toast.LENGTH_SHORT).show();
                }
                pressed = false;
               // v.setPressed(false);
            }
        }

        */
        return true;
    }


    private void writeToFile(String data) {

        Date date = new Date();
        File logFile = new File("sdcard/mHealthLogs.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(data);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public String getTouchEvents(){
        return "Touch events -1: \n"    + "Touch_major: " + touch_major + " \n"
                + "Touch_minor: " + touch_minor + "\n"
                + "Touch_time: " + touch_time + "\n"
                + "Touch_x: " + touch_x + "\n"
                + "Touch_y: " + touch_y + "\n"
                + "Touch_size: " + touch_size + "\n"
                + "Touch_pressure: " + touch_pressure + "\n"
                + "Is it in target: " + blueHit;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        ballBounds.set(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius);
        float random_number = (float) Math.random();

        if(random_number>=0.5){
            if(random_number>=0.7){
                paint.setColor(Color.GREEN);
                itIsBlue = false;
            }
            else {
                paint.setColor(Color.BLUE);
                itIsBlue = true;
            }
        }
        else {
            if(random_number<0.3){
                paint.setColor(Color.RED);
                itIsBlue = false;
            } else{
                paint.setColor(Color.YELLOW);
                itIsBlue = false;
            }
        }

        canvas.drawOval(ballBounds, paint);
        canvas.drawText("count: " + counter, 80, 60, paint);
        //   update(canvas);

        if(DataHolder.getInstance().getSendTouchEvents()){
            DataHolder.getInstance().setTouchEvents(touch_events);
        }


        try{
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        invalidate();
    }

    public void resetEvents(){
        touch_events ="";
        touch_major=0 ;
        touch_minor=0 ;
        touch_time =0;
        touch_x =0;
        touch_y =0;
        touch_size =0;
        touch_pressure=0;
        blueHit =0;
    }

    private void update(Canvas canvas){
        float random_number = (float) Math.random();
        if(random_number<=0.2){
            paint.setColor(Color.GREEN);
            itIsBlue = false;
        }
        else if(random_number<=0.4){
            paint.setColor(Color.BLUE);
            itIsBlue = true;
        }
        else if(random_number<=0.6){
            paint.setColor(Color.RED);

            itIsBlue = false;
        }
        else if(random_number<=0.8){
            paint.setColor(Color.YELLOW);

            itIsBlue = false;
        }

    }

    public void onSizeChanged(int w, int h, int oldW, int oldH){
        xMax = w-1;
        yMax = h-1;
    }


}
