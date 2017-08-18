package com.mhealthproject;


import static com.google.android.gms.internal.zzir.runOnUiThread;


/**
 * Created by B on 3/6/2016.
 */
public class MyBoolean {
    private boolean value;
    public void setter(boolean x){
        value = x;
        //MainActivity.changeImage();
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if(value){ //Stressed
                    MainActivity.imv.setImageResource(R.drawable.dead);
                }
                else{ //Not Stressed
                    MainActivity.imv.setImageResource(R.drawable.happy_alternative2);
                }
            }
        });
    }
    public boolean getter(){
        return value;
    }
}
