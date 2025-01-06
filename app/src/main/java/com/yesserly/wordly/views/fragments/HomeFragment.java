package com.yesserly.wordly.views.fragments;


import static com.yesserly.wordly.utils.Config.LANGUAGES;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yesserly.wordly.R;
import com.yesserly.wordly.base.AdsListener;
import com.yesserly.wordly.databinding.FragmentHomeBinding;
import com.yesserly.wordly.models.pojo.Mode;
import com.yesserly.wordly.utils.game.AlarmReceiver;
import com.yesserly.wordly.utils.GDPR;
import com.yesserly.wordly.viewmodels.HomeViewModel;
import com.yesserly.wordly.views.MainActivity;
import com.yesserly.wordly.views.dialogs.NewModeDialog;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements NewModeDialog.ModeCreationListener{
    private static final String TAG = "HomeFragment";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private HomeViewModel mViewModel;
    private AdsListener listener;
    private FragmentHomeBinding mBinding;
    @Inject
    GDPR gdpr;
    @Inject
    FirebaseRemoteConfig remoteConfig;

    /***********************************************************************************************
     * *********************************** LifeCycle
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Set ViewBinding
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set ViewModel
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        //Prepare Ads
        if (remoteConfig.getBoolean("ADS_ENABLED") && remoteConfig.getBoolean("INTERSTITIAL_START_AD_ENABLED"))
            listener.loadInterstitial(getString(R.string.INTERSTITIAL_START_AD_ID));
        if (remoteConfig.getBoolean("ADS_ENABLED") && remoteConfig.getBoolean("BANNER_HOME_AD_ENABLED")) {
            gdpr.loadAdBanner(mBinding.adView);
        } else mBinding.adView.setVisibility(View.GONE);

        //Init UI
        if (mViewModel.customExists())
            mBinding.continuePlay.setVisibility(View.VISIBLE);
        else mBinding.continuePlay.setVisibility(View.GONE);

        //Check if First Run
        if (!remoteConfig.getBoolean("INSTRUCTIONS_ENABLED") && mViewModel.isFirstRun()) {
            Bundle bundle = new Bundle();
            bundle.putString("page", "INSTRUCTIONS");
            mViewModel.setFirstRun();
            NavHostFragment.findNavController(this).navigate(R.id.home_web, bundle);
        }

        //Listeners
        mBinding.normalPlay.setOnClickListener(v -> goMode("NORMAL"));
        mBinding.continuePlay.setOnClickListener(v -> goMode("CUSTOM"));
        mBinding.newPlay.setOnClickListener(v -> newMode());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;//Clear mBinding
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    public void goMode(String mode) {
        final HomeFragment finalThis = this;
        boolean showed = listener.showInterstitial(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                Bundle bundle = new Bundle();
                bundle.putString("mode", mode);
                NavHostFragment.findNavController(finalThis).navigate(R.id.go_game, bundle);
            }
        });
        if (!showed){
            Bundle bundle = new Bundle();
            bundle.putString("mode", mode);
            NavHostFragment.findNavController(finalThis).navigate(R.id.go_game, bundle);
        }
    }

    public void newMode() {
        NewModeDialog dialog = new NewModeDialog(LANGUAGES.clone());
        dialog.show(getChildFragmentManager(), "NewModeDialog");
    }

    @Override
    public void createNewMode(Mode mode) {
        //Remove Existing AlarmManager
        Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) requireActivity()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);

        //Create Custom
        mViewModel.getCustomsCleared().observe(getViewLifecycleOwner(), b -> {
            mViewModel.setMode(mode);
            //Go Game
            goMode("CUSTOM");
        });
        mViewModel.clearOldCustom();
    }
}