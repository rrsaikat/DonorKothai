package com.rrsaikat.donorkothai;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.rrsaikat.donorkothai.constants.FireBaseConstants;
import com.rrsaikat.donorkothai.model.ReceiverDonorRequestType;
import com.rrsaikat.donorkothai.storage.SharedPreferenceManager;
import com.rrsaikat.donorkothai.util.CustomInfoWindow;
import com.rrsaikat.donorkothai.util.location.permission.AppPermissionsUtil;
import com.rrsaikat.donorkothai.view.RadarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener
        , FragmentRequestBlood.OnFragmentInteractionListener {

    private GoogleMap mMap;
    FirebaseAuth mAuth;
    private static int UPDATE_INTERVAL = 10000;
    private static int FASTEST_INTERVAL = 10000;
    private static int DISPLACEMENT = 10;
    private static final int MY_PERMISSION_REQUEST_CODE = 12195;
    private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 10128;
    private LocationRequest mLocationRequest;
    private boolean mIsAddressRequired;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    Location mLastLocation,loc1,loc2;
    GoogleApiClient mGoogleApiClient;
    private SharedPreferenceManager mSharedPreferenceManager;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;


    FloatingActionButton fab, fabAdd;
    private ArrayList<Marker> markers;
    private ArrayList<Marker> donorMarkers;
    private ArrayList<Marker> receiverMarkers;
    private ArrayList<LatLng> arrayLatLng;
    private LinearLayout mDonorSheet, mReceiver;
    private BottomSheetBehavior<LinearLayout> donorBehavior;
    private BottomSheetBehavior<LinearLayout> receiverBehaviour;
    private ProgressBar pbLoader;
    private RadioGroup radioGroup;
    RadarView radarView;


    Marker marker, matchedDonorMarker;
    private Circle circle;
    Polyline polyline;
    private PulsatorLayout pulsatorLayout;
    private LatLng latLng;
    private Double mlat, mlon;
    private ProgressDialog progressDialog;
    private String matchedID, curentUserID;
    List<String> latitudeList = new ArrayList<String>();
    List<String> longitudeList = new ArrayList<String>();
    List<Float> distList = new ArrayList<Float>();
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        markers = new ArrayList<>();
        donorMarkers = new ArrayList<>();
        receiverMarkers = new ArrayList<>();
        arrayLatLng = new ArrayList<>();


        mDonorSheet = findViewById(R.id.donor_sheet);
        mReceiver = findViewById(R.id.receiver_sheet);
        pbLoader = (ProgressBar) findViewById(R.id.pb);
        pulsatorLayout = (PulsatorLayout) findViewById(R.id.pulse);
        donorBehavior = BottomSheetBehavior.from(mDonorSheet);
        receiverBehaviour = BottomSheetBehavior.from(mReceiver);
        setReceiverBehaviorBottomSheetCallBack();
        setDonorBehaviorBottomSheetCallBack();
        receiverBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        donorBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pbLoader.setVisibility(View.VISIBLE);


        pulsatorLayout.start();
        addListenerOnRadioButton();
        addListenerOnRadioButtonTwo();
        fabLocationClick();
        fabAddClick();

        (mDonorSheet.findViewById(R.id.direction)).setOnClickListener(view -> {
            String lat = ((TextView) mDonorSheet.findViewById(R.id.lat)).getText().toString();
            String lon = ((TextView) mDonorSheet.findViewById(R.id.lon)).getText().toString();
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        });

        (mReceiver.findViewById(R.id.direction)).setOnClickListener(view -> {
            String lat = ((TextView) mDonorSheet.findViewById(R.id.lat)).getText().toString();
            String lon = ((TextView) mDonorSheet.findViewById(R.id.lon)).getText().toString();
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        mDonorSheet.findViewById(R.id.call).setOnClickListener(v -> {
            // ReceiverDonorRequestType phone = (ReceiverDonorRequestType) v.getTag();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent.setData(Uri.parse("tel:" + ((TextView) mDonorSheet.findViewById(R.id.phone)).getText().toString())));

        });

        mReceiver.findViewById(R.id.call).setOnClickListener(v -> {
            // ReceiverDonorRequestType phone = (ReceiverDonorRequestType) v.getTag();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent.setData(Uri.parse("tel:" + ((TextView) mReceiver.findViewById(R.id.phone)).getText().toString())));


        });

        fetchApproximateLocation(this);

    }

    private void addListenerOnRadioButtonTwo() {
        final RadioGroup radioGroup = findViewById(R.id.radio_group_two);
        radioGroup.setOnCheckedChangeListener((group, checkedId) ->
        {
            View radiobtn = radioGroup.findViewById(checkedId);
            int index = radioGroup.indexOfChild(radiobtn);
            switch (index) {
                case 0://button donor
                    //showSnackBar(radioValue);
                    updateCamera();
                    if (donorMarkers.size() != 0) {
                        donorMarkers = sortDonorListbyDistance(donorMarkers, latLng);
                        int dist = (int) getDistanceBetweenDonorPoints(donorMarkers.get(0).getPosition(), latLng);
                        if (dist < 20000) //20km =20000m
                        {
                            donorMarkers.get(0).showInfoWindow();
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(donorMarkers.get(0).getPosition()).zoom(19f).tilt(70).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 6000, null);
                            Toast.makeText(getApplicationContext(), donorMarkers.get(0).getTitle() + " " + dist + "m", Toast.LENGTH_SHORT).show();

                        } else {
                            showSnackBar("No nearby donor founds in 20km");
                        }
                    } else {
                        showSnackBar("Please try again after some moments!");
                    }
                    break;

                case 1://button receiver
                    updateCamera();
                    if (receiverMarkers.size() != 0) {
                        receiverMarkers = sortReceiverListbyDistance(receiverMarkers, latLng);
                        int dist = (int) getDistanceBetweenReceiverPoints(receiverMarkers.get(0).getPosition(), latLng);
                        if (dist < 20000) //20km =20000m
                        {
                            receiverMarkers.get(0).showInfoWindow();
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(receiverMarkers.get(0).getPosition()).zoom(19f).tilt(70).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 6000, null);
                            Toast.makeText(getApplicationContext(), receiverMarkers.get(0).getTitle() + " " + dist + "m", Toast.LENGTH_SHORT).show();

                        } else {
                            showSnackBar("No nearby receiver founds in 20km");
                        }
                    } else {
                        showSnackBar("Please try again after some moments!");
                    }
                    break;
            }
        });
    }


    private void addListenerOnRadioButton() {
        radioGroup = findViewById(R.id.radio_blood_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View radiobtn = radioGroup.findViewById(checkedId);
            int index = radioGroup.indexOfChild(radiobtn);

            switch (index) {
                case 0://first button O+

                    updateCamera();

                    int id = radioGroup.getCheckedRadioButtonId();
                    RadioButton rb = findViewById(id);
                    String getBgrp = rb.getText().toString();

                    String curentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.DONOR);
                    mref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                arrayLatLng.clear();
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    if (!data.getKey().equals(curentUserID)){
                                        Map<String, Object> map = (Map<String, Object>) data.getValue();
                                        if (map.get("bGp").equals(getBgrp)) {
                                            // matchedID = data.getKey();
                                            mlat = Double.valueOf(map.get("latitude").toString());
                                            mlon = Double.valueOf(map.get("longitude").toString());
                                            //Toast.makeText(getApplicationContext(), "" + mlat + "," + mlon, Toast.LENGTH_LONG).show();
                                            //showSnackBar(matchedID);
                                            //Toast.makeText(getApplicationContext(), "Result:" + matchedID , Toast.LENGTH_LONG).show();
/*
                                                    //put this user value into "donors" ->uid-> "searchedByUsers"

                                                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.DONOR).child(matchedID).child("searchedByUsers");
                                                String curentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                HashMap hmap = new HashMap();
                                                hmap.put("userRequestId", curentUserID);
                                                hmap.put("userRequestLat", latLng.latitude);
                                                hmap.put("userRequestLng", latLng.longitude);
                                                driverRef.updateChildren(hmap);

                                                    //
 */

                                            LatLng donorlatlng = new LatLng(mlat, mlon);
                                            arrayLatLng.add(donorlatlng);
                                            //Toast.makeText(getApplicationContext(), ""+listLatLng, Toast.LENGTH_LONG).show();
/*
                                            loc1 = new Location("");
                                            loc1.setLatitude(latLng.latitude);
                                            loc1.setLongitude(latLng.longitude);


                                            loc2 = new Location("");
                                            loc2.setLatitude(donorlatlng.latitude);
                                            loc2.setLongitude(donorlatlng.longitude);

                                            float distance = (float)loc1.distanceTo(loc2);
                                            distList.add(distance);
*/
                                        }
                                    }else {
                                        //showSnackBar("No O+ donor is found!");
                                    }
                                }
////////////////////////////////////////////////////////
                                Toast.makeText(getApplicationContext(), ""+arrayLatLng, Toast.LENGTH_LONG).show();
                                /*
                                int minDistIndex = (int) distList.indexOf(Collections.min(distList));
                                Toast.makeText(getApplicationContext(), ""+distList.get(minDistIndex), Toast.LENGTH_LONG).show();
                                if (distList.get(minDistIndex) < 100) {
                                    showSnackBar("Donor's here");
                                } else {
                                    showSnackBar("Donor found at: " + String.valueOf((distList.get(minDistIndex))) + "m");
                                }
                                */
                                if (arrayLatLng.size() != 0) {
                                    arrayLatLng = sortLatLngListbyDistance(arrayLatLng, latLng);
                                    ArrayList <LatLng> points = new ArrayList<>();
                                    points.add(arrayLatLng.get(0));
                                    points.add(latLng);
                                    PolylineOptions p = new PolylineOptions();
                                    p.width(7*1);
                                    p.geodesic(true);
                                    p.color(Color.RED);
                                    p.addAll(points);
                                    polyline = mMap.addPolyline(p);
                                    polyline.setGeodesic(true);

                                    int dist = (int) getDistanceBetweenDonorPoints(arrayLatLng.get(0), latLng);
                                    if (dist < 200) //200m
                                    {
                                        showSnackBar("Donor's here");
                                        CameraPosition cameraPosition = new CameraPosition.Builder().target(arrayLatLng.get(0)).zoom(19f).tilt(70).build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 6000, null);

                                    } else {
                                        showSnackBar("Donor found at: " + String.valueOf(dist) + "m");
                                        CameraPosition cameraPosition = new CameraPosition.Builder().target(arrayLatLng.get(0)).zoom(19f).tilt(70).build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 6000, null);
                                    }
                                }
////////////////////////////////////////////////////////
                            }else {
                                showSnackBar("No donors are listed yet!");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    break;

                case 1: //O-
                    break;

                case 2: // A+
                    break;

                case 3: // A-
                    break;

                case 4: // B+
                    break;

                case 5: // B-
                    break;

                case 6: // AB+
                    break;

                case 7: //AB-
                    Toast.makeText(getApplicationContext(), "Selected button number" + index, Toast.LENGTH_SHORT).show();
                    break;

            }
        });


    }

    private ArrayList<LatLng> sortLatLngListbyDistance(ArrayList<LatLng> arrayLatLng,final LatLng latLng) {
        Collections.sort(arrayLatLng, new Comparator<LatLng>() {
            @Override
            public int compare(LatLng l1, LatLng l2) {
                if (getDistanceBetweenDonorPoints(l1, latLng) < getDistanceBetweenDonorPoints(l2, latLng)){
                    return -1;
                }else {
                    return 1;
                }
            }
        });

        return arrayLatLng;
    }

    private ArrayList<Marker> sortDonorListbyDistance(ArrayList<Marker> donorMarkers, final LatLng latLng) {
        Collections.sort(donorMarkers, new Comparator<Marker>() {
            @Override
            public int compare(Marker m1, Marker m2) {
                if (getDistanceBetweenDonorPoints(m1.getPosition(), latLng) < getDistanceBetweenDonorPoints(m2.getPosition(), latLng)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return donorMarkers;

    }

    private static double getDistanceBetweenDonorPoints(LatLng position1, LatLng position2) {
        double results = SphericalUtil.computeDistanceBetween(position1, position2);
        return results;
    }

    private static double getDistanceBetweenReceiverPoints(LatLng position1, LatLng position2) {
        double results = SphericalUtil.computeDistanceBetween(position1, position2);
        return results;
    }

    private ArrayList<Marker> sortReceiverListbyDistance(ArrayList<Marker> receiverMarkers, LatLng latLng) {
        Collections.sort(receiverMarkers, new Comparator<Marker>() {
            @Override
            public int compare(Marker m1, Marker m2) {
                if (getDistanceBetweenReceiverPoints(m1.getPosition(), latLng) < getDistanceBetweenReceiverPoints(m2.getPosition(), latLng)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return receiverMarkers;

    }

    private void showDonors() {
        removeOldDonorMarkers(donorMarkers);
        DatabaseReference mDonorDetailsDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.DONOR);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ReceiverDonorRequestType requestType = snapshot.getValue(ReceiverDonorRequestType.class);
                            LatLng latLng = new LatLng(Double.valueOf(requestType.getLatitude()), Double.valueOf(requestType.getLongitude()));
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                    .title(String.format("%s (%s)", getString(R.string.donor), requestType.getbGp()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.redpin)));
                            donorMarkers.add(marker);
                            CustomInfoWindow customInfoWindow = new CustomInfoWindow(MapActivity.this);
                            mMap.setInfoWindowAdapter(customInfoWindow);

                            marker.setTag(requestType);
                            //marker.showInfoWindow();


                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                pbLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pbLoader.setVisibility(View.GONE);
            }
        };

        mDonorDetailsDatabaseRef.addListenerForSingleValueEvent(listener);
        mDonorDetailsDatabaseRef.push().setValue(marker);
    }

    private void showRecievers() {
        removeOldReceiverMarkers(receiverMarkers);
        DatabaseReference mDonorDetailsDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.RECEIVER);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ReceiverDonorRequestType requestType = snapshot.getValue(ReceiverDonorRequestType.class);
                            LatLng latLng = new LatLng(Double.valueOf(requestType.getLatitude()), Double.valueOf(requestType.getLongitude()));
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                    .title(String.format("%s (%s)", getString(R.string.blood_request), requestType.getbGp()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.greenpin)));
                            receiverMarkers.add(marker);
                            CustomInfoWindow customInfoWindow = new CustomInfoWindow(MapActivity.this);
                            mMap.setInfoWindowAdapter(customInfoWindow);

                            marker.setTag(requestType);
                            //marker.showInfoWindow();


                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pbLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pbLoader.setVisibility(View.GONE);
            }
        };

        mDonorDetailsDatabaseRef.addListenerForSingleValueEvent(listener);
        mDonorDetailsDatabaseRef.push().setValue(marker);
    }

    private void setDonorBehaviorBottomSheetCallBack() {
        donorBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void setReceiverBehaviorBottomSheetCallBack() {
        receiverBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    public void toggleBottomSheet(BottomSheetBehavior<LinearLayout> sheetBehavior, BottomSheetBehavior<LinearLayout> sheetBehavior2) {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        sheetBehavior2.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setTrafficEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        showDonors();
        showRecievers();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        removeOldMarkers(markers);
        removeOldDonorMarkers(donorMarkers);
        removeOldReceiverMarkers(receiverMarkers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    //String currentUserId = firebaseUser.getUid();
                    mAuth.getInstance().signOut();
                    startActivity(new Intent(MapActivity.this, LoginTestActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show();
                }

                break;


            case R.id.action_my_profile:
                startActivity(new Intent(MapActivity.this, profileActivity.class));
                finish();

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
                startLocationUpdates();
                updateCamera();

            }

    private void handleNewLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            removeOldMarkers(markers);

            double cLat = mLastLocation.getLatitude();
            double cLon = mLastLocation.getLongitude();

            latLng = new LatLng(cLat, cLon);
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(getString(R.string.you_are_here))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_loc)));

            ReceiverDonorRequestType info = new ReceiverDonorRequestType();
            info.setfName("");
            info.setlName("");
            info.setbGp("");
            CustomInfoWindow customInfoWindow = new CustomInfoWindow(MapActivity.this);
            mMap.setInfoWindowAdapter(customInfoWindow);

            marker.setTag(info);
            markers.add(marker);
            marker.showInfoWindow();
        }
    }

    @Override
            public void onConnectionSuspended(int i) {
                mGoogleApiClient.connect();
            }

            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                if (connectionResult.hasResolution()){
                    try{
                        connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }else {
                    Log.i("MapActivity", "Location services connection failed with code"+ connectionResult.getErrorCode());
                }
            }

            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
                if (mLastLocation != null) {
                    //mMap.clear();
                    //hideProgress();
                    showDonors();
                    showRecievers();
                }
            }

            @Override
            public boolean onMarkerClick(Marker marker) {
                        if (marker.getTitle() == null || marker.getTag() == null || !(marker.getTag() instanceof ReceiverDonorRequestType)) {
                            return false;
                        }
                        ReceiverDonorRequestType mReceiverDonorRequestType = (ReceiverDonorRequestType) marker.getTag();
                        if (marker.getTitle().contains(getString(R.string.donor))) {
                            ((TextView) mDonorSheet.findViewById(R.id.name)).setText(mReceiverDonorRequestType.getfName() + " " + mReceiverDonorRequestType.getlName());
                            ((TextView) mDonorSheet.findViewById(R.id.blood_group)).setText(mReceiverDonorRequestType.getbGp());
                            ((TextView) mDonorSheet.findViewById(R.id.phone)).setText(mReceiverDonorRequestType.getPhone());
                            ((TextView) mDonorSheet.findViewById(R.id.purpose)).setText(mReceiverDonorRequestType.getPurpose());
                            ((TextView) mDonorSheet.findViewById(R.id.lat)).setText(mReceiverDonorRequestType.getLatitude());
                            ((TextView) mDonorSheet.findViewById(R.id.lon)).setText(mReceiverDonorRequestType.getLongitude());
                            (mDonorSheet.findViewById(R.id.phone)).setTag(mReceiverDonorRequestType.getPhone());
                            toggleBottomSheet(donorBehavior, receiverBehaviour);
                        } else if (marker.getTitle().contains(getString(R.string.blood_request))) {
                            ((TextView) mReceiver.findViewById(R.id.name)).setText(mReceiverDonorRequestType.getfName() + " " + mReceiverDonorRequestType.getlName());
                            ((TextView) mReceiver.findViewById(R.id.blood_group)).setText(mReceiverDonorRequestType.getbGp());
                            ((TextView) mReceiver.findViewById(R.id.phone)).setText(mReceiverDonorRequestType.getPhone());
                            ((TextView) mReceiver.findViewById(R.id.purpose)).setText(mReceiverDonorRequestType.getPurpose());
                            ((TextView) mReceiver.findViewById(R.id.lat)).setText(mReceiverDonorRequestType.getLatitude());
                            ((TextView) mReceiver.findViewById(R.id.lon)).setText(mReceiverDonorRequestType.getLongitude());
                            (mReceiver.findViewById(R.id.phone)).setTag(mReceiverDonorRequestType.getPhone());
                            toggleBottomSheet(receiverBehaviour, donorBehavior);
                        }
                        return false;
                    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case MY_PERMISSION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;

            }
        }
    }

    private void fabAddClick() {
        fabAdd = (FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentRequestBlood requestDialogFragment = new FragmentRequestBlood();
                requestDialogFragment.show(fragmentManager, "request_dialog");
            }
        });
    }

    private void fabLocationClick() {
        ((FloatingActionButton)findViewById(R.id.fab_current_location))
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateCamera();
            }
        });

    }

    synchronized public void removeOldMarkers(ArrayList<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    synchronized public void removeOldDonorMarkers(ArrayList<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    synchronized public void removeOldReceiverMarkers(ArrayList<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    private void updateCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            removeOldMarkers(markers);
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            latLng = new LatLng(latitude,longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(getString(R.string.you_are_here))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_loc)));

            ReceiverDonorRequestType info = new ReceiverDonorRequestType();
            info.setfName("");
            info.setlName("");
            info.setbGp("");
            CustomInfoWindow customInfoWindow = new CustomInfoWindow(MapActivity.this);
            mMap.setInfoWindowAdapter(customInfoWindow);

            marker.setTag(info);
            markers.add(marker);
            marker.showInfoWindow();

            if (circle != null){
                circle.remove();
            }

            if(polyline != null){
                polyline.remove();
            }
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(19f).tilt(70).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),3000, null);

            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.radius(100);
            circleOptions.fillColor(Color.TRANSPARENT);
            circleOptions.strokeColor(Color.RED);
            circleOptions.strokeWidth(6);
            //circleOptions.getStrokeColor(R.color.md_amber_50);
            circle = mMap.addCircle(circleOptions);

        }
    }

    private void fetchApproximateLocation(MapActivity mapActivity) {
        String neverAskMessage = mapActivity.getString(R.string.msg_never_ask_sign_up);
        String rationaleMessage = mapActivity.getString(R.string.msg_sign_up_location_message);

        fetchLocation(neverAskMessage, rationaleMessage, false);
    }

    private void fetchLocation(String neverAskMessage, String rationaleMessage, boolean isCompleteAddressRequired) {
        mIsAddressRequired = isCompleteAddressRequired;
        //Permission is granted.
        if (AppPermissionsUtil.checkIfLocationPermissionIsGiven(this))
            setupLocation();
        else {
            // Never Ask scenario
            if (!AppPermissionsUtil.shouldShowPermissionRationaleForLocation(this)) {


                new AlertDialog.Builder(this)
                        .setTitle(R.string.msg_location_permission_title)
                        .setMessage(neverAskMessage)
                        .setPositiveButton(MapActivity.this.getString(R.string.txt_open_settings), ((dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", MapActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            MapActivity.this.startActivity(intent);
                        })).setNegativeButton(MapActivity.this.getString(R.string.cancel), ((dialog, which) -> dialog.dismiss()))
                        .create().show();

            } else if (AppPermissionsUtil.shouldShowPermissionRationaleForLocation(MapActivity.this)) {
                // Permission has been denied once.
                new AlertDialog.Builder(MapActivity.this)
                        .setTitle(R.string.msg_location_permission_title)
                        .setMessage(rationaleMessage)
                        .setPositiveButton(MapActivity.this.getString(R.string.ok), (dialog, which) -> {
                            dialog.dismiss();
                            AppPermissionsUtil.requestForLocationPermission(MapActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                            updateCamera();

                        })
                        .setNegativeButton(MapActivity.this.getString(R.string.cancel), ((dialog, which) -> dialog.dismiss()))
                        .create().show();
            } else //Ask for permission
                AppPermissionsUtil.requestForLocationPermission(MapActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
        }


    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build()).
                addOnSuccessListener(locationSettingsResponse -> updateCamera())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.

                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;

                        try {
                            resolvable.startResolutionForResult(MapActivity.this, LOCATION_SETTINGS_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

    }


    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSION_REQUEST_CODE);
        }else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();

            }
        }
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RESOLUTION_REQUEST).show();
            else {
                Toast.makeText(this,"This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }


    private void connect() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void showProgress(String message) {
        hideProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }


    @Override
    public void onFragmentInteraction(String purpose, String req, String bg, String lat, String lon) {
        radioGroup.clearCheck();
        showDonors();
        showRecievers();
        LatLng latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(19f).tilt(70).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),6000, null);

    }
}
