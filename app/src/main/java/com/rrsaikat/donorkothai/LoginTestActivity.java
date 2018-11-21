package com.rrsaikat.donorkothai;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;
import com.rrsaikat.donorkothai.constants.FireBaseConstants;
import com.rrsaikat.donorkothai.model.User;
import com.rrsaikat.donorkothai.util.Util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rezwan.pstu.cse12.view.CircularMorphLayout;
import timber.log.Timber;

public class LoginTestActivity extends CustomBaseActivity implements View.OnClickListener {
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;

    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private boolean mVerificationInProgress = false;
    private String mVerificationId,mPhoneNumber,myCCP;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mVerifyViews;
    private CircularMorphLayout cmLayout,cmlVerifyLayout;

    private TextView mDetailText,mStartButtonTxt,tvPhoneNumber;
    private ImageView editPhoneNumber;

    private EditText mPhoneNumberField;
    //private EditText mVerificationField;
    private ConstraintLayout layoutRegistration,layoutVerification;

    private TextView tvVerify;
    private Button mResendButton;
    //private Button mSignOutButton;
    ProgressBar progressBar, pbVerify;
    Pinview otp;
    CountryCodePicker ccp;
    CountDownTimer countdownTimer;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_test);
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null) {
            startActivity(new Intent(LoginTestActivity.this, profileActivity.class));
            finish();
        }else {

        }


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        pbVerify=(ProgressBar)findViewById(R.id.pb_verifying);
        layoutRegistration = (ConstraintLayout)findViewById(R.id.layout_regis);
        layoutVerification =(ConstraintLayout)findViewById(R.id.layout_verify);
        mDetailText = (TextView) findViewById(R.id.detail);
        tvPhoneNumber =(TextView)findViewById(R.id.tv_phone_number);
        mPhoneNumberField = (EditText) findViewById(R.id.et_phone_number);
        //mVerificationField = (EditText) findViewById(R.id.field_verification_code);
        otp = (Pinview)findViewById(R.id.pinview);
        ccp = (CountryCodePicker)findViewById(R.id.cc_country_code);
        cmLayout = (CircularMorphLayout)findViewById(R.id.cml_proceed_layout);
        cmlVerifyLayout=(CircularMorphLayout)findViewById(R.id.cml_verify_layout);
        mStartButtonTxt = (TextView)findViewById(R.id.tv_proceed);
        tvVerify = (TextView) findViewById(R.id.tv_verify);
        editPhoneNumber = (ImageView)findViewById(R.id.ib_edit_number);
        mResendButton =(Button)findViewById(R.id.bt_resend_code);



        mResendButton.setOnClickListener(this);
        mStartButtonTxt.setOnClickListener(this);
        editPhoneNumber.setOnClickListener(this);
        tvVerify.setOnClickListener(this);


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;

                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;
                setStartProgressVisibility(false);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mPhoneNumberField.setError("Invalid phone number.");
                    layoutVerification.setVisibility(View.GONE);
                    layoutRegistration.setVisibility(View.VISIBLE);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    showSnackBar(R.string.msg_sms_verification_limit_exceeded);
                    layoutVerification.setVisibility(View.GONE);
                    layoutRegistration.setVisibility(View.VISIBLE);
                }
                else {
                    showSnackBar(R.string.msg_encountered_an_unexpected_error);
                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                updateUI(STATE_CODE_SENT);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(mPhoneNumberField.getText().toString());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        if (!mVerificationInProgress){
            setStartProgressVisibility(true);

            mPhoneNumber = phoneNumber;
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks

            mVerificationInProgress = true;
        }else {
            showSnackBar("Please wait! Verification already in progress....");
        }
    }

    private void setStartProgressVisibility(boolean isVisible) {
        if (isVisible) {
            cmLayout.revealFrom(mStartButtonTxt.getWidth() / 2f,
                    mStartButtonTxt.getHeight() / 2f,
                    mStartButtonTxt.getWidth() / 2f,
                    mStartButtonTxt.getHeight() / 2f).setListener(
                    () -> {
                        mStartButtonTxt.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }).start();
        } else {
            mStartButtonTxt.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            cmLayout.reverse();
        }
    }

    private void setVerifyProgressVisibility(boolean isVisible) {
        if (isVisible) {
            cmlVerifyLayout.revealFrom(tvVerify.getWidth() / 2f,
                    tvVerify.getHeight() / 2f,
                    tvVerify.getWidth() / 2f,
                    tvVerify.getHeight() / 2f).setListener(
                    () -> {
                        tvVerify.setVisibility(View.GONE);
                        pbVerify.setVisibility(View.VISIBLE);
                    }).start();
        } else {
            tvVerify.setVisibility(View.VISIBLE);
            pbVerify.setVisibility(View.GONE);
            cmlVerifyLayout.reverse();
        }
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                             fetchUser();

                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showSnackBar("Invalid code.");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private void fetchUser() {
        String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userDetailDbReference = mFirebaseDatabase.getReference().child(FireBaseConstants.USERS).child(UserId);

        userDetailDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setVerifyProgressVisibility(false);
                User user = null;
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                }
                onSignInSuccess(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setVerifyProgressVisibility(false);
                setTitle(getString(R.string.verification));
                layoutVerification.setVisibility(View.VISIBLE);
                layoutRegistration.setVisibility(View.GONE);

                showSnackBar(R.string.msg_encountered_an_unexpected_error);
            }
        });
    }

    private void onSignInSuccess(User user) {
        if (Util.isValidUser(user) == 0) {
            //mSharedPreferenceManager.put(SharedPrefConstants.IS_USER_DETAILS_ENTERED, true);
            showSnackBar("Authentication Successful");
        } else {
            startActivity(new Intent(LoginTestActivity.this, profileActivity.class));
            finish();
        }
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mDetailText.setText(user.getUid());

            /*
            Intent intent = new Intent(PhoneLogin.this, MapView.class);
            startActivity(intent);
            finish();
            */
            //updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                mDetailText.setText("Please verify your phone first!");
                break;
            case STATE_CODE_SENT:
                //progressBar.setVisibility(View.INVISIBLE);
                setStartProgressVisibility(false);

                setTitle(getString(R.string.verification));
                layoutVerification.setVisibility(View.VISIBLE);
                layoutRegistration.setVisibility(View.GONE);
                startCountdown();

                mDetailText.setText("Code Sent");
                mDetailText.setTextColor(Color.parseColor("#43a047"));
                break;
            case STATE_VERIFY_FAILED:

                if (countdownTimer != null) {
                    countdownTimer.cancel();
                }

                setVerifyProgressVisibility(false);
                mDetailText.setText("Verification failed");
                mDetailText.setTextColor(Color.parseColor("#dd2c00"));
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case STATE_VERIFY_SUCCESS:

                mDetailText.setText("Verfication Sucessfull");
                mDetailText.setTextColor(Color.parseColor("#43a047"));
                progressBar.setVisibility(View.INVISIBLE);

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                       // mVerificationField.setText(cred.getSmsCode());
                        otp.setValue(cred.getSmsCode());
                    } else {
                        showSnackBar("Instant Validation");
                        //mVerificationField.setTextColor(Color.parseColor("#4bacb8"));
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                mDetailText.setText("Sign In Failed !");
                mDetailText.setTextColor(Color.parseColor("#dd2c00"));
                progressBar.setVisibility(View.INVISIBLE);
                setVerifyProgressVisibility(false);
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                //mStatusText.setText(R.string.signed_in);
                break;
        }

        if (user == null) {
            // Signed out
            //mPhoneNumberViews.setVisibility(View.VISIBLE);
           // mVerifyViews.setVisibility(View.VISIBLE);

            // mStatusText.setText(R.string.signed_out);
            ;
        } else {
            // Signed in
            //mPhoneNumberViews.setVisibility(View.GONE);

        }
    }

    private void startCountdown() {
        tvPhoneNumber.setText(mPhoneNumber);
        setResendButtonEnabled(false);
        countdownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override public void onTick(long millisUntilFinished) {
                setResendButtonTimerCount(millisUntilFinished / 1000);
            }

            @Override public void onFinish() {
                setResendButtonEnabled(true);
            }
        }.start();
    }


    public void setResendButtonEnabled(boolean isEnabled) {
        if (isEnabled) {
            mResendButton.setEnabled(true);
            mResendButton.setText(R.string.resend_code);
        } else {
            mResendButton.setEnabled(false);
        }
    }

    private void setResendButtonTimerCount(long secondsRemaining) {
        mResendButton.setText(
                String.format(Locale.ENGLISH, getString(R.string.resend_code_timer), secondsRemaining));

    }




    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            //mPhoneNumberField.setTextColor(Color.parseColor("#ff1744"));
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_proceed:
                    if (hasInternetConnection()) {
                    String phone = mPhoneNumberField.getText().toString();
                    myCCP = ccp.getSelectedCountryCode();
                    if (Util.isValidPhoneNumber(phone)){
                        progressBar.setVisibility(View.VISIBLE);
                        startPhoneNumberVerification(Util.getPhoneNumberWithPlus(phone, myCCP));
                    }

                    else {
                        mPhoneNumberField.setError(getText(R.string.msg_invalid_phone_number));

                    }
                }else {
                    showSnackBar(R.string.msg_no_internet_connection);
                }
            break;



            case R.id.tv_verify:
                String code = otp.getValue().toString();
                if (TextUtils.isEmpty(code)) {
                    showSnackBar("Please enter the phone code");
                    return;
                }
                setVerifyProgressVisibility(true);
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;

            case R.id.bt_resend_code:
                showSnackBar(R.string.msg_otp_has_been_sent);
                startPhoneNumberVerification(mPhoneNumber);
            break;


            case R.id.ib_edit_number:
                showEditPhoneDialog();
                break;
        }
    }

    private void showEditPhoneDialog() {
        new AlertDialog.Builder(this)
                .setMessage(
                        String.format(
                                getString(R.string.msg_currently_verifying_number),
                                otp.getValue(),mPhoneNumber))
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes,
                        (dialog, which) -> onEditPhoneNumberActionYes())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void onEditPhoneNumberActionYes() {
        mVerificationInProgress = false;
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        setTitle(getString(R.string.app_name));

        layoutVerification.setVisibility(View.GONE);
        layoutRegistration.setVisibility(View.VISIBLE);
    }

}

