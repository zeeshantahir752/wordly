package com.yesserly.wordly.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yesserly.wordly.models.pojo.Language;
import com.yesserly.wordly.models.pojo.Mode;
import com.yesserly.wordly.models.daos.WordDao;
import com.yesserly.wordly.utils.JsonUtils;
import com.yesserly.wordly.utils.SharedPreferencesHelper;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hilt_aggregated_deps._dagger_hilt_android_internal_managers_ViewComponentManager_ViewComponentBuilderEntryPoint;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private final ExecutorService executorService;
    private final FirebaseRemoteConfig remoteConfig;
    private final SharedPreferencesHelper mSharedPrefs;
    private final WordDao wordDao;

    //LiveData
    private final MutableLiveData<Boolean> customsCleared = new MutableLiveData<>();

    /***********************************************************************************************
     * *********************************** Constructor
     */
    @Inject
    public HomeViewModel(ExecutorService executorService, WordDao wordDao,
                         SharedPreferencesHelper mSharedPreferences, FirebaseRemoteConfig remoteConfig) {
        this.executorService = executorService;
        this.remoteConfig = remoteConfig;
        this.wordDao = wordDao;
        this.mSharedPrefs = mSharedPreferences;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    public void setMode(Mode mode) {
        mSharedPrefs.saveMode(mode);
    }

    public void clearOldCustom() {
        //Clear Custom Words
        executorService.execute(() -> {
            wordDao.clearCustoms();
            customsCleared.postValue(true);
        });
    }

    public boolean isFirstRun() {
        return mSharedPrefs.isFirstRun();
    }

    public void setFirstRun() {
        mSharedPrefs.setFirstRun();
    }

    //Getters
    public MutableLiveData<Boolean> getCustomsCleared(){
        return customsCleared;
    }

    public boolean customExists() {
        return mSharedPrefs.getMode() != null;
    }

    public int[] getTimes() {
        return JsonUtils.json2Object(remoteConfig.getString("TIMES"), int[].class);
    }

    public boolean isNotificationsOn() {
        return mSharedPrefs.isNotificationsEnabled();
    }
}