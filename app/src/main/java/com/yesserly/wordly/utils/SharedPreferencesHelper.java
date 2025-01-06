package com.yesserly.wordly.utils;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.yesserly.wordly.models.pojo.Mode;

import java.util.ArrayList;
import java.util.HashSet;

public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferencesHelper";
    private static final String PERSONALIZED_ADS = "AD";
    private static final String MODE = "MODE";
    private static final String FILES_LOADED = "Files Loaded";
    private static final String NOTIFICATIONS = "Notifications";
    private static final String FIRST_TIME = "First Time";
    private static final String FIRST_RUN = "First Run";
    private static final String FILES_VERSION = "Files Version";
    private static final String LANGUAGES = "Languages";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    @SuppressLint("CommitPrefEdits")
    public SharedPreferencesHelper(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        editor = sharedPreferences.edit();
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    //Ads
    public void setAdPersonalized(boolean isPersonalized) {
        editor.putBoolean(PERSONALIZED_ADS, isPersonalized).apply();
    }

    public boolean isAdPersonalized() {
        return sharedPreferences.getBoolean(PERSONALIZED_ADS, true);
    }

    //Mode
    public void saveMode(Mode mode) {
        editor.putString(MODE, JsonUtils.object2Json(mode)).apply();
    }

    public Mode getMode() {
        return JsonUtils.json2Object(sharedPreferences.getString(MODE, null), Mode.class);
    }

    //Words List
    public void setWords(String file, HashSet<String> words) {
        editor.putStringSet(file, words).apply();
    }

    public HashSet<String> getWords(String file) {
        return (HashSet<String>) sharedPreferences.getStringSet(file, null);
    }

    //Notifications
    public void setNotifications(boolean enabled) {
        editor.putBoolean(NOTIFICATIONS, enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return sharedPreferences.getBoolean(NOTIFICATIONS, true);
    }

    //First Run
    public void setFirstRun() {
        editor.putBoolean(FIRST_RUN, false).apply();
    }

    public boolean isFirstRun() {
        return sharedPreferences.getBoolean(FIRST_RUN, true);
    }

    //Version
    public String getFilesVersion() {
        return sharedPreferences.getString(FILES_VERSION, "0");
    }

    public void setVersion(String files_version) {
        editor.putString(FILES_VERSION, files_version).apply();
    }
}
