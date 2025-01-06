package com.yesserly.wordly.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yesserly.wordly.databinding.FragmentWebBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yesserly.wordly.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WebFragment extends Fragment {
    private static final String TAG = "WebFragment";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private FragmentWebBinding mBinding;
    private String page;
    @Inject
    FirebaseRemoteConfig remoteConfig;

    /***********************************************************************************************
     * *********************************** LifeCycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            page = getArguments().getString("page");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Set ViewBinding
        mBinding = FragmentWebBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null)
            page = savedInstanceState.getString("page");

        //Init UI
        if (page.equals("PRIVACY_POLICY")) {
            mBinding.webTitle.setText(R.string.privacy_policy_title);
            mBinding.back.setVisibility(View.VISIBLE);
            mBinding.ok.setVisibility(View.GONE);
            mBinding.webview.loadUrl(remoteConfig.getString("PRIVACY_POLICY_URL"));
        } else {
            mBinding.webTitle.setText(R.string.instructions_title);
            mBinding.back.setVisibility(View.GONE);
            mBinding.ok.setVisibility(View.VISIBLE);
            mBinding.webview.loadUrl(remoteConfig.getString("INSTRUCTIONS_URL"));
        }

        //Init Listeners
        mBinding.back.setOnClickListener(v -> requireActivity().onBackPressed());
        mBinding.ok.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("page", page);
    }
}