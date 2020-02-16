package com.example.citytransformer.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import com.example.citytransformer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by User on 10/2/2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    public View mWindow;
    public Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.share_info_window, null);
    }

    private void rendowWindowText(Marker marker, View view){

//        ShareActivity.calculateDirections(marker,true);
        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.carInfo);

        if(!title.equals("")){
            tvTitle.setText(title);
        }
        String snippet = marker.getSnippet();
        TextView tvSnippet = (TextView) view.findViewById(R.id.btnDistanceTime);
        tvSnippet.setText(snippet);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        if(marker.getTitle()!=null){
            rendowWindowText(marker, mWindow);
            return mWindow;
        }
        else return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }
}