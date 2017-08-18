package com.mhealthproject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by ZhouWenting on 5/17/17.
 */

public class RankStressActivity extends Activity{
    private Button stress1Btn;
    private Button stress2Btn;
    private Button stress3Btn;
    private Button stress4Btn;
    private Button stress5Btn;
    private Button submitBtn;
    private int tmpRank;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank_stress);

        stress1Btn = (Button) findViewById(R.id.stress1Id);
        setStressBtnListener(stress1Btn,1);
        stress2Btn = (Button) findViewById(R.id.stress2Id);
        setStressBtnListener(stress2Btn,2);
        stress3Btn = (Button) findViewById(R.id.stress3Id);
        setStressBtnListener(stress3Btn,3);
        stress4Btn = (Button) findViewById(R.id.stress4Id);
        setStressBtnListener(stress4Btn,4);
        stress5Btn = (Button) findViewById(R.id.stress5Id);
        setStressBtnListener(stress5Btn,5);

        submitBtn = (Button) findViewById(R.id.submitR);
        submitRank(submitBtn);

    }

    // This is a general function call for the button click
    private void setStressBtnListener (Button btn, final int id) {
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    tmpRank = id;
                    TextView rankView = (TextView) findViewById(R.id.rankView);
                    rankView.setText("You rank: " + id);
                }
            });
        }
    }

    // Store the final rank to the Dataholder class, which will be finally used in the SensorService class
    private void submitRank(Button btn) {
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tmpRank != 0) {
                        DataHolder.getInstance().setStressRank(tmpRank);
                        Toast.makeText(RankStressActivity.this,"you submit rank: " + tmpRank,Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RankStressActivity.this,"Please click on your stress level",Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }


    // This is the same as the function in the MainActivity
    private void writeToFile_CA(String data) {

        Date date = new Date();
        File logFile = new File("sdcard/mHealth2.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(data);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
