package com.example.citytransformer.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.citytransformer.share.ShareActivity;
import com.example.citytransformer.transitpage.BlackScreenActivity;
import com.example.citytransformer.transitpage.FirstPageActivity;
import com.example.citytransformer.R;
import com.example.citytransformer.navigation.MapsActivity;


import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


@SuppressLint("LogNotTimber")
    public class  MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
        private static final String TAG = "MainActivity";

        BluetoothAdapter mBluetoothAdapter;
        Button btnEnableDisable_Discoverable;
        BluetoothConnectionService mBluetoothConnection;
        Button btnStartConnection;
        Button btnShare, mbtnBlackScreen;


        BluetoothDevice mBTDevice;
        public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
        public DeviceListAdapter mDeviceListAdapter;
        ListView lvNewDevices;

        private final UUID MY_UUID_INSECURE =
                UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


        // Create a BroadcastReceiver for ACTION_FOUND
        private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "onReceive: STATE OFF");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                            break;
                    }
                }
            }
        };

        /**
         * Broadcast Receiver for changes made to bluetooth states such as:
         * 1) Discoverability mode on/off or expire.
         */
        private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                    int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                    switch (mode) {
                        //Device is in Discoverable Mode
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                            break;
                        //Device not in discoverable mode
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                            break;
                        case BluetoothAdapter.SCAN_MODE_NONE:
                            Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                            break;
                        case BluetoothAdapter.STATE_CONNECTING:
                            Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            Log.d(TAG, "mBroadcastReceiver2: Connected.");
                            break;
                    }

                }
            }
        };


        /**
         * Broadcast Receiver for listing devices that are not yet paired
         * -Executed by btnDiscover() method.
         */
        private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "onReceive: ACTION FOUND.");

                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);
                }
            }
        };

        /**
         * Broadcast Receiver that detects bond state changes (Pairing status changes)
         */
        private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //3 cases:
                    //case1: bonded already
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                        //inside BroadcastReceiver4
                        mBTDevice = mDevice;
                    }
                    //case2: creating a bond
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                    }
                    //case3: breaking a bond
                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    }
                }
            }
        };


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_bluetooth);
            Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
            btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
            lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
            mBTDevices = new ArrayList<>();

            btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
            btnShare = (Button) findViewById(R.id.btnShareScreen);
            mbtnBlackScreen = findViewById(R.id.btnBlackScreen);


            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            //Broadcasts when bond state changes (ie:pairing)
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver4, filter);
            //Broadcasts when discoverable state changes
            IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2, intentFilter);

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            lvNewDevices.setOnItemClickListener( MainActivity.this);
            //Broadcast after check BT permissions in manifest to discover BT near the device
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

            //Connect bluetooth from BluetoothConnectionService class
            mBluetoothConnection = BluetoothConnectionService.getInstance(MainActivity.this);
            mBluetoothConnection.setHandler(mHandler);


            btnONOFF.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                    enableDisableBT();
                }
            });

            btnStartConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startConnection();
                }
            });

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ShareActivity.class);
                    intent.putExtra("0","tablet");
                    startActivity(intent);
                }
            });
            mbtnBlackScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, BlackScreenActivity.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        protected void onDestroy() {
            Log.d(TAG, "onDestroy: called.");
            super.onDestroy();
            unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver2);
            unregisterReceiver(mBroadcastReceiver3);
            unregisterReceiver(mBroadcastReceiver4);
            mBluetoothAdapter.cancelDiscovery();
        }

        private Handler mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what==1){
                    Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
                    intent.putExtra("0","tablet");
                    startActivity(intent);
                }
                return true;
            }
        });

//create method for starting connection
//***remember the connection will fail and app will crash if you haven't paired first
        public void startConnection() {
            startBTConnection(mBTDevice, MY_UUID_INSECURE);
        }
        /**
         * starting chat service method
         */
        public void startBTConnection(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
            mBluetoothConnection.startClient(device, uuid);
        }


        public void enableDisableBT() {
            if (mBluetoothAdapter == null) {
                Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "enableDisableBT: enabling BT.");
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }
            if (mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "enableDisableBT: disabling BT.");
                mBluetoothAdapter.disable();

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }

        }


        public void btnEnableDisable_Discoverable(View view) {
            Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

            IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2, intentFilter);

        }


        public void btnDiscover(View view) {
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "btnDiscover: Canceling discovery.");

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
            if (!mBluetoothAdapter.isDiscovering()) {

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
        }


        /**
         * This method is required for all devices running API23+
         * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
         * in the manifest is not enough.
         * <p>
         * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
         */
        private void checkBTPermissions() {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0) {

                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            } else {
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }


        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //first cancel discovery because its very memory intensive.
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "onItemClick: You Clicked on a device.");
            String deviceName = mBTDevices.get(i).getName();
            String deviceAddress = mBTDevices.get(i).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            //create the bond.
            //NOTE: Requires API 17+? I think this is JellyBean
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "Trying to pair with " + deviceName);
                mBTDevices.get(i).createBond();

                mBTDevice = mBTDevices.get(i);
                mBluetoothConnection = BluetoothConnectionService.getInstance(MainActivity.this);
            }
        }

        public void searchBondedBT(){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress(); // MAC address
                    Log.d("searchBondBT", "GET deviceName = " + deviceName);
                    Log.d("searchBondBT", "GET deviceAddress = " + deviceAddress);
                    mBTDevice = device;
                    mBluetoothConnection = BluetoothConnectionService.getInstance(MainActivity.this);
                }
            }
        }

    }


