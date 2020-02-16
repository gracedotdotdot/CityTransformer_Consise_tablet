package com.example.citytransformer.transitpage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.example.citytransformer.Bluetooth.BluetoothConnectionService;
import com.example.citytransformer.R;

public class BlackScreenActivity extends AppCompatActivity {
    BluetoothConnectionService mBluetoothConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blackscreen);
        //hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Connect bluetooth from BluetoothConnectionService class
        mBluetoothConnection = BluetoothConnectionService.getInstance(BlackScreenActivity.this);
        mBluetoothConnection.setHandler(mHandler);
    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what==1){
                Intent intent = new Intent(BlackScreenActivity.this, FirstPageActivity.class);
                intent.putExtra("0","tablet");
                startActivity(intent);
            }
            return true;
        }
    });
}


