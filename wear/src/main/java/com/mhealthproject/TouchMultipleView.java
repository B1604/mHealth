package com.mhealthproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
 * Created by emir on 2/16/16.
 */
public class TouchMultipleView extends View {
    final public static String TAG = "TouchMultipleView";
    private int xMin=0;
    private int xMax;
    private int yMin=0;
    private int yMax;

    private float ballRadius = 30;
    private float ballX = 40;
    private float ballY = 150;
    private float ballSpeedX ;
    private float ballSpeedY;
    private RectF ballBounds;
    private Paint paint;

    float touch_major ;
    float touch_minor ;
    float touch_time ;
    float touch_x ;
    float touch_y ;
    float touch_size ;
    float touch_pressure;
    short blueHit;
    float math;
    String touch_events;

    Logger mLogger;

    int sq = 50;

    private boolean pressed;
    private int counter;

    private boolean newShape= true;

    private AtomicReference<Vibrator> vibratorRef =
            new AtomicReference<Vibrator>();


    public TouchMultipleView(Context context) {
        super(context);
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

        if(isInShape(event.getX(), event.getY())){
            counter++;
            //  Toast.makeText(this, counter,Toast.LENGTH_SHORT).show();
            Log.d(TAG, "In shape " + counter);
            ballRadius = (float) (ballRadius - Math.random());
            if(ballRadius<=0) ballRadius =2;


            ballSpeedX = ballSpeedX +2;
            ballSpeedY = ballSpeedY +1;

        }
        else {
            Log.d(TAG, "Try again ");
            //counter--;
            Vibrator v = vibratorRef.get();
            if (v != null) {
                v.vibrate(0L);
            }
        }


        touch_major =touch_major + event.getTouchMajor();
        touch_minor =touch_minor + event.getTouchMinor();
        touch_time = touch_time + event.getEventTime();
        touch_x = touch_x + event.getX();
        touch_y = touch_y + event.getY();
        touch_size = touch_size + event.getSize();
        touch_pressure = touch_pressure + event.getPressure();

        String touch_events;




        Log.d(TAG, "Touch events: \n" + "Touch_major: " + touch_major + " \n"
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

        mLogger.logEntry("Touch Events-3: " + touch_events);
        writeToFile("Touch Events-3: " + touch_events);

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


    @Override
    protected void onDraw(Canvas canvas) {
        if(newShape){
            math= (float) Math.random();
            newShape = false;
        }

        if(math<= 0.3) {
            ballBounds.set(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius);
            paint.setColor(Color.GREEN);
            canvas.drawOval(ballBounds, paint);
            canvas.drawText("count: " + counter, 60, 60, paint);
            update(1);
        }else if(math>= 0.7){
            ballBounds.set(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius);
            paint.setColor(Color.BLUE);
            canvas.drawOval(ballBounds, paint);
            canvas.drawText("count: " + counter, 60, 60, paint);
            update(2);
        }else {
            ballBounds.set(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius);
            paint.setColor(Color.RED);
            canvas.drawOval(ballBounds, paint);
            canvas.drawText("count: " + counter, 60, 60, paint);
            update(3);
        }

        try{
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        invalidate();
    }

    public static void getTouchEvents(){



        /*
        return "Touch events -3: \n"    + "Touch_major: " + touch_major + " \n"
                + "Touch_minor: " + touch_minor + "\n"
                + "Touch_time: " + touch_time + "\n"
                + "Touch_x: " + touch_x + "\n"
                + "Touch_y: " + touch_y + "\n"
                + "Touch_size: " + touch_size + "\n"
                + "Touch_pressure: " + touch_pressure + "\n"
                + "Is it in target: " + blueHit;
   */

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


    private void update(int shape){

        switch (shape){
            case 1:
                ballX +=ballSpeedX;
                if(ballX-ballRadius > xMax){
                    newShape = true;
                    ballRadius = 30;
                    ballX = 40;
                    ballY = 150;
                }
                break;
            case 2:
                ballX +=ballSpeedX;
                if(ballX-ballRadius > xMax){
                    newShape = true;
                    ballRadius = 30;
                    ballX = 40;
                    ballY = 150;
                }
                break;
            case 3:
                ballX +=ballSpeedX;
                if(ballX-ballRadius > xMax){
                    newShape = true;
                    ballRadius = 30;
                    ballX = 40;
                    ballY = 150;
                }
                break;


        }

    }

    public void onSizeChanged(int w, int h, int oldW, int oldH){
        xMax = w-1;
        yMax = h-1;
    }
}
