package com.yesserly.wordly.views.dialogs;

import static com.yesserly.wordly.utils.Config.TIMES;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yesserly.wordly.R;
import com.yesserly.wordly.databinding.DialogStatisticsBinding;
import com.yesserly.wordly.models.Word;
import com.yesserly.wordly.models.pojo.Mode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class StatisticsDialog extends AppCompatDialogFragment {
    private static final String TAG = "StatisticsDialog";

    public interface StatisticsListener {

        void shareLastWord(Word word);

    }

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private final ArrayList<Word> words;
    private final Mode mode;
    private CountDownTimer timer;
    private StatisticsListener listener;
    private DialogStatisticsBinding mBinding;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    public StatisticsDialog(ArrayList<Word> words, Mode mode) {
        this.words = words;
        this.mode = mode;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (StatisticsListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement Callback interface");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timer.cancel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //Create Dialog Container
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        //Set ViewBinding
        mBinding = DialogStatisticsBinding.inflate(inflater, null, false);

        //Init Statistics
        initStatistics();

        //Init Listeners
        mBinding.close.setOnClickListener((v) -> dismiss());
        mBinding.share.setOnClickListener((v) -> listener.shareLastWord(words.get(words.size() - 1)));

        //Set Dialog
        Dialog dialog = builder.setView(mBinding.getRoot()).create();

        //Make Transparent Background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @SuppressLint("SetTextI18n")
    private void initStatistics() {
        int played = 0;
        int won = 0;
        int maxWinStreak = 0;
        int currentWinStreak = 0;

        //Set Definitions and Preparations
        if (words != null && !words.isEmpty()) {
            for (Word word :
                    words) {
                if (word.isWon()) {
                    played++;
                    won++;
                    currentWinStreak++;
                } else if (word.getTrys() != null && word.getTrys().size() == word.getWord().length() + 1) {
                    played++;
                    if (currentWinStreak > maxWinStreak) maxWinStreak = currentWinStreak;
                    currentWinStreak = 0;
                }
            }
        }

        //Set Stats
        mBinding.played.setText(String.valueOf(played));
        if (played != 0)
            mBinding.wins.setText(Math.floor(((float) won / (float) played) * 100) + "%");
        else mBinding.wins.setText("0%");
        mBinding.streak.setText(String.valueOf(currentWinStreak));
        mBinding.maxStreak.setText(String.valueOf(maxWinStreak));

        //Set Timer
        if (words != null) {
            Word word = words.get(words.size() - 1);
            long start = word.getTimestamp();
            long current = System.currentTimeMillis();
            long remaining;

            if (word.isCustom()) {
                long end = TIMES[mode.getTime()] * 3600000L;
                remaining = start + end - current;
            } else {
                //Get Universal Timezone
                TimeZone timeZone = TimeZone.getTimeZone("UTC");
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.set(Calendar.HOUR_OF_DAY, 24);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                Calendar localTime = Calendar.getInstance();
                localTime.setTimeInMillis(calendar.getTimeInMillis());

                //Make AlarmManager
                long end = localTime.getTimeInMillis();
                remaining = end - current;
            }

            //Set CountDownTimer
            timer = new CountDownTimer(remaining, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                    mBinding.wordTimer.setText(getString(R.string.timer, reformatTime(hours), reformatTime(minutes), reformatTime(seconds)));
                }

                public void onFinish() {
                }
            }.start();
        }
    }

    private String reformatTime(int time) {
        if (time < 10)
            return "0" + time;
        else return String.valueOf(time);
    }
}
