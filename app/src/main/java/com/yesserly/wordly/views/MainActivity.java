package com.yesserly.wordly.views;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.ironsource.mediationsdk.IronSource;
import com.yesserly.wordly.R;
import com.yesserly.wordly.base.AdsListener;
import com.yesserly.wordly.utils.GDPR;
import com.yesserly.wordly.viewmodels.MainViewModel;
import com.yesserly.wordly.views.fragments.GameFragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements AdsListener {
    private static final String TAG = "MainActivity";

    @Inject
    GDPR gdpr;
    @Inject
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    private MainViewModel mViewModel;
    private final MutableLiveData<Boolean> loadedData = new MutableLiveData<>();

    //Ads
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;

    InterstitialAdLoadCallback interstitialCallback = new InterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            super.onAdLoaded(interstitialAd);
            mInterstitialAd = interstitialAd;
        }
    };
    RewardedAdLoadCallback rewardedCallback = new RewardedAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            super.onAdLoaded(rewardedAd);
            Log.d(TAG, "onAdLoaded");
            mRewardedAd = rewardedAd;
            loadedData.postValue(true);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            Log.d(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
            mRewardedAd = null;
            loadedData.postValue(false);
        }
    };

    /***********************************************************************************************
     * *********************************** LifeCycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set Theme
        setTheme(R.style.Theme_Wordly);
        setContentView(R.layout.activity_main);

        //Set ViewModel
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        //Init Ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        //Fetch Config
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                boolean updated = task.getResult();
                Log.d(TAG, "Config params updated: " + updated);
            } else {
                Log.d(TAG, "Fetching Failed");
            }

            //Load Files
            mViewModel.loadFiles();

            //Check GDPR
            gdpr.checkForConsent(this);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    @Override
    public void loadInterstitial(String AD_UNIT) {
        gdpr.loadInterstitialAd(this, AD_UNIT, interstitialCallback);
    }

    @Override
    public void loadRewarded(String AD_UNIT) {
        gdpr.loadRewardedAd(this, AD_UNIT, rewardedCallback);
    }

    @Override
    public boolean showInterstitial(FullScreenContentCallback callback) {
        if (mFirebaseRemoteConfig.getBoolean("ADS_ENABLED") && mInterstitialAd != null) {
            mInterstitialAd.show(this);
            mInterstitialAd.setFullScreenContentCallback(callback);
            return true;
        }
        return false;
    }

    @Override
    public boolean showRewarded(OnUserEarnedRewardListener callback) {
        if (mFirebaseRemoteConfig.getBoolean("ADS_ENABLED") && mRewardedAd != null) {
            MainActivity finalThis = this;
            runOnUiThread(() -> mRewardedAd.show(finalThis, callback));
            return true;
        }
        return false;
    }

}