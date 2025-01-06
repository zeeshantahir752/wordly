package com.yesserly.wordly.viewmodels;

import static com.yesserly.wordly.utils.Config.FIRST_DATE;
import static com.yesserly.wordly.utils.Config.LANGUAGES;
import static com.yesserly.wordly.utils.Config.TIMES;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yesserly.wordly.models.Word;
import com.yesserly.wordly.models.daos.WordDao;
import com.yesserly.wordly.models.pojo.Mode;
import com.yesserly.wordly.models.pojo.definitions.Definition;
import com.yesserly.wordly.models.pojo.definitions.Meaning;
import com.yesserly.wordly.models.pojo.definitions.WordDefinition;
import com.yesserly.wordly.models.retrofit.WordService;
import com.yesserly.wordly.utils.game.GameAppInterface;
import com.yesserly.wordly.utils.SharedPreferencesHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class GameViewModel extends ViewModel implements GameAppInterface.UIListener {
    private static final String TAG = "GameViewModel";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private final ExecutorService executorService;
    private final SharedPreferencesHelper mSharedPreferences;
    private final WordDao wordDao;
    private final WordService wordService;
    private CountDownTimer timer;

    //To use
    private Word word = null;
    private ArrayList<Word> words = null;
    private Mode mode = null;

    //LiveData
    private final MutableLiveData<Word> wordData = new MutableLiveData<>();
    private final MutableLiveData<Long> timerData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Word>> wordsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gameEndedData = new MutableLiveData<>();

    /***********************************************************************************************
     * *********************************** Constructor
     */
    @Inject
    public GameViewModel(ExecutorService executorService, WordDao wordDao,
                         SharedPreferencesHelper mSharedPreferences, WordService wordService) {
        this.executorService = executorService;
        this.wordDao = wordDao;
        this.mSharedPreferences = mSharedPreferences;
        this.wordService = wordService;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    public void getLastWord(boolean custom) {
        if (custom)
            mode = mSharedPreferences.getMode();
        executorService.execute(() -> {
            word = wordDao.getLastWord(custom);
            if (word == null || timePassed(word.getTimestamp()))
                generateNewWord();
            else wordData.postValue(word);
        });
    }

    private void generateNewWord() {
        //Get Random Word
        String w;
        if (mode != null) {
            Object[] words = mSharedPreferences.getWords(mode.getLanguage().getName() + "-" + mode.getLanguage().getLettersNumber()[mode.getLetters()]).toArray();
            //Get Random Word
            w = words[new Random().nextInt(words.length - 1)].toString();
        } else {
            Object[] words = mSharedPreferences.getWords(LANGUAGES[0].getName() + "-" + 5).toArray();

            //Get First/Second Date
            long first_timestamp = 0L;
            long second_timestamp = 0L;
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                //Get First Date
                first_timestamp = Objects.requireNonNull(formatter.parse(FIRST_DATE + " 12:00:00 AM")).getTime();
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                String todayDate = calendar.get(Calendar.DAY_OF_MONTH) + "-" +
                        (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR);
                second_timestamp = Objects.requireNonNull(formatter.parse(todayDate + " 12:00:00 AM")).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Get Random Word
            int pos = (int) Math.round(((double) second_timestamp - (double) first_timestamp) / 86400000L);
            w = words[pos].toString();
        }

        //Create New Word
        String finalW = w;
        executorService.execute(() -> {
            word = new Word(finalW, System.currentTimeMillis(), mode != null, false, false, null);
            Long id = wordDao.insertWord(word);
            word.setId(id);
            wordData.postValue(word);
            getDefinition(word.getWord());
            timerData.postValue(word.getTimestamp());
        });
    }

    private void getDefinition(String w) {
        wordService.getDefinition(w).enqueue(new Callback<WordDefinition[]>() {
            @Override
            public void onResponse(@NonNull Call<WordDefinition[]> call, @NonNull Response<WordDefinition[]> response) {
                if (response.isSuccessful()) {
                    String definition = "";
                    assert response.body() != null;
                    if (response.body().length > 0) {
                        WordDefinition wordDef = response.body()[0];
                        for (Meaning m : wordDef.getMeanings()) {
                            definition = definition + "(" + m.getPartOfSpeech() + "): ";
                            for (Definition d :
                                    m.getDefinitions()) {
                                definition = definition + d.getDefinition() + ".";
                                if (d.getExample() != null)
                                    definition = definition + "Example: " + d.getExample() + "\n";
                                else definition = definition + "\n";
                            }
                            definition = definition + "\n";
                        }
                        word.setDefinition(definition);
                        executorService.execute(() -> wordDao.updateWord(word));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WordDefinition[]> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void getWordsList() {
        executorService.execute(() -> {
            words = new ArrayList<>(wordDao.getWords(mode != null));
            wordsData.postValue(words);
        });
    }

    private boolean timePassed(Long timestamp) {
        if (mode != null)//Custom Mode
            return System.currentTimeMillis() >= (timestamp + TIMES[mode.getTime()] * 3600000L);
        else {
            //Normal Mode
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Calendar lastGame = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            lastGame.setTimeInMillis(timestamp);
            lastGame.set(Calendar.HOUR_OF_DAY, 0);
            lastGame.set(Calendar.MINUTE, 0);
            lastGame.set(Calendar.SECOND, 0);

            return Math.abs(lastGame.getTimeInMillis() - calendar.getTimeInMillis()) >= 60000L;
        }
    }

    public long FirstAlarmTime() {
        long time;

        if (mode != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.HOUR, TIMES[mode.getTime()]);
            time = calendar.getTimeInMillis();
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
            time = localTime.getTimeInMillis();
        }

        Log.d(TAG, "FirstAlarmTime: " + time);
        return time;
    }

    public void setupCountdownTimer(Context context) {
        long start = word.getTimestamp();
        long current = System.currentTimeMillis();
        long remaining;

        if (mode != null) {
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

        timer = new CountDownTimer(remaining, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Toast.makeText(context, "Generating a New Word", Toast.LENGTH_LONG).show();
                generateNewWord();
            }
        }.start();
    }

    public void rewindTry() {
        if (word != null && word.getTrys() != null && !word.getTrys().isEmpty()) {
            word.getTrys().remove(word.getTrys().size() - 1);
            word.setRewinded(true);
            executorService.execute(() -> {
                wordDao.updateWord(word);
                wordData.postValue(word);
            });
        }
    }

    @Override
    public boolean wordExists(String word) {
        if (mode != null)
            return mSharedPreferences.getWords(mode.getLanguage().getName() + "-" + mode.getLanguage().getLettersNumber()[mode.getLetters()])
                    .contains(word);
        else return mSharedPreferences.getWords(LANGUAGES[0].getName() + "-" + 5).contains(word);
    }

    @Override
    public void addTry(String newTry) {
        if (word.getTrys() == null)
            word.setTrys(new ArrayList<>());

        //Update Word
        word.getTrys().add(newTry);
        if (word.getWord().equals(newTry)) {
            word.setWon(true);
        }

        executorService.execute(() -> {
            wordDao.updateWord(word);
            if (word.isWon() || word.getTrys().size() == word.getWord().length() + 1) {
                //Update Last Word
                if (words == null)
                    words = new ArrayList<>();
                words.add(word);
                //Game Ended
                gameEndedData.postValue(word.isWon());
            }
        });
    }

    @Override
    public int getLettersNumber() {
        if (mode != null) return mode.getLanguage().getLettersNumber()[mode.getLetters()];
        else return 5;
    }

    public boolean isConnected() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 8.8.8.8");
            int returnVal = p1.waitFor();
            p1.destroy();
            return (returnVal == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Settings
    public void setNotifications(boolean enabled) {
        mSharedPreferences.setNotifications(enabled);
    }

    public boolean isNotificationsOn() {
        return mSharedPreferences.isNotificationsEnabled();
    }

    //Getters
    @Override
    public Mode getMode() {
        return mode;
    }

    public MutableLiveData<Word> getWordData() {
        return wordData;
    }

    public MutableLiveData<Long> getTimerData() {
        return timerData;
    }

    public MutableLiveData<ArrayList<Word>> getWordsData() {
        return wordsData;
    }

    public MutableLiveData<Boolean> getGameEndedData() {
        return gameEndedData;
    }

    public Word getWord() {
        return word;
    }

    public ArrayList<Word> getWords() {
        return words;
    }

    //OnCleared
    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null)
            timer.cancel();
    }
}