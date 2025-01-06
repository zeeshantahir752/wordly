package com.yesserly.wordly.base;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.OnUserEarnedRewardListener;

public interface AdsListener {

    void loadInterstitial(String AD_UNIT);

    boolean showInterstitial(FullScreenContentCallback callback);

    void loadRewarded(String AD_UNIT);

    boolean showRewarded(OnUserEarnedRewardListener callback);
}
