package com.example.citytransformer.share;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.citytransformer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by User on 10/2/2017.
 */

public class ChargeInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    public View mWindow;
    public Context mContext;

    public ChargeInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.charge_info_window, null);
    }

    private void rendowWindowText(Marker marker, View view){


        String title = marker.getTitle();
        String snippet = marker.getSnippet();
        TextView tvTitle = (TextView) view.findViewById(R.id.chargeSpotName);
        TextView tvSnippet = (TextView) view.findViewById(R.id.chargeInfo);
        ConstraintLayout mInfoWindowLayout = view.findViewById(R.id.infoWindowLayout);
        mInfoWindowLayout.setVisibility(View.GONE);
        if(title != null){
            tvTitle.setText(title);
        }

        if(snippet != null){
            tvSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }
}
