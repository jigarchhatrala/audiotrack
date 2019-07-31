package com.example.dhara.audiotrack;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements Flesh.FrequencyAck {

    TextView textView;
    Switch aSwitch;
    Flesh flesh = null;
    TextView frequencyText, detectedText, falseCountText, timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 1);
        textView = findViewById(R.id.Onofftext);
        aSwitch = findViewById(R.id.onoffswithch);
        frequencyText = findViewById(R.id.frequencyText);
        detectedText = findViewById(R.id.detectedText);
        falseCountText = findViewById(R.id.falseCountText);
        timerText = findViewById(R.id.timerText);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    on();
                } else {
                    off();
                }
            }
        });
    }

    private void on() {
        aSwitch.setChecked(true);
        textView.setText("ON");
        flesh = new Flesh(this, this);
        flesh.start();
    }

    private void off() {
        aSwitch.setChecked(false);
        textView.setText("OFF");
        if (flesh != null) {
            if(flesh.isRunning){
                flesh.end();
            }
        }
    }

    @Override
    public void frequencyData(final long frequency, final long detectedCount, final long falseCount, final long timer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                falseCountText.setText("" + falseCount);
                detectedText.setText("" + detectedCount);
                frequencyText.setText("" + frequency);
                timerText.setText("" + timer);
            }
        });
    }

    @Override
    public void closeTrack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                off();
            }
        });
    }
}
