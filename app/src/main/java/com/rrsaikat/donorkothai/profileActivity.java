package com.rrsaikat.donorkothai;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.rrsaikat.donorkothai.constants.FireBaseConstants;
import com.rrsaikat.donorkothai.constants.SharedPrefConstants;
import com.rrsaikat.donorkothai.model.User;
import com.rrsaikat.donorkothai.storage.SharedPreferenceManager;
import com.rrsaikat.donorkothai.util.PreferencesUtil;
import com.rrsaikat.donorkothai.util.Util;
import com.rrsaikat.donorkothai.util.location.GpsTracker;
import com.rrsaikat.donorkothai.util.location.permission.AppPermissionsUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class profileActivity extends CustomBaseActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private static int UPDATE_INTERVAL = 3000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    private static final int MY_PERMISSION_REQUEST_CODE = 12195;
    private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 10128;
    private DatePickerDialog datePickerDialog;
    private LocationRequest mLocationRequest;
    private boolean mIsAddressRequired;

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;

    GpsTracker gps;
    private SharedPreferenceManager mSharedPreferenceManager;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private AppCompatTextView tvlocWithadress;
    private AppCompatEditText fnmae,lname, email;
    private Spinner bloodGroupDropDown;
    private Button btnDob, createNow;
    private RadioButton msex;
    private RadioGroup sexGroup;
    private Double latitude=0.0,longitude=0.0;
    private User user;
    private String userID;
    public SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bloodGroupDropDown = (Spinner)findViewById(R.id.blood_group_drop_down);
        btnDob = (Button)findViewById(R.id.select_birth_date_button);
        tvlocWithadress =(AppCompatTextView)findViewById(R.id.tv_location_picker);
        fnmae = (AppCompatEditText) findViewById(R.id.et_first_name);
        lname = (AppCompatEditText) findViewById(R.id.et_last_name);
        createNow = (Button)findViewById(R.id.create_profile_button);
        email=(AppCompatEditText)findViewById(R.id.et_email);
        sexGroup = (RadioGroup)findViewById(R.id.gender_radio_group);

        getSupportActionBar().setTitle(R.string.user_profile);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initSpinner();
        fetchApproximateLocation(this);
        /*
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user = ProfileManager.getInstance(this).buildProfile(firebaseUser);
        fnmae.setText(user.getfName());
        lname.setText(user.getlName());
        email.setText(user.getEmail());
        tvlocWithadress.setText("Latitude :"+String.valueOf(user.getLatitude())+ "\n" + "Longitude :" + String.valueOf(user.getLongitude()));
        bloodGroupDropDown.setId(user.getSex());
        btnDob.setText(user.getDob());
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        */
        btnDob.setOnClickListener(this);
        tvlocWithadress.setOnClickListener(this);
        createNow.setOnClickListener(this);
    }



    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.blood_group,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupDropDown.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_birth_date_button:
                showDatePickerDialog();
                break;

            case R.id.tv_location_picker:
                fetchApproximateLocation(this);
                break;

            case R.id.create_profile_button:
                if (hasInternetConnection()) {
                    //User user = new User();
                    //attemptCreateProfile(user);
                    createProfile();
                } else {
                    showSnackBar("No internet connection");
                }
                break;
        }
    }


    private void createProfile() {
        showProgress();

        String fn = fnmae.getText().toString();
        String ln = lname.getText().toString();
        String em = email.getText().toString();
        String dob = btnDob.getText().toString();
        String bg = bloodGroupDropDown.getSelectedItem().toString();

        Double userLatitude = Double.valueOf(latitude.toString());
        Double userLongitude = Double.valueOf(longitude.toString());
        int selectedID = sexGroup.getCheckedRadioButtonId();
        msex = (RadioButton)findViewById(selectedID);
        String gender = msex.getText().toString();


        if(fn.equalsIgnoreCase("")||fn.length() < 3 || ln.equalsIgnoreCase("") || ln.length() < 3 || em.equalsIgnoreCase("")||em.length()==8||
                dob.equalsIgnoreCase("")) {

            if (fn.equalsIgnoreCase("")) {
                fnmae.setError("Please Enter first Name ");
                fnmae.requestFocus();
                hideProgress();
            } else if (fn.length() < 3) {
                fnmae.setError("At least use 3 character ");
                fnmae.requestFocus();
                hideProgress();
            } else if (ln.equalsIgnoreCase("")) {
                lname.setError("Please Enter last Name ");
                lname.requestFocus();
                hideProgress();
            } else if (ln.length() < 3) {
                lname.setError("At least use 3 character");
                lname.requestFocus();
                hideProgress();
            } else if (em.equalsIgnoreCase("") || !Util.isValidEmail(em)) {
                email.setError("Pleas Enter Valid Email ");
                email.requestFocus();
                hideProgress();
            } else if (dob.equalsIgnoreCase("")) {
                //btnDob.setError("This field is required");
                showSnackBar("Select your date of birth first");
                btnDob.requestFocus();
                hideProgress();
            }

        }
            else {
                if(latitude != 0.0 && longitude != 0.0){

                    String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.USERS).child(UserId);
                    Map userInfo = new HashMap();
                    userInfo.put("fname", fn);
                    userInfo.put("lname", ln);
                    userInfo.put("email", em);
                    userInfo.put("bloodgroup", bg);
                    userInfo.put("dateofBirth", dob);
                    userInfo.put("latitude", userLatitude);
                    userInfo.put("longitude", userLongitude);
                    userInfo.put("gender", gender);
                    if (!TextUtils.isEmpty(UserId)) {
                        Task<Void> task = mDatabase.setValue(userInfo);
                        task.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                hideProgress();
                                showSnackBar("Successfull!");
                                startActivity(new Intent(profileActivity.this, MapActivity.class));
                                finish();
                                Log.d("Profile Status", "createOrUpdateProfile, success: " + task.isSuccessful());
                                ///////
                            }
                        });


                        // mSharedPreferenceManager.put(SharedPrefConstants.IS_USER_DETAILS_ENTERED, true);
                        // mSharedPreferenceManager.put(SharedPrefConstants.USER_DETAILS, new Gson().toJson(user));
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Please select your location", Toast.LENGTH_SHORT).show();
                    hideProgress();
                }

            }
        }




        //showSnackBar(fn + " "+ ln + " "+em+" "+ dob+" "+bg+" "+ gender+ " "+userLatitude+" "+userLongitude);


    private void displayUserInfo() {
        String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.USERS).child(UserId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("fname") != null) {
                        fnmae.setText(map.get("fname").toString());
                    }

                    if (map.get("lname") != null) {
                        lname.setText(map.get("lname").toString());
                    }

                    if (map.get("email") != null) {
                        email.setText(map.get("email").toString());
                    }

                    if (map.get("bloodgroup") != null) {
                        ArrayAdapter myAdap = (ArrayAdapter) bloodGroupDropDown.getAdapter();
                        int pos = myAdap.getPosition(map.get("bloodgroup").toString());
                        bloodGroupDropDown.setSelection(pos);
                    }
                    if (map.get("dateofBirth") != null) {
                        btnDob.setText(map.get("dateofBirth").toString());
                    }

                    if (map.get("latitude") != null && map.get("longitude") != null) {
                        tvlocWithadress.setText("Latitude :"+ map.get("latitude").toString() + "\n"+ "Longitude :"+ map.get("longitude").toString());
                    }

                    if (map.get("gender") != null) {
                        if (map.get("gender").toString().equals("Male"))
                            sexGroup.check(R.id.rb_male);

                        if (map.get("gender").toString().equals("Female"))
                            sexGroup.check(R.id.rb_female);

                        if (map.get("gender").toString().equals("Unclussified"))
                            sexGroup.check(R.id.rb_unclussified);


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(profileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSION_REQUEST_CODE);
        }else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                //displayLocation();

            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
             latitude = mLastLocation.getLatitude();
             longitude = mLastLocation.getLongitude();

            tvlocWithadress.setText("Latitude : " + Double.toString(latitude) + "\n"+ "Longitude : " + Double.toString(longitude) );

        } else {
            //uLa.setText("Error! Make sure location is enabled on the device");
            Toast.makeText(getApplicationContext(), "Make sure location is enabled on the device", Toast.LENGTH_LONG).show();;
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

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(profileActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.YEAR, -18);
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                calendar = Calendar.getInstance();
                calendar.add(Calendar.YEAR, -60);
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());


                btnDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
        }, year, month, day);

        datePickerDialog.show();
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayUserInfo();

    }

    @Override
    protected void onStop() {
        super.onStop();

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
                        //displayLocation();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;

            }
        }
    }

    private void fetchApproximateLocation(profileActivity profileActivity) {
        String neverAskMessage = profileActivity.getString(R.string.msg_never_ask_sign_up);
        String rationaleMessage = profileActivity.getString(R.string.msg_sign_up_location_message);

        fetchLocation(neverAskMessage, rationaleMessage, false);
    }

    private void fetchLocation(String neverAskMessage, String rationaleMessage, boolean isCompleteAddressRequired) {
        mIsAddressRequired = isCompleteAddressRequired;
        //Permission is granted.
        if (AppPermissionsUtil.checkIfLocationPermissionIsGiven(profileActivity.this))
            setupLocation();
        else {
            // Never Ask scenario
            if (!AppPermissionsUtil.shouldShowPermissionRationaleForLocation(profileActivity.this)) {


                new AlertDialog.Builder(profileActivity.this)
                        .setTitle(R.string.msg_location_permission_title)
                        .setMessage(neverAskMessage)
                        .setPositiveButton(profileActivity.this.getString(R.string.txt_open_settings), ((dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", profileActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            profileActivity.this.startActivity(intent);
                        })).setNegativeButton(profileActivity.this.getString(R.string.cancel), ((dialog, which) -> dialog.dismiss()))
                        .create().show();

            } else if (AppPermissionsUtil.shouldShowPermissionRationaleForLocation(profileActivity.this)) {
                // Permission has been denied once.
                new AlertDialog.Builder(profileActivity.this)
                        .setTitle(R.string.msg_location_permission_title)
                        .setMessage(rationaleMessage)
                        .setPositiveButton(profileActivity.this.getString(R.string.ok), (dialog, which) -> {
                            dialog.dismiss();
                            AppPermissionsUtil.requestForLocationPermission(profileActivity.this, LOCATION_PERMISSION_REQUEST_CODE);

                        })
                        .setNegativeButton(profileActivity.this.getString(R.string.cancel), ((dialog, which) -> dialog.dismiss()))
                        .create().show();
            } else //Ask for permission
                AppPermissionsUtil.requestForLocationPermission(profileActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
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

        SettingsClient settingsClient = LocationServices.getSettingsClient(profileActivity.this);
        settingsClient.checkLocationSettings(builder.build()).
                addOnSuccessListener(locationSettingsResponse -> displayLocation())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.

                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;

                        try {
                            resolvable.startResolutionForResult(profileActivity.this, LOCATION_SETTINGS_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
