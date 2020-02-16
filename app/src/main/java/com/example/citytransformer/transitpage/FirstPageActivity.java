package com.example.citytransformer.transitpage;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.citytransformer.R;
import com.example.citytransformer.navigation.MapsActivity;

public class FirstPageActivity extends AppCompatActivity {

    private static int TIME_OUT = 4000; //Time to launch the another activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tablet_welcome);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Uri uri = Uri.parse("android.resource://"+
                getPackageName() + "/" + R.raw.welcome);
        MediaPlayer mp =MediaPlayer.create(FirstPageActivity.this,uri);
        mp.start();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(FirstPageActivity.this, MapsActivity.class);
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }
}
