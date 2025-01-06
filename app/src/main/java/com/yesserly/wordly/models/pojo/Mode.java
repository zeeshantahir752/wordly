package com.yesserly.wordly.models.pojo;

public class Mode {

    private int time;
    private int letters;
    private Language language;

    public Mode(int time, int letters, Language language) {
        this.time = time;
        this.letters = letters;
        this.language = language;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getLetters() {
        return letters;
    }

    public void setLetters(int letters) {
        this.letters = letters;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
