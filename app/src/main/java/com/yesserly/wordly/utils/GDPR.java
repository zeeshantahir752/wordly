package com.yesserly.wordly.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yesserly.wordly.R;
import com.yesserly.wordly.views.MainActivity;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

public class GDPR {
    private static final String TAG = "GDPR";

    private ConsentForm form;
    private final FirebaseRemoteConfig remoteConfig;
    private final SharedPreferencesHelper mSharedPrefs;

    @Inject
    public GDPR(FirebaseRemoteConfig remoteConfig, SharedPreferencesHelper mSharedPrefs) {
        this.remoteConfig = remoteConfig;
        this.mSharedPrefs = mSharedPrefs;
    }

    /**
     * GDPR CODE
     */
    public void checkForConsent(Context mContext) {
        Log.d(TAG, "checkForConsent: Checking For Consent");
        ConsentInformation consentInformation = ConsentInformation.getInstance(mContext);
        String[] publisherIds = {remoteConfig.getString("PUBLISHER_ID")};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d(TAG, "onConsentInfoUpdated: Successful");
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Log.d(TAG, "Showing Personalized ads");
                        mSharedPrefs.setAdPersonalized(true);
                        break;
                    case NON_PERSONALIZED:
                        Log.d(TAG, "Showing Non-Personalized ads");
                        mSharedPrefs.setAdPersonalized(false);
                        break;
                    case UNKNOWN:
                        Log.d(TAG, "Requesting Consent");
                        if (ConsentInformation.getInstance(mContext)
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent(mContext);
                        } else {
                            mSharedPrefs.setAdPersonalized(true);
                        }
                        break;
                    default:
                        Log.d(TAG, "onConsentInfoUpdated: Nothing");
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                Log.d(TAG, "onFailedToUpdateConsentInfo: Failed to update");
                // User's consent status failed to update.
            }
        });
    }

    private void requestConsent(Context mContext) {
        Log.d(TAG, "requestConsent: Requesting Consent");
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(remoteConfig.getString("PRIVACY_POLICY_URL"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error_image.
        }
        form = new ConsentForm.Builder(mContext, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "Requesting Consent: onConsentFormLoaded");
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "Requesting Consent: onConsentFormOpened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d(TAG, "Requesting Consent: onConsentFormClosed");
                        if (userPrefersAdFree) {
                            // Buy or Subscribe
                            Log.d(TAG, "Requesting Consent: User prefers AdFree");
                        } else {
                            Log.d(TAG, "Requesting Consent: Requesting consent again");
                            switch (consentStatus) {
                                case PERSONALIZED:
                                    mSharedPrefs.setAdPersonalized(true);
                                    break;
                                case NON_PERSONALIZED:
                                    break;
                                case UNKNOWN:
                                    mSharedPrefs.setAdPersonalized(false);
                                    break;
                            }
                        }
                        // Consent form was closed.
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d(TAG, "Requesting Consent: onConsentFormError. Error - " + errorDescription);
                        // Consent form error_image.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    public void loadAdBanner(AdView ad) {
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if (remoteConfig.getBoolean("GDPR_ENABLED") && !mSharedPrefs.isAdPersonalized())
            adRequest.addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle());
        ad.loadAd(adRequest.build());
    }

    public void loadInterstitialAd(Context mContext, String AD_UNIT, InterstitialAdLoadCallback callback) {
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if (remoteConfig.getBoolean("GDPR_ENABLED") && !mSharedPrefs.isAdPersonalized())
            adRequest.addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle());
        InterstitialAd.load(mContext, AD_UNIT, adRequest.build(), callback);
    }

    public void loadRewardedAd(Context mContext, String AD_UNIT, RewardedAdLoadCallback callback) {
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if (remoteConfig.getBoolean("GDPR_ENABLED") && !mSharedPrefs.isAdPersonalized())
            adRequest.addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle());
        RewardedAd.load(mContext, AD_UNIT, adRequest.build(), callback);
    }

    private Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        return extras;
    }

    private void showForm() {
        if (form == null) {
            Log.d(TAG, "Consent form is null");
        }
        if (form != null) {
            Log.d(TAG, "Showing consent form");
            form.show();
        } else {
            Log.d(TAG, "Not Showing consent form");
        }
    }
}
