package com.example.citytransformer.navigation;


import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;

import com.example.citytransformer.transitpage.EndPageActivity;
import com.example.citytransformer.R;
import com.example.citytransformer.share.ChargeInfoWindowAdapter;
import com.google.android.gms.maps.LocationSource;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;

@SuppressLint("LogNotTimber")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener, LocationSource,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;

    // for locating current user position, but it's not used now
    // for we set the default user position to Hilton Berlin for convenience
    private final int REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 100;
    private LocationManager mLocationMgr;
    private LocationSource.OnLocationChangedListener mLocationChangedListener;
    private Marker origin;
    private double mlat, mlng;



    //for showing directions and route
    private Polyline currentPolyline;
    private Marker des;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private GeoApiContext mGeoApiContext = null;    //telling the api our geolocation
    private String mDistance;
    private String mDuration;
    private double durationRatio = 1.33; //for changing into CT mode

    //listview
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActBarDrawerToggle;
    private LocaleUtils localeUtils;

    //interactive widgets
    ImageButton mbtnFinish;
    AutoCompleteTextView mSearchText;
    TextView mtripDuration;
    TextView mtripDistance;
    TextView marrivalTime;
    ImageButton mbtnSearch;
    TextView mDestination;
    String TAG = "MapsActivity";
    ConstraintLayout mtripInfo;
    ConstraintLayout mdestinationLayout;
    ImageView mLogo;
    ImageButton mMessageAfterDrive;
    GoogleMap.InfoWindowAdapter mChargeInfoWindowAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_navigation_launcher);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        mLocationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Berlin Hiton Hotel
        mlat = 52.5122;
        mlng = 13.39285;

        //get the google direction api
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.googlemap_api_key))
                    .build();
        }
        //customize action bar
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setLocationSource(this);

        // interactive widgets
        mtripDuration = findViewById(R.id.tripDuration);
        mtripInfo = findViewById(R.id.tripInfo);
        mdestinationLayout = findViewById(R.id.destinationLayout);
        mtripDistance = findViewById(R.id.tripDistance);
        mLogo = findViewById(R.id.logo);
        marrivalTime = findViewById(R.id.arrivalTime);
        mSearchText = findViewById(R.id.destination);
        mbtnFinish = findViewById(R.id.btnFinish);
        mbtnSearch = findViewById(R.id.btnSearch);
        mSearchText.setText("Brandenburg Gate");
        mDestination = findViewById(R.id.destination);
        mMessageAfterDrive = findViewById(R.id.messageAfterDrive);


        mtripInfo.setBackground(null);
        mtripDuration.setVisibility(GONE);
        mtripDistance.setVisibility(GONE);
        marrivalTime.setVisibility(GONE);
        mLogo.setVisibility(GONE);
        mMessageAfterDrive.setVisibility(GONE);
        mdestinationLayout.setVisibility(View.VISIBLE);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getTitle()!=null) mDestination.setText(marker.getTitle());
                else mDestination.setText("Parking");
                mtripInfo.setBackground(getDrawable(R.drawable.rounded_corner));
                mtripInfo.setVisibility(View.VISIBLE);
                mtripDuration.setVisibility(View.VISIBLE);
                mtripDistance.setVisibility(View.VISIBLE);
                marrivalTime.setVisibility(View.VISIBLE);
                mLogo.setVisibility(View.VISIBLE);
            }
        });

        mbtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mtripInfo.setBackground(getDrawable(R.drawable.rounded_corner));
                mtripDuration.setVisibility(View.VISIBLE);
                mtripDistance.setVisibility(View.VISIBLE);
                marrivalTime.setVisibility(View.VISIBLE);
                mLogo.setVisibility(View.VISIBLE);
                geoLocate();
            }
        });

        mbtnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageAfterDrive.setVisibility(View.VISIBLE);
                mtripInfo.setVisibility(GONE);
//                mtripDuration.setVisibility(GONE);
//                mtripDistance.setVisibility(GONE);
//                marrivalTime.setVisibility(GONE);
//                mLogo.setVisibility(GONE);

            }
        });

        mMessageAfterDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder confirmExit  = new AlertDialog.Builder(MapsActivity.this);
                confirmExit.setTitle("Return the Car");
                confirmExit.setMessage("Are you sure to return the car? The car will be locked after 30 seconds.");
                confirmExit.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MapsActivity.this, EndPageActivity.class);
                                startActivity(intent);
                            }
                        });
                confirmExit.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mtripInfo.setVisibility(View.VISIBLE);
                                mdestinationLayout.setVisibility(View.VISIBLE);
                                mMessageAfterDrive.setVisibility(GONE);
                            }
                        });
                confirmExit.show();
            }
        });
        Marker chargeSpot1 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(52.51654, 13.38255))
                .title("Linden Grill")
                .snippet("charge $0.5/kwh")
                 .icon(//BitmapDescriptorFactory.fromBitmap(createCustomMarker(MapsActivity.this, R.drawable.charge_stations_pic)))
                         bitmapDescriptorFromVector(this, R.drawable.charge_stations))
        );
        Marker chargeSpot2 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(52.51133, 13.3892))
                .title("Restaurant Maximilians")
                .snippet("charge $0.75/kwh\n + 8 credit")
                .icon(//BitmapDescriptorFactory.fromBitmap(createCustomMarker(MapsActivity.this, R.drawable.charge_stations_pic)))
                        bitmapDescriptorFromVector(this, R.drawable.charge_stations))
        );
        Marker chargeSpot3 = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(52.51052, 13.3808))
                .title("Mall of Berlin")
                .snippet("charge $0.8/kwh\n"+"credit+5")
                .anchor(1,1)
                .icon(//BitmapDescriptorFactory.fromBitmap(createCustomMarker(MapsActivity.this, R.drawable.charge_stations_pic)))
                        bitmapDescriptorFromVector(this, R.drawable.charge_stations))
        );
        //Marker markerDestination = mMap.addMarker(new Marker)



        List<LatLng> latLngCtPark = new ArrayList<>();
        latLngCtPark.add(new LatLng(52.514450, 13.392090));
        latLngCtPark.add(new LatLng(52.512380, 13.390000));
        latLngCtPark.add(new LatLng(52.515800, 13.379540));
        latLngCtPark.add(new LatLng(52.515869, 13.378940));
        latLngCtPark.add(new LatLng(52.516880, 13.381520));

        List<LatLng> latLngPark = new ArrayList<>();
        latLngPark.add(new LatLng(52.518589, 13.375520));
        latLngPark.add(new LatLng(52.509960, 13.379640));


        List<Marker> markerPark =new ArrayList<>();
        List<Marker> markerCtPark = new ArrayList<>();
        createMarkerByLatlng(markerPark,latLngPark,R.drawable.parking,"2");
        createMarkerByLatlng(markerCtPark,latLngCtPark,R.drawable.ct_park, "0");

        //make every marker in the scene by zooming the screen
        List<LatLng> currentView = new ArrayList<>();
        currentView.add(new LatLng(mlat,mlng));
        for(int i=0;i<markerPark.size();i++){
            currentView.add(new LatLng(
                    markerPark.get(i).getPosition().latitude,markerPark.get(i).getPosition().longitude));
        }
        for(int i=0;i<markerCtPark.size();i++){
            currentView.add(new LatLng(
                    markerCtPark.get(i).getPosition().latitude,markerCtPark.get(i).getPosition().longitude));
        }
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                zoomRoute(mMap,currentView);
            }
        });

        // fetch the posiion last time. This is only used for getting user position, which is not used now.
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Location location =
                    mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                location =
                        mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                Toast.makeText(MapsActivity.this, "Fetch the position successfully",
                        Toast.LENGTH_LONG).show();
                onLocationChanged(location);    // update the position of the map
            } else
                Toast.makeText(MapsActivity.this, "No data for previous position",
                        Toast.LENGTH_LONG).show();
        }
        // When marker is clicked, show route and info window
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                /*TODO Add multiple destinations*/
                calculateDirections(marker,true);
                mtripInfo.setBackground(getDrawable(R.drawable.rounded_corner));
                mtripInfo.setVisibility(View.VISIBLE);
                mtripDuration.setVisibility(View.VISIBLE);
                mtripDistance.setVisibility(View.VISIBLE);
                marrivalTime.setVisibility(View.VISIBLE);
                mLogo.setVisibility(View.VISIBLE);
                if(marker.getTitle()!=null){
                    mChargeInfoWindowAdapter = new ChargeInfoWindowAdapter(MapsActivity.this);
                    mMap.setInfoWindowAdapter(mChargeInfoWindowAdapter);
                    return false;
                }else
                    return true;
            }
        });
    }
    public void createMarkerByLatlng(List<Marker> markerList, List<LatLng> latLngList, int resourceId, String money){
        for(int i=0; i<latLngList.size();i++){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLngList.get(i))
                    .title("Parking")
                    .icon(//BitmapDescriptorFactory.fromBitmap(createCustomMarker(MapsActivity.this, resourceId)))
                            bitmapDescriptorFromVector(this, resourceId))
            );
            if(!money.equals("null")) marker.setSnippet("$"+money+"/hr");
            markerList.add(marker);
        }
    }
    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        ImageView markerImage = marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
//        TextView txt_name = (TextView)marker.findViewById(R.id.name);
//        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }
    //create bitmap from vecter asset xml file
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, 300, 300);
        Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        //vectorDrawable.getIntrinsicWidth(
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    //zoom camera to the size bounding route and markers
    public void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 50;
        LatLngBounds latLngBounds = boundsBuilder.build();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    public void init(){
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();

                }

                return false;
            }
        });
    }
    // for changing the address user want to go to latlng
    // and call calculateDirections to fetch directions form google map
    private void geoLocate(){
        Log.d("geoLocate", "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e("geoLocate", "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d("geoLocate", "geoLocate: found a location: " + address.toString());
            //if want to start with user's current position, used below origin point
            //origin = mMap.addMarker(new MarkerOptions().position(new LatLng(mlat, mlng)).title("My location"));
            des= mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));
            calculateDirections(des,true);

            //create route for the direction
            List<LatLng> route = new ArrayList<>();
            route.add(new LatLng(mlat,mlng));
            route.add(new LatLng(address.getLatitude(), address.getLongitude()));
            zoomRoute(mMap,route);
        }
    }
    private void calculateDirections(Marker marker, boolean showOrHide){
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

                mDuration = String.valueOf((int)result.routes[0].legs[0].duration.inSeconds/60);
                mDistance = result.routes[0].legs[0].distance.humanReadable; //adjust the unit to user friendly format automatically
                mtripDistance.setText(mDistance);
                mtripDuration.setText(mDuration+ " min");
                Calendar now = Calendar.getInstance();
                double tempDuration = result.routes[0].legs[0].duration.inSeconds;
                double min = now.get(Calendar.MINUTE) + Math.round(tempDuration/60) ;
                if( min >=60){
                    min = min - 61;
                    if(min < 10)
                        marrivalTime.setText(now.get(Calendar.HOUR_OF_DAY)+1+":"+"0"+ (int)min);
                    else  marrivalTime.setText(now.get(Calendar.HOUR_OF_DAY)+1+":"+ (int)min);
                }else{
                    if(min < 10)
                        marrivalTime.setText(now.get(Calendar.HOUR_OF_DAY) +":"+"0"+ (int)min);
                    else  marrivalTime.setText(now.get(Calendar.HOUR_OF_DAY)+":"+ (int)min);
                }

                addPolylinesToMap(result,showOrHide);
                Geocoder geocoder=new Geocoder(MapsActivity.this);
                try {
                    List<Address> addressList=geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude,1);
                    for(Address address:addressList){
                        Log.d("TAG",address.getAddressLine(0));
                        mDestination.setText(address.getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(getResources().getColor(R.color.colorPrimary));
                    polyline.setClickable(true);
                    polyline.setVisible(showOrHide);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                }
            }
        });
    }
    //Inspect the user allow using GPS or not
    private void checkLocationPermissionAndEnableLocationUpdate(boolean on) {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Not yet ask for user permission
            // Start the process of asking permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(MapsActivity.this);
                altDlgBuilder.setTitle("Alert");
                altDlgBuilder.setMessage("App needs to turn on the GPS");
                altDlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("OK",
                        (dialogInterface, i) -> {
                            // requestPermissions has permit and reject two button
                            // After user reply, implement callback onRequestPermissionsResult()
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{
                                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);
                        });
                altDlgBuilder.show();

                return;
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION);

                return;
            }
        }

        // This function has been granted by the user before
        // Turn on or off the GPS funtion according to the value of ON
        if (on) {
            // If GPS is turned on, use it. Otherwise, use the internet positioning
            mMap.setMyLocationEnabled(true);   // If tested on the computer simulater, add this code
            if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationMgr.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 5000, 5,this );
                Toast.makeText(MapsActivity.this, "Using GPS postioning", Toast.LENGTH_LONG)
                        .show();
            } else if (mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationMgr.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 5000, 5, this);
                Toast.makeText(MapsActivity.this, "Using internet positioning", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            mLocationMgr.removeUpdates(this);
            Toast.makeText(MapsActivity.this, "Positioning function has been stopped", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Inspect the permission code is same as what we send or not
        if (requestCode == REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check it again and enter the granted condition and activate
                checkLocationPermissionAndEnableLocationUpdate(true);
                init();
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onLocationChanged(Location location) {
        // Send the new position to my location layer in Google map
        if (mLocationChangedListener != null)
            mLocationChangedListener.onLocationChanged(location);

        /* If want to use user's current position, use this
        mlat= location.getLatitude();
        mlng = location.getLongitude();*/


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String str = provider;
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                str += "GPS is not functioning";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                str += "GPS is getting the position";
                break;
        }
        Toast.makeText(MapsActivity.this, str, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(MapsActivity.this, provider + "Positioning function is open",
                Toast.LENGTH_LONG).show();
        checkLocationPermissionAndEnableLocationUpdate(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MapsActivity.this, provider + "Positioning function ",
                Toast.LENGTH_LONG).show();
        checkLocationPermissionAndEnableLocationUpdate(false);
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mLocationChangedListener = listener;
        checkLocationPermissionAndEnableLocationUpdate(true);
        Toast.makeText(MapsActivity.this, "My location Layer has been enabled",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void deactivate() {
        mLocationChangedListener = null;
        checkLocationPermissionAndEnableLocationUpdate(false);
        Toast.makeText(MapsActivity.this, "My location Layer has been unabled",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mMap!=null)
            checkLocationPermissionAndEnableLocationUpdate(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkLocationPermissionAndEnableLocationUpdate(false);
    }

}