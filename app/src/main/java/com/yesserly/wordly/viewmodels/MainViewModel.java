package com.yesserly.wordly.viewmodels;


import static com.yesserly.wordly.utils.Config.LANGUAGES;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yesserly.wordly.models.pojo.Language;
import com.yesserly.wordly.utils.JsonUtils;
import com.yesserly.wordly.utils.SharedPreferencesHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private static final String TAG = "MainViewModel";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private final FirebaseRemoteConfig remoteConfig;
    private final SharedPreferencesHelper mSharedPreferences;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    @Inject
    public MainViewModel(@ApplicationContext Context mContext, FirebaseRemoteConfig remoteConfig, SharedPreferencesHelper mSharedPreferences) {
        this.mContext = mContext;
        this.remoteConfig = remoteConfig;
        this.mSharedPreferences = mSharedPreferences;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    public void loadFiles() {
        //Load Files
        if (!remoteConfig.getString("FILES_VERSION").equals(mSharedPreferences.getFilesVersion()))
            loadFromAssets();
    }

    private void loadFromAssets() {
        for (Language language :
                LANGUAGES) {
            for (String file :
                    language.getFiles()) {
                try {
                    InputStreamReader input = new InputStreamReader(mContext.getAssets().open("words/" + file));
                    BufferedReader reader = new BufferedReader(input);
                    HashSet<String> words = new HashSet<>();
                    String line = reader.readLine();
                    int word_length = line.length();
                    while (line != null) {
                        words.add(line.toLowerCase());
                        line = reader.readLine();
                    }
                    mSharedPreferences.setWords(language.getName() + "-" + word_length, words);
                    input.close();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        mSharedPreferences.setVersion(remoteConfig.getString("FILES_VERSION"));
    }

}
