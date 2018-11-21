package com.rrsaikat.donorkothai;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rrsaikat.donorkothai.constants.FireBaseConstants;
import com.rrsaikat.donorkothai.constants.SharedPrefConstants;
import com.rrsaikat.donorkothai.injection.Injection;
import com.rrsaikat.donorkothai.model.User;
import com.rrsaikat.donorkothai.util.Util;


public class SplashScreen extends CustomBaseActivity {

    private static final Long SPLASH_DELAY = 2500L;
    private Handler mDelayHandler = new Handler();
    private TextView mAppNameView;
    private TextView mAppVersionView;
    private ImageView mAppLogoView;
    private SharedPreferences pref;
    private   Intent intent;


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (hasInternetConnection()) {
                showProgress();
                if (!isFinishing()) {
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        intent = new Intent(SplashScreen.this, LoginTestActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        CheckprofileCreatedOrNot();
                    }

                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            }else {
                    exitWarningDialog();
          }
        }
    };

    private void CheckprofileCreatedOrNot() {
        String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userDetailDbReference = mFirebaseDatabase.getReference().child(FireBaseConstants.USERS).child(UserId);
        userDetailDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = null;
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                        hideProgress();
                        intent = new Intent(SplashScreen.this , MapActivity.class);
                        startActivity(intent);
                        finish();
                }else {
                    hideProgress();
                    intent =new Intent(SplashScreen.this, profileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgress();
                showSnackBar("Failed to connect with server");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        mAppVersionView.setText(AppVersionUtil.getAppVersion(this));

        mDelayHandler.postDelayed(runnable, SPLASH_DELAY);
        startAnimation();


    }



    private void initViews() {
        setContentView(R.layout.activity_splash_screen);

        mAppLogoView = findViewById(R.id.iv_app_icon);
        mAppNameView = findViewById(R.id.tv_app_name);
        mAppVersionView = findViewById(R.id.tv_app_version);
    }

    private void startAnimation() {
        mAppNameView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mAppNameView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        AnimatorSet mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.playTogether(ObjectAnimator.ofFloat(mAppNameView, "alpha", 0, 1, 1, 1),
                                ObjectAnimator.ofFloat(mAppNameView, "scaleX", 0.3f, 1.05f, 0.9f, 1),
                                ObjectAnimator.ofFloat(mAppNameView, "scaleY", 0.3f, 1.05f, 0.9f, 1));
                        mAnimatorSet.setDuration(1500);
                        mAnimatorSet.start();
                    }
                });
        mAppVersionView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mAppVersionView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        AnimatorSet mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.playTogether(ObjectAnimator.ofFloat(mAppVersionView, "alpha", 0, 1, 1, 1),
                                ObjectAnimator.ofFloat(mAppVersionView, "scaleX", 0.3f, 1.05f, 0.9f, 1),
                                ObjectAnimator.ofFloat(mAppVersionView, "scaleY", 0.3f, 1.05f, 0.9f, 1));
                        mAnimatorSet.setDuration(1500);
                        mAnimatorSet.start();
                    }
                });
        mAppLogoView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mAppLogoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        AnimatorSet mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.playTogether(ObjectAnimator.ofFloat(mAppLogoView, "alpha", 0, 1, 1, 1),
                                ObjectAnimator.ofFloat(mAppLogoView, "scaleX", 0.3f, 1.05f, 0.9f, 1),
                                ObjectAnimator.ofFloat(mAppLogoView, "scaleY", 0.3f, 1.05f, 0.9f, 1));
                        mAnimatorSet.setDuration(1500);
                        mAnimatorSet.start();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDelayHandler.removeCallbacks(runnable);
    }
}
