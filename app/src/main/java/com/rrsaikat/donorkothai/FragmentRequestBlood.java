package com.rrsaikat.donorkothai;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rrsaikat.donorkothai.constants.FireBaseConstants;

import java.util.HashMap;
import java.util.Map;


public class FragmentRequestBlood extends DialogFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Button button;
    private AppCompatTextView tvLocationPick;
    private AppCompatEditText etPurpose;
    private String purposeOfRequest,bg,req,lati,longi;
    private Spinner reqType,bloodgroup;
    private profileActivity p;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private Double latitude,longitude;
    private String fn,ln;
    private ProgressDialog progressDialog;

    private OnFragmentInteractionListener mListener;

    public FragmentRequestBlood() {

    }

    public static FragmentRequestBlood newInstance(String param1, String param2) {
        FragmentRequestBlood fragment = new FragmentRequestBlood();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_request_blood, container, false);
        button = (Button)rootView.findViewById(R.id.submit);
        reqType = (Spinner)rootView.findViewById(R.id.request_drop_down);
        bloodgroup = (Spinner)rootView.findViewById(R.id.blood_group_drop_down);
        etPurpose = (AppCompatEditText)rootView.findViewById(R.id.et_purpose);
        tvLocationPick =(AppCompatTextView)rootView.findViewById(R.id.tv_location_picker);

        fetchName();
        buildGoogleApiClient();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPurpose.getText().toString().equalsIgnoreCase("")) {
                    etPurpose.setError("This field is important");

                }else {
                    purposeOfRequest = etPurpose.getText().toString();
                    req = reqType.getSelectedItem().toString();
                    bg = bloodgroup.getSelectedItem().toString();
                    lati = Double.valueOf(latitude).toString();
                    longi= Double.valueOf(longitude).toString();
                    onButtonPressed(purposeOfRequest, req, bg, lati, longi);

                    showProgress();
                    insertDataintoFiebase();
                }
            }
        });

        tvLocationPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Pressed", Toast.LENGTH_SHORT).show();
                 //p = new profileActivity();
                 //p.displayLocation();
                 disPlayLFragmentocation();
            }
        });


        return rootView;
    }

    private void insertDataintoFiebase() {
        // Request Type is receiver
        if (req.equals("Request Blood")) {

            String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.RECEIVER).child(UserId);
            Map userInfo = new HashMap();
            userInfo.put("fName", fn);
            userInfo.put("lName", ln);
            userInfo.put("requestType", req);
            userInfo.put("bGp", bg);
            userInfo.put("purpose", purposeOfRequest);
            userInfo.put("latitude", lati);
            userInfo.put("longitude", longi);
            userInfo.put("phone", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());



            Task<Void> task = mDatabase.setValue(userInfo);
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dismiss();
                    hideProgress();
                    Toast.makeText(getActivity(), "Submission done !", Toast.LENGTH_SHORT).show();
                    Log.d("Reciever Profile Status", "FragmentRequestDialog, success: " + task.isSuccessful());
                }
            });
        }

        else {
            // Request Type is donor
            String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.DONOR).child(UserId);
            Map userInfo = new HashMap();
            userInfo.put("fName", fn);
            userInfo.put("lName", ln);
            userInfo.put("reqT", req);
            userInfo.put("bGp", bg);
            userInfo.put("purpose", purposeOfRequest);
            userInfo.put("latitude", lati);
            userInfo.put("longitude", longi);
            userInfo.put("phone", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

            Task<Void> task = mDatabase.setValue(userInfo);
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dismiss();
                    hideProgress();
                    Toast.makeText(getActivity(), "Submission done !", Toast.LENGTH_SHORT).show();
                    Log.d("Donor Profile Status", "FragmentRequestDialog, success: " + task.isSuccessful());
                }
            });
        }
    }

    private void fetchName() {
        String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.USERS).child(UserId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("fname") != null) {
                       fn = map.get("fname").toString();
                    }

                    if (map.get("lname") != null) {
                        ln = map.get("lname").toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void disPlayLFragmentocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            tvLocationPick.setText("Latitude : " + Double.toString(latitude) + "\n"+ "Longitude : " + Double.toString(longitude) );

        } else {
            //uLa.setText("Error! Make sure location is enabled on the device");
            Toast.makeText(getActivity(), "Make sure location is enabled on the device", Toast.LENGTH_LONG).show();;
        }
    }


    public void onButtonPressed(String purpose, String req, String bg, String lat, String lon ) {
        if (mListener != null) {
           mListener.onFragmentInteraction(purpose, req ,bg, lat, lon);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        window.setLayout((int) (size.x * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        disPlayLFragmentocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String purpose, String req, String bg, String lat, String lon);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void showProgress() {
        showProgress(R.string.loading);
    }

    public void showProgress(int message) {
        hideProgress();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(message));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
