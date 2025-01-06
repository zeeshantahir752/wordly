package com.yesserly.wordly.utils.game;

import static com.yesserly.wordly.utils.Config.LANGUAGES;

import android.webkit.JavascriptInterface;

import com.yesserly.wordly.models.Word;
import com.yesserly.wordly.models.pojo.Mode;
import com.yesserly.wordly.utils.RoomConverters;
import com.yesserly.wordly.views.fragments.GameFragment;

public class GameAppInterface {

    public interface UIListener {

        boolean wordExists(String word);

        void addTry(String newTry);

        int getLettersNumber();

        Mode getMode();

    }

    public interface FragmentListener {

        void showRewarded();

    }

    private final UIListener listener;
    private final FragmentListener adListener;
    private final Word word;

    public GameAppInterface(Word word, UIListener listener, FragmentListener adListener) {
        this.word = word;
        this.listener = listener;
        this.adListener = adListener;
    }

    @JavascriptInterface
    public boolean isGameDone() {
        return word.isWon() || (word.getTrys() != null && word.getTrys().size() == word.getWord().length() + 1);
    }

    @JavascriptInterface
    public boolean isRTL() {
        if (listener.getMode() == null)
            return LANGUAGES[0].isRTL();
        else return listener.getMode().getLanguage().isRTL();
    }

    @JavascriptInterface
    public String getKeyboard() {
        if (listener.getMode() == null)
            return LANGUAGES[0].getKeyboard();
        else return listener.getMode().getLanguage().getKeyboard();
    }

    @JavascriptInterface
    public int getLettersNumber() {
        return listener.getLettersNumber();
    }

    @JavascriptInterface
    public String getWord() {
        return word.getWord();
    }

    @JavascriptInterface
    public String getTrys() {
        return RoomConverters.fromArrayList(word.getTrys());
    }

    @JavascriptInterface
    public boolean wordExists(String word) {
        return listener.wordExists(word);
    }

    @JavascriptInterface
    public void addTry(String word) {
        listener.addTry(word);
    }

    @JavascriptInterface
    public void rewind() {
        adListener.showRewarded();
    }
}
