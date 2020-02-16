package com.example.citytransformer.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.example.citytransformer.Bluetooth.BluetoothConnectionService;
import com.example.citytransformer.navigation.CustomInfoWindowAdapter;
import com.example.citytransformer.R;
import com.example.citytransformer.navigation.PolylineData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Button mbtnUnlock, mbtnRing;
    private ImageView mimgDistance;

    private TextView mtextDistance, mtextTime;
    private  String TAG = "ShareActivity";
    private BluetoothConnectionService mBluetoothConnection;
    private GoogleMap.InfoWindowAdapter mCustomInfoWindowAdapter;

    private final int REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 100;
    private LocationManager mLocationMgr;
    private LocationSource.OnLocationChangedListener mLocationChangedListener;

    //for showing directions and route
    private Polyline currentPolyline;
    private Marker des;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private GeoApiContext mGeoApiContext = null;    //telling the api our geolocation
    private String mDistance;
    private String mDuration;
    private double mlat, mlng;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        mbtnUnlock = findViewById(R.id.btnUnlock);
        mbtnRing = findViewById(R.id.btnRing);
        mimgDistance = findViewById(R.id.imgDistanceTime);
        mtextDistance = findViewById(R.id.textDistance);
        mtextTime = findViewById(R.id.textTime);


        mimgDistance.setVisibility(View.GONE);
        mtextDistance.setVisibility(View.GONE);
        mtextTime.setVisibility(View.GONE);
        mbtnRing.setVisibility(View.GONE);

        //Connect bluetooth from BluetoothConnectionService class
        mBluetoothConnection = BluetoothConnectionService.getInstance(ShareActivity.this);
        mBluetoothConnection.setHandler(mHandler);

        //Berlin Hiton Hotel
        mlat = 52.5122;
        mlng = 13.39285;

        //get the google direction api
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.googlemap_api_key))
                    .build();
        }

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);

        //mBluetoothConnection = new MainActivity.BluetoothConnectionService(ShareActivity.this);
        mbtnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"btnUnlock, mBluetoothConnection.write ");
                byte[] bytes = "Unlock".getBytes();
                Intent intent = new Intent(ShareActivity.this,UnlockActivity.class);
                startActivity(intent);
            }
        });

    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what==1){
                Intent intent = new Intent(ShareActivity.this, UnlockActivity.class);
                startActivity(intent);
            }
            return true;
        }
    });

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                mimgDistance.setVisibility(View.VISIBLE);
                mtextDistance.setVisibility(View.VISIBLE);
                mtextTime.setVisibility(View.VISIBLE);
                mbtnRing.setVisibility(View.VISIBLE);
            }
        });
        Marker markerCar1 = mMap.addMarker(new MarkerOptions()
                        .position(new com.google.android.gms.maps.model.LatLng(52.51335, 13.39561))
                        .snippet("371m 1min")
                        .title("Fb520           €0.4/min\n"+"Battery left           50min")
                        .icon(bitmapDescriptorFromVector(this, R.drawable.car_marker))
        );
        Marker markerCar2 = mMap.addMarker(new MarkerOptions()
                .position(new com.google.android.gms.maps.model.LatLng(52.51182, 13.38518))
                .title("IG109       €0.4/min\n"+"Battery left           40min")
                .snippet("533m 2min")
                .icon(bitmapDescriptorFromVector(this, R.drawable.car_marker))
        );
        Marker markerCar3 = mMap.addMarker(new MarkerOptions()
                .position(new com.google.android.gms.maps.model.LatLng(52.51439, 13.39239))
                .title("AI007       €0.4/min\n"+"Battery left           2hr")
                .snippet("277m 1min")
                .icon(bitmapDescriptorFromVector(this, R.drawable.car_marker_red))
        );


        List<com.google.android.gms.maps.model.LatLng> currentView = new ArrayList<>();
        currentView.add(new com.google.android.gms.maps.model.LatLng(mlat,mlng));
        currentView.add(new com.google.android.gms.maps.model.LatLng (markerCar1.getPosition().latitude, markerCar1.getPosition().longitude));
        currentView.add(new com.google.android.gms.maps.model.LatLng (markerCar2.getPosition().latitude, markerCar2.getPosition().longitude));
        currentView.add(new com.google.android.gms.maps.model.LatLng (markerCar3.getPosition().latitude, markerCar3.getPosition().longitude));
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                zoomRoute(mMap,currentView);
            }
        });

        // When marker is clicked, show route and info window
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                /*TODO Add multiple destinations*/
                calculateDirections(marker,true);
                mCustomInfoWindowAdapter = new CustomInfoWindowAdapter(ShareActivity.this);
                mMap.setInfoWindowAdapter(mCustomInfoWindowAdapter);
                return false;
            }
        });
    }

    //create bitmap from vecter asset xml file
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    //zoom camera to the size bounding route and markers
    public void zoomRoute(GoogleMap googleMap, List<com.google.android.gms.maps.model.LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (com.google.android.gms.maps.model.LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        LatLngBounds latLngBounds = boundsBuilder.build();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    public void calculateDirections(Marker marker, boolean showOrHide){
        Log.d("calculateDirections", "calculating directions.");
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );

        //request google directions api to sent the data
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(false);
        directions.origin(new com.google.maps.model.LatLng(mlat,mlng));
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance_img: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "onResult: successfully retrieved directions.");

                // highlight the fastest route and adjust camera
                mDuration = String.valueOf((int)result.routes[0].legs[0].duration.inSeconds/60);
                mDistance = String.valueOf((int)result.routes[0].legs[0].distance.inMeters);

                //marker.setSnippet(mDistance + " " + mDuration);
                mtextTime.setText(mDuration+"min");
                mtextDistance.setText(mDistance +"m");
                addPolylinesToMap(result,showOrHide);

            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });

    }

    private void addPolylinesToMap(final DirectionsResult result, boolean showOrHide){
        new Handler(Looper.getMainLooper()).post(new Runnable() {   // to ensure the following code will be run in main thread
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolyLinesData.size() > 0){
                    for(PolylineData polylineData: mPolyLinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<com.google.android.gms.maps.model.LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new com.google.android.gms.maps.model.LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(getResources().getColor(R.color.colorPrimary));
                    polyline.setClickable(true);
                    polyline.setVisible(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                }
            }
        });
    }
}

