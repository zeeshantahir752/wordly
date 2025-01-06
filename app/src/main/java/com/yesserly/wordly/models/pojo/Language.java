package com.yesserly.wordly.models.pojo;

public class Language {

    private String name;
    private String keyboard;
    private boolean RTL;
    private String[] files;
    private int[] lettersNumber;

    public Language(String name, String keyboard, boolean RTL, String[] files, int[] lettersNumber) {
        this.name = name;
        this.keyboard = keyboard;
        this.RTL = RTL;
        this.files = files;
        this.lettersNumber = lettersNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyboard() {
        return keyboard;
    }

    public void setKeyboard(String keyboard) {
        this.keyboard = keyboard;
    }

    public boolean isRTL() {
        return RTL;
    }

    public void setRTL(boolean RTL) {
        this.RTL = RTL;
    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public int[] getLettersNumber() {
        return lettersNumber;
    }

    public void setLettersNumber(int[] lettersNumber) {
        this.lettersNumber = lettersNumber;
    }

}
