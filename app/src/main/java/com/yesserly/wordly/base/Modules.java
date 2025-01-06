package com.yesserly.wordly.base;

import static com.yesserly.wordly.utils.Config.DATABASE_NAME;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.work.WorkManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.yesserly.wordly.R;
import com.yesserly.wordly.models.daos.WordDao;
import com.yesserly.wordly.models.retrofit.WordService;
import com.yesserly.wordly.utils.GDPR;
import com.yesserly.wordly.utils.SharedPreferencesHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class Modules {
    private static final String TAG = "Modules";

    @Provides
    @Singleton
    public static AppDatabase providesAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("ALTER TABLE Word "
                                + " ADD COLUMN definition STRING");
                        database.execSQL("ALTER TABLE Word "
                                + " ADD COLUMN rewinded INTEGER NOT NULL");
                    }
                })
                .build();
    }

    @Singleton
    @Provides
    public WordDao providesCVsDao(AppDatabase mDB) {
        return mDB.wordDao();
    }

    @Singleton
    @Provides
    public static SharedPreferencesHelper providesSharedPreferences(@ApplicationContext Context context) {
        return new SharedPreferencesHelper(context.getSharedPreferences("BASIC", Context.MODE_PRIVATE));
    }

    @Provides
    @Singleton
    public static GDPR providesGDPR(FirebaseRemoteConfig remoteConfig,
                                    SharedPreferencesHelper sharedPreferencesHelper) {
        return new GDPR(remoteConfig, sharedPreferencesHelper);
    }

    @Provides
    @Singleton
    public static FirebaseRemoteConfig providesRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        return mFirebaseRemoteConfig;
    }

    @Provides
    @Singleton
    public static WordService provideWordsService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.dictionaryapi.dev/api/v2/entries/en/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(WordService.class);
    }

    @Provides
    @Singleton
    public static ExecutorService providesExecutor() {
        return new ThreadPoolExecutor(0,
                Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), (r, executor) -> System.out.println("Runnable rejected :: " + TAG));
    }
}
