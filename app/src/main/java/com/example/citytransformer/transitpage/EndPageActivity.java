package com.example.citytransformer.transitpage;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.citytransformer.R;
import com.example.citytransformer.navigation.MapsActivity;

import java.util.Timer;
import java.util.TimerTask;

public class EndPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tablet_welcome);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        TextView mText = findViewById(R.id.text);
        TextView mSlogan = findViewById(R.id.slogan);
        mText.setText("See you next Time!");
        mSlogan.setText(null);

        Timer timer=new Timer();
        TimerTask task=new TimerTask(){
            public void run(){
                Intent intent;
                Uri uri = Uri.parse("android.resource://"+
                        getPackageName() + "/" + R.raw.goodbye);
                MediaPlayer mp =MediaPlayer.create(EndPageActivity.this,uri);
                mp.start();
                intent = new Intent(EndPageActivity.this, MapsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
        timer.schedule(task, 2500);
    }
}