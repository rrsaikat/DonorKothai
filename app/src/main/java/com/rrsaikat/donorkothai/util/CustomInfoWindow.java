package com.rrsaikat.donorkothai.util;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.rrsaikat.donorkothai.R;
import com.rrsaikat.donorkothai.model.ReceiverDonorRequestType;
import com.rrsaikat.donorkothai.model.User;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Activity context;
    public CustomInfoWindow(Activity context) {
        this.context = context;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.marker_info_window, null);

        TextView heading = view.findViewById(R.id.heading);
        TextView name = view.findViewById(R.id.name);
        ImageView img = view.findViewById(R.id.img);
        TextView bg = view.findViewById(R.id.blood);

        heading.setText(marker.getTitle());
/*
        User infoWindowData = (User) marker.getTag();

        int imgId = context.getResources().getIdentifier(infoWindowData.getImg().toLowerCase(), "drawable", context.getPackageName());
        img.setImageResource(imgId);

        name.setText(infoWindowData.getfName()+" "+infoWindowData.getlName());
        bg.setText(infoWindowData.getBloodGroup());
*/

        ReceiverDonorRequestType info = (ReceiverDonorRequestType) marker.getTag();
        name.setText(info.getfName()+" "+info.getlName());
        bg.setText(info.getbGp());

        return view;
    }
}
