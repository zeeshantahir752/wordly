package com.yesserly.wordly.views.fragments;

import static com.yesserly.wordly.utils.Config.LANGUAGES;
import static com.yesserly.wordly.utils.Config.TIMES;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.webkit.WebViewAssetLoader;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yesserly.wordly.R;
import com.yesserly.wordly.base.AdsListener;
import com.yesserly.wordly.models.Word;
import com.yesserly.wordly.utils.game.AlarmReceiver;
import com.yesserly.wordly.utils.GDPR;
import com.yesserly.wordly.utils.game.GameAppInterface;
import com.yesserly.wordly.utils.game.LocalContentWebViewClient;
import com.yesserly.wordly.viewmodels.GameViewModel;
import com.yesserly.wordly.views.MainActivity;
import com.yesserly.wordly.views.dialogs.NewModeDialog;
import com.yesserly.wordly.views.dialogs.RewindDialog;
import com.yesserly.wordly.views.dialogs.SettingsDialog;
import com.yesserly.wordly.views.dialogs.StatisticsDialog;
import com.yesserly.wordly.databinding.FragmentGameBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GameFragment extends Fragment implements StatisticsDialog.StatisticsListener,
        SettingsDialog.SettingsListener, GameAppInterface.FragmentListener, RewindDialog.RewindListener {
    private static final String TAG = "GameFragment";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private GameViewModel mViewModel;
    private FragmentGameBinding mBinding;
    private AdsListener listener;

    private RewindDialog dialog = null;

    @Inject
    GDPR gdpr;
    @Inject
    FirebaseRemoteConfig remoteConfig;

    private boolean custom = false;

    //Observers
    private final Observer<Word> dbWordObserver = word -> {
        //Init Game
        initWebView(word);

        //Set Timer and Alarm
        mViewModel.setupCountdownTimer(requireContext());

    };
    private final Observer<Long> timerObserver = timestamp -> {
        //Create Notification
        Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
        int id;
        if (custom)
            id = 1;
        else id = 2;
        PendingIntent pi = PendingIntent.getBroadcast(requireContext(), id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) requireActivity()
                .getSystemService(Context.ALARM_SERVICE);

        long hours;
        if (custom)
            hours = TIMES[mViewModel.getMode().getTime()] * 3600000L;
        else hours = 24 * 3600000L;

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mViewModel.FirstAlarmTime(), hours, pi);
    };
    private final Observer<ArrayList<Word>> dbWordsObserver = words -> {
        StatisticsDialog dialog = new StatisticsDialog(words, mViewModel.getMode());
        dialog.show(getChildFragmentManager(), "StatisticsDialog");
    };
    private final Observer<Boolean> gameEndedObserver = won -> {
        new Handler().postDelayed(() -> {
            boolean showed = listener.showInterstitial(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (mViewModel.getWord().getDefinition() != null) {
                        RewindDialog dialog = new RewindDialog(mViewModel.getWord().getWord(), mViewModel.getWord().getDefinition());
                        dialog.show(getChildFragmentManager(), "RewindDialog");
                    } else openStatisticsDialog();
                }
            });
            if (!showed) {
                if (mViewModel.getWord().getDefinition() != null) {
                    RewindDialog dialog = new RewindDialog(mViewModel.getWord().getWord(), mViewModel.getWord().getDefinition());
                    dialog.show(getChildFragmentManager(), "RewindDialog");
                } else openStatisticsDialog();
            }
        }, 2000);
    };

    /***********************************************************************************************
     * *********************************** LifeCycle
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            custom = getArguments().getString("mode").equals("CUSTOM");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Set ViewBinding
        mBinding = FragmentGameBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set ViewModel
        mViewModel = new ViewModelProvider(this).get(GameViewModel.class);

        //Start
        if (savedInstanceState != null) {
            custom = savedInstanceState.getBoolean("custom");
            initWebView(mViewModel.getWord());
        } else {
            mViewModel.getLastWord(custom);
        }

        //Load Ads
        if (remoteConfig.getBoolean("ADS_ENABLED") && remoteConfig.getBoolean("INTERSTITIAL_END_AD_ENABLED"))
            listener.loadInterstitial(getString(R.string.INTERSTITIAL_END_AD_ID));

        if (remoteConfig.getBoolean("ADS_ENABLED") && remoteConfig.getBoolean("BANNER_GAME_AD_ENABLED")) {
            gdpr.loadAdBanner(mBinding.adView);
        } else mBinding.adView.setVisibility(View.GONE);

        if (remoteConfig.getBoolean("ADS_ENABLED") && remoteConfig.getBoolean("REWARDED_REWIND_AD_ENABLED"))
            listener.loadRewarded(getString(R.string.REWARDED_REWIND_AD_ID));

        //ClickListeners
        mBinding.statistics.setOnClickListener(v -> openStatisticsDialog());
        mBinding.instructions.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("page", "INSTRUCTIONS");
            NavHostFragment.findNavController(this).navigate(R.id.game_web, bundle);
        });
        mBinding.settings.setOnClickListener(v -> openSettingsDialog());
        mBinding.retry.setOnClickListener(v -> {
            mBinding.retry.setVisibility(View.GONE);
            mBinding.noInternet.setVisibility(View.GONE);
            mBinding.progress.setVisibility(View.VISIBLE);
            mBinding.gameWebview.reload();
        });

        //Set LiveData Observers
        mViewModel.getWordsData().observe(getViewLifecycleOwner(), dbWordsObserver);
        mViewModel.getWordData().observe(getViewLifecycleOwner(), dbWordObserver);
        mViewModel.getTimerData().observe(getViewLifecycleOwner(), timerObserver);
        mViewModel.getGameEndedData().observe(getViewLifecycleOwner(), gameEndedObserver);

        //Page Configurations
        if (!remoteConfig.getBoolean("INSTRUCTIONS_ENABLED"))
            mBinding.instructions.setVisibility(View.GONE);

        if (!remoteConfig.getBoolean("INTERNET_REQUIRED")) {
            mBinding.progress.setVisibility(View.GONE);
            mBinding.gameWebview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("custom", false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;//Clear mBinding
        mViewModel.getWordData().removeObserver(dbWordObserver);
        mViewModel.getWordsData().removeObserver(dbWordsObserver);
        mViewModel.getGameEndedData().removeObserver(gameEndedObserver);
        mViewModel.getTimerData().removeObserver(timerObserver);
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(Word word) {
        //Enable Javascript
        WebSettings webSettings = mBinding.gameWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //Add Callback
        mBinding.gameWebview.addJavascriptInterface(new GameAppInterface(word, mViewModel, this), "android");

        //Set Local Files
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .build();
        mBinding.gameWebview.setWebViewClient(new LocalContentWebViewClient(assetLoader));

        //Load HTML file
        mBinding.gameWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    if (remoteConfig.getBoolean("INTERNET_REQUIRED")) {
                        mBinding.progress.setVisibility(View.GONE);
                        if (mViewModel.isConnected()) {
                            mBinding.gameWebview.setVisibility(View.VISIBLE);
                            mBinding.noInternet.setVisibility(View.GONE);
                            mBinding.retry.setVisibility(View.GONE);
                            Log.d(TAG, "onProgressChanged: Found Internet");
                        } else {
                            mBinding.noInternet.setVisibility(View.VISIBLE);
                            mBinding.retry.setVisibility(View.VISIBLE);
                            mBinding.gameWebview.setVisibility(View.GONE);
                            Log.d(TAG, "onProgressChanged: No Internet");
                        }
                    } else mBinding.gameWebview.setVisibility(View.VISIBLE);
                }
            }
        });

        mBinding.gameWebview.loadUrl("https://appassets.androidplatform.net/assets/game/index.html");
    }

    private void openSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(mViewModel.isNotificationsOn());
        dialog.show(getChildFragmentManager(), "SettingsDialog");
    }

    private void openStatisticsDialog() {
        if (mViewModel.getWords() == null)
            mViewModel.getWordsList();
        else {
            StatisticsDialog dialog = new StatisticsDialog(mViewModel.getWords(), mViewModel.getMode());
            dialog.show(getChildFragmentManager(), "StatisticsDialog");
        }
    }

    @Override
    public void switchNotifications(boolean enabled) {
        mViewModel.setNotifications(enabled);
    }

    @Override
    public void rateUs() {
        Uri uri = Uri.parse("market://details?id=" + requireActivity().getPackageName());

        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
        }
    }

    @Override
    public void contactUs() {
        String[] TO = {remoteConfig.getString("DEVELOPER_EMAIL")};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback For " + getResources().getString(R.string.app_name));
        String message = "Message:\n\n---\n";
        try {
            PackageInfo pInfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            message = message + "App Version : " + version + "\n";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        message = message + "Android Version : " + android.os.Build.VERSION.SDK_INT + "\n";
        message = message + "Device Brand : " + Build.MANUFACTURER + "\n";
        message = message + "Device Model : " + Build.MODEL;

        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(requireActivity(),
                    getResources().getString(R.string.email_client), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void goPrivacyPolicy() {
        Bundle bundle = new Bundle();
        bundle.putString("page", "PRIVACY_POLICY");
        NavHostFragment.findNavController(this).navigate(R.id.game_web, bundle);
    }

    @Override
    public void shareLastWord(Word word) {
        if (custom) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            String sAux = getString(R.string.share_app_message) + "\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName();
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, getString(R.string.share_using)));
        } else {
            StringBuilder message = new StringBuilder();
            ArrayList<String> wrd = new ArrayList<>(Arrays.asList(word.getWord().split("")));

            message.append(getString(R.string.share_message))
                    .append(", https://play.google.com/store/apps/details?id=")
                    .append(requireActivity().getPackageName())
                    .append("\n");
            if (word.getTrys() != null && !word.getTrys().isEmpty()) {
                message.append(getString(R.string.last_score)).append("\n");
                for (String w :
                        word.getTrys()) {
                    ArrayList<String> ww = new ArrayList<>(Arrays.asList(w.split("")));
                    for (int i = 0; i < word.getWord().length(); i++) {
                        if (ww.get(i).equals(wrd.get(i)))
                            message.append(new String(Character.toChars(0x1F7E9)));//green
                        else if (wrd.contains(ww.get(i)))
                            message.append(new String(Character.toChars(0x1F7E8)));//yellow
                        else message.append(new String(Character.toChars(0x2B1B)));//grey
                    }
                    message.append("\n");
                }
            }

            //Share Last Score
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(android.content.Intent.EXTRA_TEXT, message.toString());
            startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
        }
    }

    @Override
    public void showRewarded() {
        if (dialog == null) {
            if (mViewModel.getWord().isRewinded()) {
                dialog = new RewindDialog(getString(R.string.rewind_benefited));
                dialog.show(getChildFragmentManager(), "RewindDialog");
            } else if (mViewModel.getWord().getTrys() != null && mViewModel.getWord().getTrys().size() == mViewModel.getWord().getWord().length() + 1) {
                dialog = new RewindDialog(getString(R.string.no_rewind_lost));
                dialog.show(getChildFragmentManager(), "RewindDialog");
            } else if (mViewModel.getWord().getTrys() == null || mViewModel.getWord().getTrys().isEmpty()) {
                dialog = new RewindDialog(getString(R.string.no_rewind_empty));
                dialog.show(getChildFragmentManager(), "RewindDialog");
            } else if (mViewModel.getWord().isWon()) {
                dialog = new RewindDialog(getString(R.string.no_rewind_won));
                dialog.show(getChildFragmentManager(), "RewindDialog");
            } else {
                dialog = new RewindDialog(getString(R.string.rewind_question));
                dialog.show(getChildFragmentManager(), "RewindDialog");
            }
        }
    }

    @Override
    public void rewind() {
        dialog = null;
        boolean showed = listener.showRewarded(rewardItem -> mViewModel.rewindTry());
        if (!showed) mViewModel.rewindTry();
    }

    @Override
    public void goBack() {
        dialog = null;
        requireActivity().onBackPressed();
    }

    @Override
    public void openStatistics() {
        dialog = null;
        openStatisticsDialog();
    }

    @Override
    public void dismissed() {
        dialog = null;
    }
}